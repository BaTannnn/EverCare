package com.evercare.repositories.impl;

import com.evercare.enums.InvoiceStatus;
import com.evercare.pojo.Invoice;
import com.evercare.repositories.InvoiceRepository;
import com.evercare.utils.PaginationUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InvoiceRepositoryImpl implements InvoiceRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    @Override
    public List<Invoice> getInvoicesForReceptionist(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Invoice> cq = cb.createQuery(Invoice.class);
        Root<Invoice> root = cq.from(Invoice.class);
        Join<Invoice, com.evercare.pojo.Patient> patientJoin = root.join("patientId", JoinType.INNER);
        Join<Invoice, com.evercare.pojo.MedicalRecord> recordJoin = root.join("medicalRecordId", JoinType.INNER);

        root.fetch("patientId", JoinType.INNER);
        root.fetch("medicalRecordId", JoinType.INNER);
        root.fetch("paymentSet", JoinType.LEFT);

        List<Predicate> predicates = buildReceptionistPredicates(params, cb, root, patientJoin, recordJoin);

        cq.select(root).distinct(true);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("id")));

        Query<Invoice> query = session.createQuery(cq);
        int pageSize = resolvePageSize(params);
        int page = PaginationUtils.normalizePage(PaginationUtils.getPage(params), countInvoicesForReceptionist(params), pageSize);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public long countInvoicesForReceptionist(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Invoice> root = cq.from(Invoice.class);
        Join<Invoice, com.evercare.pojo.Patient> patientJoin = root.join("patientId", JoinType.INNER);
        Join<Invoice, com.evercare.pojo.MedicalRecord> recordJoin = root.join("medicalRecordId", JoinType.INNER);

        List<Predicate> predicates = buildReceptionistPredicates(params, cb, root, patientJoin, recordJoin);
        cq.select(cb.countDistinct(root));
        cq.where(predicates.toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }

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
            predicates.add(cb.equal(cb.upper(root.get("paymentStatus")), InvoiceStatus.normalize(paymentStatus)));
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
    public void addInvoice(Invoice invoice) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(invoice);
        session.flush();
    }

    @Override
    public void updateInvoice(Invoice invoice) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(invoice);
    }

    private List<Predicate> buildReceptionistPredicates(
            Map<String, String> params,
            CriteriaBuilder cb,
            Root<Invoice> root,
            Join<Invoice, com.evercare.pojo.Patient> patientJoin,
            Join<Invoice, com.evercare.pojo.MedicalRecord> recordJoin
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));

        String paymentStatus = params != null ? params.get("paymentStatus") : null;
        if (paymentStatus == null || paymentStatus.isBlank()) {
            predicates.add(cb.equal(cb.upper(root.get("paymentStatus")), InvoiceStatus.UNPAID.getCode()));
        } else {
            predicates.add(cb.equal(cb.upper(root.get("paymentStatus")), InvoiceStatus.normalize(paymentStatus)));
        }

        if (params != null) {
            String from = params.get("from");
            if (from != null && !from.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        java.sql.Timestamp.valueOf(LocalDate.parse(from.trim()).atStartOfDay())
                ));
            }

            String to = params.get("to");
            if (to != null && !to.isBlank()) {
                predicates.add(cb.lessThan(
                        root.get("createdAt"),
                        java.sql.Timestamp.valueOf(LocalDate.parse(to.trim()).plusDays(1).atStartOfDay())
                ));
            }

            String keyword = params.get("keyword");
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("invoiceCode")), like),
                        cb.like(cb.lower(patientJoin.get("patientCode")), like),
                        cb.like(cb.lower(patientJoin.get("fullName")), like),
                        cb.like(cb.lower(patientJoin.get("phone")), like),
                        cb.like(cb.lower(recordJoin.get("recordCode")), like)
                ));
            }
        }

        return predicates;
    }

    private int resolvePageSize(Map<String, String> params) {
        int defaultPageSize = this.env.getProperty("invoice.pageSize", Integer.class, 10);
        if (params == null) {
            return defaultPageSize;
        }

        String sizeValue = params.get("size");
        if (sizeValue == null || sizeValue.isBlank()) {
            return defaultPageSize;
        }

        try {
            int size = Integer.parseInt(sizeValue.trim());
            return size > 0 ? size : defaultPageSize;
        } catch (NumberFormatException ex) {
            return defaultPageSize;
        }
    }
}
