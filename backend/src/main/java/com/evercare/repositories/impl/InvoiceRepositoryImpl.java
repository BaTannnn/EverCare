package com.evercare.repositories.impl;

import com.evercare.pojo.Invoice;
import com.evercare.repositories.InvoiceRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InvoiceRepositoryImpl implements InvoiceRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Invoice getInvoiceByMedicalRecordId(Long medicalRecordId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT i
                FROM Invoice i
                JOIN FETCH i.medicalRecordId mr
                JOIN FETCH i.patientId p
                LEFT JOIN FETCH p.userId u
                WHERE i.medicalRecordId.id = :medicalRecordId
                    AND i.active = true
                """, Invoice.class)
                .setParameter("medicalRecordId", medicalRecordId)
                .uniqueResult();
    }

    @Override
    public List<Invoice> getInvoicesByPatientId(Long patientId, String paymentStatus, LocalDate from, LocalDate to) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Invoice> cq = cb.createQuery(Invoice.class);
        Root<Invoice> root = cq.from(Invoice.class);

        root.fetch("medicalRecordId", jakarta.persistence.criteria.JoinType.LEFT);
        root.fetch("patientId");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.equal(root.get("patientId").get("id"), patientId));

        if (paymentStatus != null && !paymentStatus.isBlank()) {
            predicates.add(cb.equal(cb.upper(root.get("paymentStatus")), paymentStatus.trim().toUpperCase()));
        }

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    java.sql.Timestamp.valueOf(from.atStartOfDay())
            ));
        }

        if (to != null) {
            predicates.add(cb.lessThan(
                    root.get("createdAt"),
                    java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay())
            ));
        }

        cq.select(root).distinct(true);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public Invoice getInvoiceByPatientIdAndId(Long patientId, Long invoiceId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT i
                FROM Invoice i
                JOIN FETCH i.medicalRecordId mr
                JOIN FETCH i.patientId p
                LEFT JOIN FETCH p.userId u
                WHERE i.active = true
                    AND p.id = :patientId
                    AND i.id = :invoiceId
                """, Invoice.class)
                .setParameter("patientId", patientId)
                .setParameter("invoiceId", invoiceId)
                .uniqueResult();
    }

    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT i
                FROM Invoice i
                JOIN FETCH i.medicalRecordId mr
                JOIN FETCH i.patientId p
                LEFT JOIN FETCH p.userId u
                WHERE i.active = true
                    AND i.id = :invoiceId
                """, Invoice.class)
                .setParameter("invoiceId", invoiceId)
                .uniqueResult();
    }

    @Override
    public Invoice getInvoiceByAppointmentId(Long appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT i
                FROM Invoice i
                JOIN FETCH i.medicalRecordId mr
                JOIN FETCH i.patientId p
                LEFT JOIN FETCH p.userId u
                WHERE i.active = true
                    AND mr.appointmentId.id = :appointmentId
                """, Invoice.class)
                .setParameter("appointmentId", appointmentId)
                .uniqueResult();
    }

    @Override
    public void updateInvoice(Invoice invoice) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(invoice);
    }
}
