package com.example.moneyplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moneyplanner.databinding.FragmentMonthlyReportBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar

class MonthlyReportFragment : Fragment() {
    private var _binding: FragmentMonthlyReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        loadData()
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setMaxVisibleValueCount(60)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
            }
            
            axisRight.apply {
                axisMinimum = 0f
                granularity = 1f
            }
            
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            legend.apply {
                isEnabled = true
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                form = com.github.mikephil.charting.components.Legend.LegendForm.SQUARE
                formSize = 9f
                textSize = 11f
                xEntrySpace = 4f
            }
        }
    }

    private fun loadData() {
        val calendar = Calendar.getInstance()
        val months = mutableListOf<String>()
        val incomeEntries = mutableListOf<BarEntry>()
        val expenseEntries = mutableListOf<BarEntry>()

        // Generate data for the last 6 months
        for (i in 5 downTo 0) {
            calendar.add(Calendar.MONTH, -1)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            
            // Sample data - replace with actual data from your database
            val income = (1000..5000).random().toFloat()
            val expense = (500..3000).random().toFloat()
            
            incomeEntries.add(BarEntry(i.toFloat(), income))
            expenseEntries.add(BarEntry(i.toFloat(), expense))
            
            // Format month name
            val monthName = when (month) {
                Calendar.JANUARY -> "Jan"
                Calendar.FEBRUARY -> "Feb"
                Calendar.MARCH -> "Mar"
                Calendar.APRIL -> "Apr"
                Calendar.MAY -> "May"
                Calendar.JUNE -> "Jun"
                Calendar.JULY -> "Jul"
                Calendar.AUGUST -> "Aug"
                Calendar.SEPTEMBER -> "Sep"
                Calendar.OCTOBER -> "Oct"
                Calendar.NOVEMBER -> "Nov"
                Calendar.DECEMBER -> "Dec"
                else -> ""
            }
            months.add("$monthName $year")
        }

        val incomeDataSet = BarDataSet(incomeEntries, "Income").apply {
            color = ColorTemplate.MATERIAL_COLORS[0]
            valueTextColor = android.graphics.Color.BLACK
            valueTextSize = 10f
        }

        val expenseDataSet = BarDataSet(expenseEntries, "Expenses").apply {
            color = ColorTemplate.MATERIAL_COLORS[1]
            valueTextColor = android.graphics.Color.BLACK
            valueTextSize = 10f
        }

        val data = BarData(incomeDataSet, expenseDataSet).apply {
            barWidth = 0.3f
            groupBars(0f, 0.4f, 0.1f)
        }

        binding.barChart.apply {
            this.data = data
            xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(months)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 