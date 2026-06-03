package com.evercare.repositories.impl;

import com.evercare.enums.MedicalServiceType;
import com.evercare.pojo.MedicalService;
import com.evercare.repositories.MedicalServiceRepository;
import com.evercare.utils.PaginationUtils;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class MedicalServiceRepositoryImpl implements MedicalServiceRepository {

    @Autowired
    private Environment env;

    @Autowired
    private LocalSessionFactoryBean factory;

    List<Predicate> getPredicates(Map<String, String> params, CriteriaBuilder cb, Root root) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.isTrue(root.get("active")));

        if (params != null) {
            String kw = params.get("kw");
            if (kw != null && !kw.isBlank()) {
                String keyword = "%" + kw.trim().toLowerCase() + "%";

                Predicate byName = cb.like(cb.lower(root.get("name")), keyword);
                Predicate byCode = cb.like(cb.lower(root.get("code")), keyword);

                predicates.add(cb.or(byName, byCode));
            }

            String departmentId = params.get("departmentId");
            if (departmentId != null && !departmentId.isBlank()) {
                predicates.add(cb.equal(
                        root.get("departmentId").get("id"),
                        Integer.parseInt(departmentId)
                ));
            }

            String serviceTypes = params.get("serviceTypes");
            if (serviceTypes != null && !serviceTypes.isBlank()) {
                List<String> normalizedTypes = parseServiceTypes(serviceTypes);
                CriteriaBuilder.In<String> inClause = cb.in(root.get("serviceType"));
                normalizedTypes.forEach(inClause::value);
                predicates.add(inClause);
            } else {
                String serviceType = params.get("serviceType");
                if (serviceType != null && !serviceType.isBlank()) {
                    predicates.add(cb.equal(root.get("serviceType"), MedicalServiceType.normalize(serviceType)));
                }
            }
        }
        return predicates;
    }

    private List<String> parseServiceTypes(String serviceTypes) {
        List<String> normalizedTypes = new ArrayList<>();

        for (String type : serviceTypes.split(",")) {
            if (!type.isBlank()) {
                normalizedTypes.add(MedicalServiceType.normalize(type));
            }
        }

        if (normalizedTypes.isEmpty()) {
            throw new IllegalArgumentException("Loại dịch vụ không hợp lệ");
        }

        return normalizedTypes;
    }

    @Override
    public List<MedicalService> getServices(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicalService> cq = cb.createQuery(MedicalService.class);
        Root<MedicalService> root = cq.from(MedicalService.class);
        root.fetch("departmentId", JoinType.LEFT);

        List<Predicate> predicates = getPredicates(params, cb, root);

        cq.select(root).distinct(true);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(root.get("name")));

        Query<MedicalService> query = session.createQuery(cq);

        if (params != null && !params.isEmpty() && !Boolean.parseBoolean(params.getOrDefault("noPaging", "false"))) {
            int pageSize = this.env.getProperty("medicalService.pageSize", Integer.class);
            int page = PaginationUtils.normalizePage(PaginationUtils.getPage(params), this.countMedicalServices(params), pageSize);
            int start = (page - 1) * pageSize;

            query.setMaxResults(pageSize);
            query.setFirstResult(start);
        }

        return query.getResultList();
    }

    @Override
    public MedicalService getServiceById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(MedicalService.class, id);
    }

    @Override
    public List<MedicalService> getActiveExaminationServicesByDepartmentId(Long departmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<MedicalService> query = builder.createQuery(MedicalService.class);
        Root<MedicalService> root = query.from(MedicalService.class);
        root.fetch("departmentId", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.isTrue(root.get("active")));
        predicates.add(builder.equal(root.get("serviceType"), MedicalServiceType.EXAMINATION.getCode()));

        if (departmentId != null) {
            predicates.add(builder.equal(root.get("departmentId").get("id"), departmentId));
        }

        query.select(root).distinct(true);
        query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(builder.asc(root.get("name")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public void addService(MedicalService service) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(service);
    }

    @Override
    public void updateService(MedicalService service) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(service);
    }

    @Override
    public long countMedicalServices(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MedicalService> root = cq.from(MedicalService.class);
        cq.select(cb.count(root));

        List<Predicate> predicates = getPredicates(params, cb, root);

        cq.where(predicates.toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        long count = this.countMedicalServices(params);
        int pageSize = Integer.parseInt(env.getProperty("department.pageSize"));

        return (long) Math.max(1, Math.ceil((double) count / pageSize));
    }
}
