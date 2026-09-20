package com.example.data.repository

import com.example.data.local.PosDao
import com.example.data.model.OrderEntity
import com.example.data.model.TransactionLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing persistent storage, retrieval, and querying of transaction logs
 * for completed sales, audit trails, and the Recent Sales review interface.
 */
class TransactionLogRepository(private val posDao: PosDao) {

    val recentSales: Flow<List<TransactionLog>> = posDao.getRecentSales(limit = 50)
    val allLogs: Flow<List<TransactionLog>> = posDao.getAllTransactionLogs()

    suspend fun logTransaction(transaction: TransactionLog): Long {
        return posDao.insertTransactionLog(transaction)
    }

    suspend fun logOrder(
        order: OrderEntity,
        itemsSummary: String = "",
        totalItems: Int = 1,
        notes: String = ""
    ): Long {
        val log = TransactionLog(
            orderId = order.orderId,
            timestamp = order.timestamp,
            cashierName = order.cashierName,
            cashierRole = order.cashierRole,
            shiftName = order.shiftName,
            customerName = order.customerName,
            paymentMethod = order.paymentMethod,
            subtotal = order.subtotal,
            taxAmount = order.taxAmount,
            discountAmount = order.discountTotal,
            grandTotal = order.grandTotal,
            cashReceived = order.cashReceived,
            changeGiven = order.changeGiven,
            totalItems = totalItems,
            itemsSummary = itemsSummary.ifBlank { "Pesanan #${order.orderId}" },
            status = order.status,
            notes = notes.ifBlank { order.orderNote }
        )
        return posDao.insertTransactionLog(log)
    }

    suspend fun getLogByOrderId(orderId: String): TransactionLog? {
        return posDao.getTransactionLogByOrderId(orderId)
    }

    suspend fun deleteLog(id: Long) {
        posDao.deleteTransactionLog(id)
    }
}
