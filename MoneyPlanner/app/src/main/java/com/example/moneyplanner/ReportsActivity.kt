package com.example.moneyplanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneyplanner.data.AppDatabase
import com.example.moneyplanner.data.Transaction
import com.example.moneyplanner.data.TransactionType
import com.example.moneyplanner.databinding.ActivityReportsBinding
import com.example.moneyplanner.utils.PdfExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportsBinding
    private lateinit var database: AppDatabase
    private lateinit var pdfExporter: PdfExporter
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        pdfExporter = PdfExporter(this)

        // Setup bottom navigation
        setupBottomNavigation()

        // Load monthly summary
        loadMonthlySummary()
        setupExportButtons()
    }

    private fun loadMonthlySummary() {
        val calendar = Calendar.getInstance()
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transactions = database.transactionDao().getTransactionsBetweenDates(startOfMonth, endOfMonth)
                val totalIncome = transactions.filter { transaction -> transaction.type == TransactionType.INCOME }.sumOf { transaction -> transaction.amount }
                val totalExpenses = transactions.filter { transaction -> transaction.type == TransactionType.EXPENSE }.sumOf { transaction -> transaction.amount }
                val balance = totalIncome - totalExpenses

                withContext(Dispatchers.Main) {
                    binding.tvIncome.text = "₹${String.format("%.2f", totalIncome)}"
                    binding.tvExpenses.text = "₹${String.format("%.2f", totalExpenses)}"
                    binding.tvBalance.text = "₹${String.format("%.2f", balance)}"
                    binding.tvMonthYear.text = dateFormat.format(Date())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportsActivity, "Error loading monthly summary", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                    startActivity(Intent(this, ExpensesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_reports -> {
                    // Already on reports page
                    true
                }
                else -> false
            }
        }

        // Set the reports as selected by default
        binding.bottomNavigation.selectedItemId = R.id.nav_reports
    }

    private fun setupExportButtons() {
        binding.btnExportAll.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val transactions = database.transactionDao().getAllTransactions()
                    if (transactions.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ReportsActivity, "No transactions to export", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                    val pdfFile = pdfExporter.exportTransactions(transactions)
                    withContext(Dispatchers.Main) {
                        sharePdfFile(pdfFile, "All Transactions Report")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ReportsActivity,
                            "Failed to export: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        binding.btnExportMonth.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    val startOfMonth = calendar.time

                    calendar.add(Calendar.MONTH, 1)
                    calendar.add(Calendar.SECOND, -1)
                    val endOfMonth = calendar.time

                    val transactions = database.transactionDao().getTransactionsBetweenDates(startOfMonth, endOfMonth)
                    if (transactions.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ReportsActivity, "No transactions for this month to export", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                    val pdfFile = pdfExporter.exportTransactions(transactions, true)
                    withContext(Dispatchers.Main) {
                        sharePdfFile(pdfFile, "Monthly Report")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ReportsActivity,
                            "Failed to export: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun sharePdfFile(file: File, title: String) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to share PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadMonthlySummary()
    }
} 