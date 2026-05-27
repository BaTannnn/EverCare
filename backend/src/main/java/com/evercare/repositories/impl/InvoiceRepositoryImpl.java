package com.evercare.repositories.impl;

import com.evercare.pojo.Invoice;
import com.evercare.repositories.InvoiceRepository;
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
                SELECT i FROM Invoice i
                WHERE i.medicalRecordId.id = :medicalRecordId
                    AND i.active = true
                """, Invoice.class)
                .setParameter("medicalRecordId", medicalRecordId)
                .uniqueResult();
    }

    @Override
    public void updateInvoice(Invoice invoice) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(invoice);
    }
}
