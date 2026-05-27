package com.evercare.repositories;

import com.evercare.pojo.InventoryTransaction;

public interface InventoryTransactionRepository {
    void addTransaction(InventoryTransaction transaction);
}
