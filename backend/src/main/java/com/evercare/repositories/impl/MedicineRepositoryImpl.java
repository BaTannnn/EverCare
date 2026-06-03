package com.evercare.repositories.impl;

import com.evercare.pojo.Medicine;
import com.evercare.pojo.MedicineBatch;
import com.evercare.pojo.PrescriptionItem;
import com.evercare.repositories.MedicineRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
            if ((kw == null || kw.isBlank()) && params.get("keyword") != null) {
                kw = params.get("keyword");
            }

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
    public List<Object[]> getLowStockMedicines() {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<Medicine> root = cq.from(Medicine.class);
        Join<Medicine, MedicineBatch> batchJoin = root.join("medicineBatchSet", JoinType.LEFT);
        batchJoin.on(cb.isTrue(batchJoin.get("active")));

        Expression<Long> totalRemaining = cb.coalesce(cb.sumAsLong(batchJoin.<Integer>get("remainingQuantity")), 0L);
        Expression<Integer> minStockQuantity = cb.coalesce(root.<Integer>get("minStockQuantity"), 0);

        cq.multiselect(root, totalRemaining);
        cq.where(cb.isTrue(root.get("active")));
        cq.groupBy(root);
        cq.having(cb.le(totalRemaining, cb.toLong(minStockQuantity)));
        cq.orderBy(cb.asc(root.get("name")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public Medicine getMedicineById(Long id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Medicine.class, id);
    }

    @Override
    public List<Medicine> getActiveMedicinesByIds(List<Long> ids) {
        List<Long> normalizedIds = ids == null
                ? List.of()
                : ids.stream()
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList();

        if (normalizedIds.isEmpty()) {
            return List.of();
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Medicine> cq = cb.createQuery(Medicine.class);
        Root<Medicine> root = cq.from(Medicine.class);

        cq.select(root);
        cq.where(
                root.get("id").in(normalizedIds),
                cb.isTrue(root.get("active"))
        );

        return session.createQuery(cq).getResultList();
    }

    @Override
    public Medicine getMedicineByCode(String medicineCode) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Medicine> cq = cb.createQuery(Medicine.class);
        Root<Medicine> root = cq.from(Medicine.class);

        cq.select(root);
        cq.where(cb.equal(cb.lower(root.get("medicineCode")), medicineCode.trim().toLowerCase()));

        return session.createQuery(cq)
                .uniqueResult();
    }

    @Override
    public boolean existsByCode(String medicineCode, Long excludeId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Medicine> root = cq.from(Medicine.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(cb.lower(root.get("medicineCode")), medicineCode.trim().toLowerCase()));
        if (excludeId != null) {
            predicates.add(cb.notEqual(root.get("id"), excludeId));
        }

        cq.select(cb.count(root));
        cq.where(predicates.toArray(Predicate[]::new));

        Long count = session.createQuery(cq).getSingleResult();

        return count > 0;
    }

    @Override
    public boolean hasPrescriptionItems(Long medicineId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PrescriptionItem> root = cq.from(PrescriptionItem.class);

        cq.select(cb.count(root));
        cq.where(cb.equal(root.get("medicineId").get("id"), medicineId));

        Long count = session.createQuery(cq).getSingleResult();

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
