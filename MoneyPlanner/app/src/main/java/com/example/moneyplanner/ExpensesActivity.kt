package com.example.moneyplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneyplanner.data.AppDatabase
import com.example.moneyplanner.data.Transaction
import com.example.moneyplanner.data.TransactionType
import com.example.moneyplanner.databinding.ActivityExpensesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ExpensesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpensesBinding
    private lateinit var database: AppDatabase
    private lateinit var expenseAdapter: TransactionAdapter
    private val expenses = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        // Setup RecyclerView
        setupRecyclerView()

        // Load expenses
        loadExpenses()

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        expenseAdapter = TransactionAdapter(expenses)
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(this@ExpensesActivity)
            adapter = expenseAdapter
        }
    }

    private fun loadExpenses() {
        CoroutineScope(Dispatchers.IO).launch {
            val allTransactions = database.transactionDao().getAllTransactions()
            val expenseTransactions = allTransactions.filter { it.type == TransactionType.EXPENSE }
            withContext(Dispatchers.Main) {
                expenses.clear()
                expenses.addAll(expenseTransactions)
                expenseAdapter.notifyDataSetChanged()
                updateTotalExpenses()
            }
        }
    }

    private fun updateTotalExpenses() {
        val totalExpenses = expenses.sumOf { it.amount }
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        binding.tvTotalExpenses.text = format.format(totalExpenses)
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_expenses -> {
                    // Already on expenses screen
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

        // Set the expenses as selected by default
        binding.bottomNavigation.selectedItemId = R.id.nav_expenses
    }
} 