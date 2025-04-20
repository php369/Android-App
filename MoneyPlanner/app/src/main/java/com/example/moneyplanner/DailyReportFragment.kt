package com.example.moneyplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.moneyplanner.data.AppDatabase
import com.example.moneyplanner.data.Transaction
import com.example.moneyplanner.data.TransactionType
import com.example.moneyplanner.databinding.FragmentDailyReportBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DailyReportFragment : Fragment() {
    private var _binding: FragmentDailyReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var lineChart: LineChart
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyReportBinding.inflate(inflater, container, false)
        database = AppDatabase.getDatabase(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lineChart = binding.lineChart
        setupChart()
        loadData()
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return dateFormat.format(Date(value.toLong()))
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
            }

            legend.apply {
                isEnabled = true
                textColor = resources.getColor(android.R.color.black, null)
            }
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, -7)
            val startDate = calendar.time

            val transactions = database.transactionDao().getTransactionsBetweenDates(startDate, endDate)
            updateChart(transactions)
        }
    }

    private fun updateChart(transactions: List<Transaction>) {
        val incomeEntries = mutableListOf<Entry>()
        val expenseEntries = mutableListOf<Entry>()
        
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Initialize daily totals
        val dailyTotals = mutableMapOf<String, Pair<Double, Double>>()
        for (i in 0..6) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val date = dateFormat.format(calendar.time)
            dailyTotals[date] = Pair(0.0, 0.0)
        }
        
        // Calculate daily totals
        transactions.forEach { transaction ->
            val date = dateFormat.format(transaction.date)
            if (dailyTotals.containsKey(date)) {
                val (income, expense) = dailyTotals[date]!!
                if (transaction.type == TransactionType.INCOME) {
                    dailyTotals[date] = Pair(income + transaction.amount, expense)
                } else {
                    dailyTotals[date] = Pair(income, expense + transaction.amount)
                }
            }
        }
        
        // Create entries
        dailyTotals.entries.reversed().forEachIndexed { index, (_, totals) ->
            incomeEntries.add(Entry(index.toFloat(), totals.first.toFloat()))
            expenseEntries.add(Entry(index.toFloat(), totals.second.toFloat()))
        }

        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = resources.getColor(R.color.green, null)
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(true)
            valueTextColor = resources.getColor(R.color.white, null)
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Expenses").apply {
            color = resources.getColor(R.color.red, null)
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(true)
            valueTextColor = resources.getColor(R.color.white, null)
        }

        lineChart.data = LineData(incomeDataSet, expenseDataSet)
        lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 