package com.evercare.repositories.impl;

import com.evercare.pojo.Employee;
import com.evercare.repositories.EmployeeRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class EmployeeRepositoryImpl implements EmployeeRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Employee getEmployeeByUserId(Long userId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery(
                "SELECT e FROM Employee e WHERE e.userId.id = :userId",
                Employee.class
        )
                .setParameter("userId", userId)
                .uniqueResult();
    }
}
