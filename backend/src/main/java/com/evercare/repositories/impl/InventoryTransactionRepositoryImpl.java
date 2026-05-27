package com.evercare.repositories.impl;

import com.evercare.pojo.InventoryTransaction;
import com.evercare.repositories.InventoryTransactionRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InventoryTransactionRepositoryImpl implements InventoryTransactionRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public void addTransaction(InventoryTransaction transaction) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(transaction);
        session.flush();
    }
}
