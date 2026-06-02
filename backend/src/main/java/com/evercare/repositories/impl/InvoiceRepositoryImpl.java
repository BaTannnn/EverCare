package com.evercare.repositories.impl;

import com.evercare.enums.InvoiceStatus;
import com.evercare.pojo.*;
import com.evercare.repositories.InvoiceRepository;
import com.evercare.utils.QueryPagingSupport;
import jakarta.persistence.criteria.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        Fetch<Invoice, MedicalRecord> medicalRecordFetch =  root.fetch("medicalRecordId", JoinType.INNER);
        medicalRecordFetch.fetch("prescription", JoinType.LEFT);

        List<Predicate> predicates = buildReceptionistPredicates(params, cb, root, patientJoin, recordJoin);

        cq.select(root);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("id")));

        Query<Invoice> query = session.createQuery(cq);
        int pageSize = resolvePageSize(params);
        QueryPagingSupport.applyPaging(query, params, countInvoicesForReceptionist(params), pageSize);

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

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Invoice> query = builder.createQuery(Invoice.class);
        Root<Invoice> root = query.from(Invoice.class);

        root.fetch("medicalRecordId", JoinType.INNER);
        Fetch<Invoice, Patient> patientFetch = root.fetch("patientId", JoinType.INNER);
        patientFetch.fetch("userId", JoinType.LEFT);

        query.select(root);
        query.where(
                builder.equal(root.get("medicalRecordId").get("id"), medicalRecordId),
                builder.isTrue(root.get("active"))
        );

        return session.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Invoice> getInvoicesByPatientId(Long patientId, String paymentStatus, LocalDate from, LocalDate to) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Invoice> cq = cb.createQuery(Invoice.class);
        Root<Invoice> root = cq.from(Invoice.class);

        root.fetch("medicalRecordId", JoinType.LEFT);

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

        cq.select(root);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public Map<Long, BigDecimal> getTotalTestAmountsByMedicalRecordIds(List<Long> medicalRecordIds) {
        Map<Long, BigDecimal> totals = new HashMap<>();
        if (medicalRecordIds == null || medicalRecordIds.isEmpty()) {
            return totals;
        }

        List<Long> ids = medicalRecordIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return totals;
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MedicalRecordService> root = cq.from(MedicalRecordService.class);
        Join<MedicalRecordService, MedicalService> serviceJoin = root.join("serviceId", JoinType.INNER);

        Path<Long> recordIdPath = root.get("medicalRecordId").get("id");
        CriteriaBuilder.Coalesce<BigDecimal> unitPrice = cb.coalesce();
        unitPrice.value(root.get("unitPrice"));
        unitPrice.value(BigDecimal.ZERO);

        CriteriaBuilder.Coalesce<Integer> quantity = cb.coalesce();
        quantity.value(root.get("quantity"));
        quantity.value(0);

        Expression<BigDecimal> amount = cb.prod(unitPrice, quantity.as(BigDecimal.class));
        Expression<String> serviceType = cb.upper(cb.trim(serviceJoin.get("serviceType")));

        cq.multiselect(recordIdPath, cb.sum(amount));
        cq.where(
                cb.isTrue(root.get("active")),
                recordIdPath.in(ids),
                serviceType.in("TEST", "LAB_TEST")
        );
        cq.groupBy(recordIdPath);

        for (Object[] row : session.createQuery(cq).getResultList()) {
            if (row[0] instanceof Long recordId && row[1] instanceof BigDecimal total) {
                totals.put(recordId, total);
            }
        }

        return totals;
    }

    @Override
    public Invoice getInvoiceByPatientIdAndId(Long patientId, Long invoiceId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Invoice> query = builder.createQuery(Invoice.class);
        Root<Invoice> root = query.from(Invoice.class);
        root.fetch("medicalRecordId", JoinType.INNER);
        Fetch<Invoice, Patient> patientFetch = root.fetch("patientId", JoinType.INNER);

        patientFetch.fetch("userId", JoinType.LEFT);

        query.select(root);
        query.where(
                builder.isTrue(root.get("active")),
                builder.equal(root.get("id"), invoiceId),
                builder.equal(root.get("patientId").get("id"), patientId)
        );

        return session.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Invoice> query = builder.createQuery(Invoice.class);
        Root<Invoice> root = query.from(Invoice.class);
        root.fetch("medicalRecordId", JoinType.INNER);
        Fetch<Invoice, Patient> patientFetch = root.fetch("patientId", JoinType.INNER);

        patientFetch.fetch("userId", JoinType.LEFT);

        query.select(root);
        query.where(
                builder.isTrue(root.get("active")),
                builder.equal(root.get("id"), invoiceId)
        );

        return session.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElse(null);
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
        return QueryPagingSupport.resolvePageSize(this.env, "invoice.pageSize", params, 10);
    }
}
