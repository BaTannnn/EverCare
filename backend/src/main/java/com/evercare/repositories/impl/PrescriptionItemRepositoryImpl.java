package com.evercare.repositories.impl;

import com.evercare.pojo.PrescriptionItem;
import com.evercare.repositories.PrescriptionItemRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PrescriptionItemRepositoryImpl implements PrescriptionItemRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public void addItem(PrescriptionItem item) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(item);
    }

    @Override
    public void deleteItemsByPrescriptionId(Long prescriptionId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PrescriptionItem> cq = cb.createQuery(PrescriptionItem.class);
        Root<PrescriptionItem> root = cq.from(PrescriptionItem.class);

        cq.select(root);
        cq.where(cb.equal(root.get("prescriptionId").get("id"), prescriptionId));

        List<PrescriptionItem> items = session.createQuery(cq).getResultList();

        for (PrescriptionItem item : items) {
            session.remove(item);
        }
    }
}
