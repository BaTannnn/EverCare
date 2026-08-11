package com.evercare.repositories.impl;

import com.evercare.pojo.MedicineBatch;
import com.evercare.repositories.MedicineBatchRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
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
import com.evercare.utils.QueryPagingSupport;

@Repository
@Transactional
public class MedicineBatchRepositoryImpl implements MedicineBatchRepository {
    @Autowired
    private LocalSessionFactoryBean factory;
    @Autowired
    private Environment env;
    private List<Predicate> getBatchPredicates(Map<String, String> params, CriteriaBuilder cb, Root<MedicineBatch> root, Join<MedicineBatch, ?> medicineJoin) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));

        if (params != null) {
            String medicineId = params.get("medicineId");
            if (medicineId != null && !medicineId.isBlank()) {
                predicates.add(cb.equal(root.get("medicineId").get("id"), Long.parseLong(medicineId)));
            }

            String kw = params.get("kw");
            if ((kw == null || kw.isBlank()) && params.get("keyword") != null) {
                kw = params.get("keyword");
            }

            if (kw != null && !kw.isBlank()) {
                String keyword = "%" + kw.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("batchCode")), keyword),
                        cb.like(cb.lower(medicineJoin.get("name")), keyword)
                ));
            }

            String status = params.get("status");
            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
                Date nearExpiryDate = java.sql.Date.valueOf(java.time.LocalDate.now().plusDays(30));

                if ("EXPIRED".equalsIgnoreCase(status)) {
                    predicates.add(cb.lessThan(root.<Date>get("expiryDate"), today));
                } else if ("NEAR_EXPIRY".equalsIgnoreCase(status)) {
                    predicates.add(cb.greaterThanOrEqualTo(root.<Date>get("expiryDate"), today));
                    predicates.add(cb.lessThanOrEqualTo(root.<Date>get("expiryDate"), nearExpiryDate));
                } else if ("VALID".equalsIgnoreCase(status) || "ENOUGH".equalsIgnoreCase(status)) {
                    predicates.add(cb.greaterThan(root.<Date>get("expiryDate"), nearExpiryDate));
                }
            }
        }

        return predicates;
    }

    @Override
    public List<MedicineBatch> getBatches(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        Join<MedicineBatch, ?> medicineJoin = root.join("medicineId", JoinType.INNER);
        root.fetch("medicineId", JoinType.INNER);

        cq.select(root).distinct(true);
        cq.where(getBatchPredicates(params, cb, root, medicineJoin).toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("importDate")), cb.desc(root.get("id")));

        Query<MedicineBatch> query = session.createQuery(cq);
        if (params != null && params.containsKey("page")) {
            int pageSize = QueryPagingSupport.resolvePageSize(this.env, "pharmacistMedicineBatch.pageSize", params, 10);
            QueryPagingSupport.applyPaging(query, params, countBatches(params), pageSize);
        }

        return query.getResultList();
    }

    private long countBatches(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        Join<MedicineBatch, ?> medicineJoin = root.join("medicineId", JoinType.INNER);

        cq.select(cb.countDistinct(root));
        cq.where(getBatchPredicates(params, cb, root, medicineJoin).toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    public List<MedicineBatch> getBatchesByMedicineId(Long medicineId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        root.fetch("medicineId", JoinType.INNER);

        cq.select(root).distinct(true);
        cq.where(
                cb.equal(root.get("medicineId").get("id"), medicineId),
                cb.isTrue(root.get("active"))
        );
        cq.orderBy(cb.asc(root.get("expiryDate")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public MedicineBatch getBatchById(Long id) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        root.fetch("medicineId", JoinType.INNER);

        cq.select(root).distinct(true);
        cq.where(
                cb.equal(root.get("id"), id),
                cb.isTrue(root.get("active"))
        );

        return session.createQuery(cq).uniqueResult();
    }

    @Override
    public MedicineBatch getBatchByCode(String batchCode) {
        if (batchCode == null || batchCode.isBlank()) {
            return null;
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        root.fetch("medicineId", JoinType.INNER);

        cq.select(root).distinct(true);
        cq.where(cb.equal(cb.lower(root.get("batchCode")), batchCode.trim().toLowerCase()));

        return session.createQuery(cq).uniqueResult();
    }

    @Override
    public Long getAvailableQuantityByMedicineId(Long medicineId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);

        cq.select(cb.coalesce(cb.sumAsLong(root.<Integer>get("remainingQuantity")), 0L));
        cq.where(
                cb.equal(root.get("medicineId").get("id"), medicineId),
                cb.isTrue(root.get("active")),
                cb.gt(root.<Integer>get("remainingQuantity"), 0)
        );

        Long total = session.createQuery(cq).getSingleResult();

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
    public List<MedicineBatch> getDispensableBatchesByMedicineIdForUpdate(Long medicineId, Date today) {
        return getDispensableBatchesByMedicineIdsForUpdate(List.of(medicineId), today);
    }

    @Override
    public List<MedicineBatch> getDispensableBatchesByMedicineIdsForUpdate(List<Long> medicineIds, Date today) {
        List<Long> ids = medicineIds == null
                ? List.of()
                : medicineIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);

        cq.select(root).distinct(true);
        cq.where(
                root.get("medicineId").get("id").in(ids),
                cb.isTrue(root.get("active")),
                cb.gt(root.<Integer>get("remainingQuantity"), 0),
                cb.greaterThanOrEqualTo(root.<Date>get("expiryDate"), today)
        );
        cq.orderBy(
                cb.asc(root.get("medicineId").get("id")),
                cb.asc(root.get("expiryDate")),
                cb.asc(root.get("id"))
        );

        return session.createQuery(cq)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }

    @Override
    public List<MedicineBatch> getNearExpiryBatches(Date toDate) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        Join<MedicineBatch, ?> medicineJoin = root.join("medicineId", JoinType.INNER);
        root.fetch("medicineId", JoinType.INNER);

        cq.select(root).distinct(true);
        cq.where(
                cb.isTrue(root.get("active")),
                cb.gt(root.<Integer>get("remainingQuantity"), 0),
                cb.lessThanOrEqualTo(root.<Date>get("expiryDate"), toDate)
        );
        cq.orderBy(cb.asc(root.get("expiryDate")), cb.asc(medicineJoin.get("name")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<MedicineBatch> getExpiredBatches(Date today) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicineBatch> cq = cb.createQuery(MedicineBatch.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);
        Join<MedicineBatch, ?> medicineJoin = root.join("medicineId", JoinType.INNER);
        root.fetch("medicineId", JoinType.INNER);

        cq.select(root).distinct(true);
        cq.where(
                cb.isTrue(root.get("active")),
                cb.gt(root.<Integer>get("remainingQuantity"), 0),
                cb.lessThan(root.<Date>get("expiryDate"), today)
        );
        cq.orderBy(cb.asc(root.get("expiryDate")), cb.asc(medicineJoin.get("name")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public boolean existsByBatchCode(String batchCode) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MedicineBatch> root = cq.from(MedicineBatch.class);

        cq.select(cb.count(root));
        cq.where(cb.equal(cb.lower(root.get("batchCode")), batchCode.trim().toLowerCase()));

        Long count = session.createQuery(cq).getSingleResult();

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
