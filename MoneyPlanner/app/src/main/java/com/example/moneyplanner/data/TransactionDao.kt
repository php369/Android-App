package com.example.moneyplanner.data

import androidx.room.*
import java.util.Date

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsBetweenDates(startDate: Date, endDate: Date): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalIncome(startDate: Date, endDate: Date): Double

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpenses(startDate: Date, endDate: Date): Double

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE type = :type 
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalAmountByType(type: TransactionType, startDate: Date, endDate: Date): Double?

    @Query("""
        SELECT strftime('%Y-%m', date/1000, 'unixepoch') as month,
               SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income,
               SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense
        FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY strftime('%Y-%m', date/1000, 'unixepoch')
        ORDER BY month DESC
        LIMIT 6
    """)
    suspend fun getMonthlySummary(startDate: Date, endDate: Date): List<MonthlySummary>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

data class MonthlySummary(
    val month: String,
    val income: Double,
    val expense: Double
) 