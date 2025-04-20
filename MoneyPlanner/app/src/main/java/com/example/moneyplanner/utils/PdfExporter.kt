package com.example.moneyplanner.utils

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Environment
import android.util.Log
import com.example.moneyplanner.data.Transaction
import com.example.moneyplanner.data.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val TAG = "PdfExporter"

    fun exportTransactions(transactions: List<Transaction>, isMonthly: Boolean = false): File {
        try {
            Log.d(TAG, "Starting PDF export with ${transactions.size} transactions")
            
            val document = PdfDocument()
            val pageInfo = PageInfo.Builder(595, 842, 1).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            val paint = android.graphics.Paint()

            // Title
            paint.textSize = 24f
            paint.color = Color.BLACK
            canvas.drawText(
                if (isMonthly) "Monthly Transaction Report" else "All Transactions Report",
                50f,
                50f,
                paint
            )

            // Date
            paint.textSize = 12f
            canvas.drawText(
                "Generated on: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                50f,
                80f,
                paint
            )

            // Table headers
            paint.textSize = 14f
            paint.isFakeBoldText = true
            var y = 120f
            canvas.drawText("Date", 50f, y, paint)
            canvas.drawText("Description", 150f, y, paint)
            canvas.drawText("Amount", 400f, y, paint)
            canvas.drawText("Type", 500f, y, paint)

            // Table rows
            paint.textSize = 12f
            paint.isFakeBoldText = false
            y += 20f

            var totalIncome = 0.0
            var totalExpenses = 0.0

            for (transaction in transactions) {
                if (y > 800f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = 50f
                }

                canvas.drawText(dateFormat.format(transaction.date), 50f, y, paint)
                canvas.drawText(transaction.description, 150f, y, paint)
                canvas.drawText("₹${String.format("%.2f", transaction.amount)}", 400f, y, paint)

                // Set color based on transaction type
                if (transaction.type == TransactionType.INCOME) {
                    paint.color = Color.GREEN
                    totalIncome += transaction.amount
                } else {
                    paint.color = Color.RED
                    totalExpenses += transaction.amount
                }
                canvas.drawText(transaction.type.name, 500f, y, paint)
                paint.color = Color.BLACK

                y += 20f
            }

            // Totals
            y += 20f
            paint.isFakeBoldText = true
            canvas.drawText("Total Income: ₹${String.format("%.2f", totalIncome)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Total Expenses: ₹${String.format("%.2f", totalExpenses)}", 50f, y, paint)
            y += 20f
            canvas.drawText(
                "Net Balance: ₹${String.format("%.2f", totalIncome - totalExpenses)}",
                50f,
                y,
                paint
            )

            document.finishPage(page)

            // Save the document
            val fileName = if (isMonthly) {
                "monthly_report_${SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())}.pdf"
            } else {
                "all_transactions_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.pdf"
            }

            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (documentsDir == null) {
                throw Exception("Could not access documents directory")
            }

            val file = File(documentsDir, fileName)
            Log.d(TAG, "Saving PDF to: ${file.absolutePath}")

            // Ensure directory exists
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            // Write the document
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()

            Log.d(TAG, "PDF export completed successfully")
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error during PDF export: ${e.message}", e)
            throw Exception("Failed to export PDF: ${e.message}")
        }
    }
} 