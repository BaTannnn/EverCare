package com.evercare.repositories.impl;

import com.evercare.enums.MedicalServiceType;
import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.TestResult;
import com.evercare.repositories.MedicalRecordServiceRepository;
import com.evercare.utils.PaginationUtils;
import com.evercare.utils.QueryPagingSupport;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Date;
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
public class MedicalRecordServiceRepositoryImpl implements MedicalRecordServiceRepository {
    @Autowired
    private LocalSessionFactoryBean factory;
    @Autowired
    private Environment env;
    @Override
    public void addMedicalRecordService(MedicalRecordService medicalRecordService) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(medicalRecordService);
        session.flush();
    }

    @Override
    public List<MedicalRecordService> getServicesByMedicalRecordId(Long recordId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicalRecordService> cq = cb.createQuery(MedicalRecordService.class);
        Root<MedicalRecordService> root = cq.from(MedicalRecordService.class);

        root.fetch("medicalRecordId", JoinType.INNER);
        root.fetch("serviceId", JoinType.INNER);

        cq.select(root).distinct(true);
        cq.where(
                cb.equal(root.get("medicalRecordId").get("id"), recordId),
                cb.isTrue(root.get("active"))
        );
        cq.orderBy(cb.asc(root.get("createdAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<String> getPendingResultServiceNames(Long recordId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MedicalRecordService> root = cq.from(MedicalRecordService.class);
        Join<MedicalRecordService, ?> serviceJoin = root.join("serviceId", JoinType.INNER);

        Subquery<Long> resultSubquery = cq.subquery(Long.class);
        Root<TestResult> resultRoot = resultSubquery.from(TestResult.class);
        resultSubquery.select(resultRoot.get("id"));
        resultSubquery.where(
                cb.isTrue(resultRoot.get("active")),
                cb.equal(resultRoot.get("medicalRecordId").get("id"), root.get("medicalRecordId").get("id")),
                cb.equal(resultRoot.get("serviceId").get("id"), serviceJoin.get("id"))
        );

        Expression<String> serviceType = cb.upper(cb.trim(serviceJoin.get("serviceType")));
        cq.multiselect(serviceJoin.get("id"), serviceJoin.get("name"));
        cq.distinct(true);
        cq.where(
                cb.equal(root.get("medicalRecordId").get("id"), recordId),
                cb.isTrue(root.get("active")),
                serviceType.in(
                        MedicalServiceType.TEST.getCode(),
                        MedicalServiceType.IMAGING.getCode()
                ),
                cb.not(cb.exists(resultSubquery))
        );
        cq.orderBy(cb.asc(serviceJoin.get("name")), cb.asc(root.get("id")));

        return session.createQuery(cq)
                .getResultList()
                .stream()
                .map(row -> row[1] != null ? row[1].toString() : "Dịch vụ #" + row[0])
                .distinct()
                .toList();
    }

    private Subquery<Long> pendingResultSubquery(CriteriaQuery<?> cq, CriteriaBuilder cb, Root<MedicalRecordService> root) {
        Subquery<Long> resultSubquery = cq.subquery(Long.class);
        Root<TestResult> testResult = resultSubquery.from(TestResult.class);
        resultSubquery.select(testResult.get("id"));
        resultSubquery.where(
                cb.isTrue(testResult.get("active")),
                cb.equal(testResult.get("medicalRecordId").get("id"), root.get("medicalRecordId").get("id")),
                cb.equal(testResult.get("serviceId").get("id"), root.get("serviceId").get("id"))
        );

        return resultSubquery;
    }

    private List<Predicate> getPendingTestRequestPredicates(
            Map<String, String> params,
            CriteriaQuery<?> cq,
            CriteriaBuilder cb,
            Root<MedicalRecordService> root
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.isTrue(root.get("medicalRecordId").get("active")));
        predicates.add(cb.not(cb.exists(pendingResultSubquery(cq, cb, root))));

        if (params != null) {
            String kw = params.get("kw");
            if ((kw == null || kw.isBlank()) && params.get("keyword") != null) {
                kw = params.get("keyword");
            }

            if (kw != null && !kw.isBlank()) {
                String searchValue = kw.trim().toLowerCase();
                String recordIdSearchValue = searchValue.startsWith("#") ? searchValue.substring(1) : searchValue;
                String keyword = "%" + searchValue + "%";
                String recordIdKeyword = "%" + recordIdSearchValue + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("medicalRecordId").get("recordCode")), keyword),
                        cb.like(root.get("medicalRecordId").get("id").as(String.class), recordIdKeyword),
                        cb.like(cb.lower(root.get("medicalRecordId").get("patientId").get("fullName")), keyword),
                        cb.like(cb.lower(root.get("medicalRecordId").get("patientId").get("patientCode")), keyword)
                ));
            }
        }

        return predicates;
    }

    @Override
    public List<MedicalRecordService> getPendingTestRequests(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        if (params != null && params.containsKey("page")) {
            CriteriaQuery<Long> idQuery = cb.createQuery(Long.class);
            Root<MedicalRecordService> idRoot = idQuery.from(MedicalRecordService.class);
            idQuery.select(idRoot.get("medicalRecordId").get("id"));
            idQuery.where(getPendingTestRequestPredicates(params, idQuery, cb, idRoot).toArray(Predicate[]::new));
            idQuery.groupBy(idRoot.get("medicalRecordId").get("id"));
            idQuery.orderBy(
                    cb.desc(cb.greatest(idRoot.<Date>get("createdAt"))),
                    cb.desc(idRoot.get("medicalRecordId").get("id"))
            );

            Query<Long> query = session.createQuery(idQuery);
            long totalElements = countPendingTestRequests(params);
            int pageSize = QueryPagingSupport.resolvePageSize(this.env, "staffTestRequest.pageSize", params, 10);
            int page = PaginationUtils.normalizePage(PaginationUtils.getPage(params), totalElements, pageSize);
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            List<Long> recordIds = query.getResultList();
            if (recordIds.isEmpty()) {
                return List.of();
            }

            CriteriaQuery<MedicalRecordService> pagedQuery = cb.createQuery(MedicalRecordService.class);
            Root<MedicalRecordService> pagedRoot = pagedQuery.from(MedicalRecordService.class);
            fetchPendingTestRequestGraph(pagedRoot);
            pagedQuery.select(pagedRoot).distinct(true);
            pagedQuery.where(
                    pagedRoot.get("medicalRecordId").get("id").in(recordIds),
                    cb.isTrue(pagedRoot.get("active")),
                    cb.isTrue(pagedRoot.get("medicalRecordId").get("active")),
                    cb.not(cb.exists(pendingResultSubquery(pagedQuery, cb, pagedRoot)))
            );
            pagedQuery.orderBy(cb.desc(pagedRoot.get("createdAt")), cb.desc(pagedRoot.get("id")));

            return session.createQuery(pagedQuery).getResultList();
        }

        CriteriaQuery<MedicalRecordService> cq = cb.createQuery(MedicalRecordService.class);
        Root<MedicalRecordService> root = cq.from(MedicalRecordService.class);

        fetchPendingTestRequestGraph(root);

        cq.select(root).distinct(true);
        cq.where(getPendingTestRequestPredicates(params, cq, cb, root).toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    private void fetchPendingTestRequestGraph(Root<MedicalRecordService> root) {
        Fetch<MedicalRecordService, ?> medicalRecordFetch = root.fetch("medicalRecordId", JoinType.INNER);
        medicalRecordFetch.fetch("patientId", JoinType.INNER);
        medicalRecordFetch.fetch("doctorId", JoinType.INNER);
        medicalRecordFetch.fetch("invoice", JoinType.LEFT);
        medicalRecordFetch.fetch("prescription", JoinType.LEFT);
        root.fetch("serviceId", JoinType.INNER);
    }

    private long countPendingTestRequests(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MedicalRecordService> root = cq.from(MedicalRecordService.class);

        cq.select(cb.countDistinct(root.get("medicalRecordId").get("id")));
        cq.where(getPendingTestRequestPredicates(params, cq, cb, root).toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }
}
