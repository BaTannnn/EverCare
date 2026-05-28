package com.evercare.repositories.impl;

import com.evercare.pojo.PrescriptionItem;
import com.evercare.repositories.PrescriptionItemRepository;
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
        List<PrescriptionItem> items = session.createQuery("""
                SELECT item FROM PrescriptionItem item
                WHERE item.prescriptionId.id = :prescriptionId
                """, PrescriptionItem.class)
                .setParameter("prescriptionId", prescriptionId)
                .getResultList();

        for (PrescriptionItem item : items) {
            session.remove(item);
        }
    }
}
