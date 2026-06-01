package com.evercare.repositories.impl;

import com.evercare.pojo.MedicineBatch;
import com.evercare.repositories.MedicineBatchRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MedicineBatchRepositoryImpl implements MedicineBatchRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<MedicineBatch> getBatches(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        root.fetch("medicineId");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));

        if (params != null) {
            String medicineId = params.get("medicineId");
            if (medicineId != null && !medicineId.isBlank()) {
                predicates.add(cb.equal(root.get("medicineId").get("id"), Long.parseLong(medicineId)));
            }

            String kw = params.get("kw");
            if (kw != null && !kw.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("batchCode")), "%" + kw.trim().toLowerCase() + "%"));
            }
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("importDate")), cb.desc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<MedicineBatch> getBatchesByMedicineId(Long medicineId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT b FROM MedicineBatch b
                JOIN FETCH b.medicineId m
                WHERE m.id = :medicineId
                    AND b.active = true
                ORDER BY b.expiryDate ASC, b.id ASC
                """, MedicineBatch.class)
                .setParameter("medicineId", medicineId)
                .getResultList();
    }

    @Override
    public Long getAvailableQuantityByMedicineId(Long medicineId) {
        Session session = this.factory.getObject().getCurrentSession();

        Long total = session.createQuery("""
                SELECT COALESCE(SUM(b.remainingQuantity), 0)
                FROM MedicineBatch b
                WHERE b.medicineId.id = :medicineId
                    AND b.active = true
                    AND b.remainingQuantity > 0
                """, Long.class)
                .setParameter("medicineId", medicineId)
                .getSingleResult();

        return total != null ? total : 0L;
    }

    @Override
    public Long getAvailableNonExpiredQuantityByMedicineId(Long medicineId, Date today) {
        if (medicineId == null) {
            return 0L;
        }

        return getAvailableNonExpiredQuantitiesByMedicineIds(List.of(medicineId), today)
                .getOrDefault(medicineId, 0L);
    }

    @Override
    public Map<Long, Long> getAvailableNonExpiredQuantitiesByMedicineIds(List<Long> medicineIds, Date today) {
        List<Long> ids = medicineIds == null
                ? List.of()
                : medicineIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<Long, Long> quantities = new HashMap<>();
        for (Long id : ids) {
            quantities.put(id, 0L);
        }

        if (ids.isEmpty()) {
            return quantities;
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);

        cq.multiselect(
                root.get("medicineId").get("id"),
                cb.sumAsLong(root.<Integer>get("remainingQuantity"))
        );
        cq.where(
                root.get("medicineId").get("id").in(ids),
                cb.isTrue(root.get("active")),
                cb.gt(root.<Integer>get("remainingQuantity"), 0),
                cb.greaterThanOrEqualTo(root.<Date>get("expiryDate"), today)
        );
        cq.groupBy(root.get("medicineId").get("id"));

        for (Object[] row : session.createQuery(cq).getResultList()) {
            Long medicineId = (Long) row[0];
            Number total = (Number) row[1];
            quantities.put(medicineId, total != null ? total.longValue() : 0L);
        }

        return quantities;
    }

    @Override
    public List<MedicineBatch> getDispensableBatchesByMedicineId(Long medicineId, Date today) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT b FROM MedicineBatch b
                JOIN FETCH b.medicineId m
                WHERE m.id = :medicineId
                    AND b.active = true
                    AND b.remainingQuantity > 0
                    AND b.expiryDate >= :today
                ORDER BY b.expiryDate ASC, b.id ASC
                """, MedicineBatch.class)
                .setParameter("medicineId", medicineId)
                .setParameter("today", today)
                .getResultList();
    }

    @Override
    public List<MedicineBatch> getDispensableBatchesByMedicineIdForUpdate(Long medicineId, Date today) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        root.fetch("medicineId");

        cq.select(root).distinct(true);
        cq.where(
                cb.equal(root.get("medicineId").get("id"), medicineId),
                cb.isTrue(root.get("active")),
                cb.gt(root.<Integer>get("remainingQuantity"), 0),
                cb.greaterThanOrEqualTo(root.<Date>get("expiryDate"), today)
        );
        cq.orderBy(cb.asc(root.get("expiryDate")), cb.asc(root.get("id")));

        return session.createQuery(cq)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }

    @Override
    public List<MedicineBatch> getNearExpiryBatches(Date toDate) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT b FROM MedicineBatch b
                JOIN FETCH b.medicineId m
                WHERE b.active = true
                    AND b.remainingQuantity > 0
                    AND b.expiryDate <= :toDate
                ORDER BY b.expiryDate ASC, m.name ASC, b.id ASC
                """, MedicineBatch.class)
                .setParameter("toDate", toDate)
                .getResultList();
    }

    @Override
    public List<MedicineBatch> getExpiredBatches(Date today) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT b FROM MedicineBatch b
                JOIN FETCH b.medicineId m
                WHERE b.active = true
                    AND b.remainingQuantity > 0
                    AND b.expiryDate < :today
                ORDER BY b.expiryDate ASC, m.name ASC, b.id ASC
                """, MedicineBatch.class)
                .setParameter("today", today)
                .getResultList();
    }

    @Override
    public boolean existsByBatchCode(String batchCode) {
        Session session = this.factory.getObject().getCurrentSession();

        Long count = session.createQuery(
                "SELECT COUNT(b) FROM MedicineBatch b WHERE lower(b.batchCode) = :batchCode",
                Long.class
        )
                .setParameter("batchCode", batchCode.trim().toLowerCase())
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void addBatch(MedicineBatch batch) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(batch);
        session.flush();
    }

    @Override
    public void updateBatch(MedicineBatch batch) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(batch);
    }
}
