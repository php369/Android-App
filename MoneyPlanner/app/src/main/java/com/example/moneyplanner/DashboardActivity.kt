package com.example.moneyplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneyplanner.data.AppDatabase
import com.example.moneyplanner.data.Transaction
import com.example.moneyplanner.data.TransactionType
import com.example.moneyplanner.databinding.ActivityDashboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var database: AppDatabase
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        // Setup RecyclerView
        setupRecyclerView()

        // Load transactions
        loadTransactions()

        // Setup bottom navigation
        setupBottomNavigation()

        // Setup reset button
        setupResetButton()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(transactions)
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = transactionAdapter
        }
    }

    private fun loadTransactions() {
        CoroutineScope(Dispatchers.IO).launch {
            val allTransactions = database.transactionDao().getAllTransactions()
            withContext(Dispatchers.Main) {
                transactions.clear()
                transactions.addAll(allTransactions)
                transactionAdapter.notifyDataSetChanged()
                updateSummary()
            }
        }
    }

    private fun updateSummary() {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        val format = NumberFormat.getCurrencyInstance(Locale.US)
        binding.tvBalance.text = format.format(balance)
        binding.tvIncome.text = format.format(totalIncome)
        binding.tvExpenses.text = format.format(totalExpenses)
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Already on dashboard
                    true
                }
                R.id.nav_expenses -> {
                    startActivity(Intent(this, ExpensesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Set the dashboard as selected by default
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
    }

    private fun setupResetButton() {
        binding.btnReset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset All Transactions")
                .setMessage("Are you sure you want to delete all transactions? This action cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.transactionDao().deleteAllTransactions()
                        withContext(Dispatchers.Main) {
                            transactions.clear()
                            transactionAdapter.notifyDataSetChanged()
                            updateSummary()
                            Toast.makeText(this@DashboardActivity, "All transactions have been reset", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}

data class Transaction(
    val description: String,
    val amount: Double,
    val isIncome: Boolean,
    val date: String
) 