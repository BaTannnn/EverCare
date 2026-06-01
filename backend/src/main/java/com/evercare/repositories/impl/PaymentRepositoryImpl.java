package com.evercare.repositories.impl;

import com.evercare.enums.PaymentStatus;
import com.evercare.pojo.Payment;
import com.evercare.repositories.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PaymentRepositoryImpl implements PaymentRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Payment createPayment(Payment payment) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(payment);
        session.flush();
        return payment;
    }

    @Override
    public Payment updatePayment(Payment payment) {
        Session session = this.factory.getObject().getCurrentSession();
        payment = (Payment) session.merge(payment);
        session.flush();
        return payment;
    }

    @Override
    public Payment getPaymentByTransactionCode(String transactionCode) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT p
                FROM Payment p
                JOIN FETCH p.invoiceId i
                JOIN FETCH i.patientId patient
                LEFT JOIN FETCH patient.userId u
                LEFT JOIN FETCH i.medicalRecordId mr
                LEFT JOIN FETCH mr.appointmentId a
                WHERE p.active = true
                    AND p.transactionCode = :transactionCode
                """, Payment.class)
                .setParameter("transactionCode", transactionCode)
                .uniqueResult();
    }

    @Override
    public List<Payment> getPaymentsByInvoiceId(Long invoiceId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT p
                FROM Payment p
                JOIN FETCH p.invoiceId i
                WHERE p.active = true
                    AND i.id = :invoiceId
                ORDER BY p.createdAt ASC, p.id ASC
                """, Payment.class)
                .setParameter("invoiceId", invoiceId)
                .getResultList();
    }

    @Override
    public List<Payment> getSuccessPaymentsByInvoiceId(Long invoiceId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT p
                FROM Payment p
                JOIN FETCH p.invoiceId i
                WHERE p.active = true
                    AND i.id = :invoiceId
                    AND UPPER(p.paymentStatus) = :status
                ORDER BY p.createdAt ASC, p.id ASC
                """, Payment.class)
                .setParameter("invoiceId", invoiceId)
                .setParameter("status", PaymentStatus.SUCCESS.getCode())
                .getResultList();
    }

    @Override
    public BigDecimal sumSuccessAmountByInvoiceId(Long invoiceId) {
        Session session = this.factory.getObject().getCurrentSession();

        BigDecimal amount = session.createQuery("""
                SELECT COALESCE(SUM(p.amount), 0)
                FROM Payment p
                WHERE p.active = true
                    AND p.invoiceId.id = :invoiceId
                    AND UPPER(p.paymentStatus) = :status
                """, BigDecimal.class)
                .setParameter("invoiceId", invoiceId)
                .setParameter("status", PaymentStatus.SUCCESS.getCode())
                .uniqueResult();

        return amount != null ? amount : BigDecimal.ZERO;
    }
}
