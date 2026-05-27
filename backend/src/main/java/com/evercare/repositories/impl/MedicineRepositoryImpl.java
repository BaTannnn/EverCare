package com.evercare.repositories.impl;

import com.evercare.pojo.Medicine;
import com.evercare.repositories.MedicineRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MedicineRepositoryImpl implements MedicineRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Medicine> getMedicines(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Medicine> cq = cb.createQuery(Medicine.class);
        Root<Medicine> root = cq.from(Medicine.class);

        List<Predicate> predicates = new ArrayList<>();

        if (params != null) {
            String active = params.get("active");
            if (active != null && !active.isBlank()) {
                predicates.add(Boolean.parseBoolean(active) ? cb.isTrue(root.get("active")) : cb.isFalse(root.get("active")));
            } else {
                predicates.add(cb.isTrue(root.get("active")));
            }

            String kw = params.get("kw");
            if (kw != null && !kw.isBlank()) {
                String keyword = "%" + kw.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("medicineCode")), keyword),
                        cb.like(cb.lower(root.get("name")), keyword)
                ));
            }

            String unit = params.get("unit");
            if (unit != null && !unit.isBlank()) {
                predicates.add(cb.equal(root.get("unit"), unit.trim().toUpperCase()));
            }
        } else {
            predicates.add(cb.isTrue(root.get("active")));
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(root.get("name")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public Medicine getMedicineById(Long id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Medicine.class, id);
    }

    @Override
    public Medicine getMedicineByCode(String medicineCode) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery(
                "SELECT m FROM Medicine m WHERE lower(m.medicineCode) = :medicineCode",
                Medicine.class
        )
                .setParameter("medicineCode", medicineCode.trim().toLowerCase())
                .uniqueResult();
    }

    @Override
    public boolean existsByCode(String medicineCode, Long excludeId) {
        Session session = this.factory.getObject().getCurrentSession();

        String hql = """
                SELECT COUNT(m) FROM Medicine m
                WHERE lower(m.medicineCode) = :medicineCode
                    AND (:excludeId IS NULL OR m.id <> :excludeId)
                """;

        Long count = session.createQuery(hql, Long.class)
                .setParameter("medicineCode", medicineCode.trim().toLowerCase())
                .setParameter("excludeId", excludeId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public boolean hasPrescriptionItems(Long medicineId) {
        Session session = this.factory.getObject().getCurrentSession();

        Long count = session.createQuery(
                "SELECT COUNT(p) FROM PrescriptionItem p WHERE p.medicineId.id = :medicineId",
                Long.class
        )
                .setParameter("medicineId", medicineId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void addMedicine(Medicine medicine) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(medicine);
    }

    @Override
    public void updateMedicine(Medicine medicine) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(medicine);
    }
}
