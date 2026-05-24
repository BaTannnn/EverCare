package com.evercare.repositories.impl;

import com.evercare.pojo.MedicalService;
import com.evercare.repositories.MedicalServiceRepository;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
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
    private LocalSessionFactoryBean factory;

    @Override
    public List<MedicalService> getServices(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicalService> cq = cb.createQuery(MedicalService.class);
        Root<MedicalService> root = cq.from(MedicalService.class);

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

            String serviceType = params.get("serviceType");
            if (serviceType != null && !serviceType.isBlank()) {
                predicates.add(cb.equal(root.get("serviceType"), serviceType));
            }
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(root.get("name")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public MedicalService getServiceById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(MedicalService.class, id);
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
}