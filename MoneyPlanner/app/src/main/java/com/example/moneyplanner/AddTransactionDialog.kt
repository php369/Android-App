package com.example.moneyplanner

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionDialog : DialogFragment() {
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var etDate: TextInputEditText

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_add_transaction, null)

        etAmount = view.findViewById(R.id.etAmount)
        etDescription = view.findViewById(R.id.etDescription)
        actvCategory = view.findViewById(R.id.actvCategory)
        etDate = view.findViewById(R.id.etDate)

        setupCategoryDropdown()
        setupDatePicker()
        setupSaveButton(view)

        builder.setView(view)
        return builder.create()
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf(
            "Salary", "Freelance", "Investment", "Food", "Transportation",
            "Housing", "Utilities", "Entertainment", "Shopping", "Other"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        actvCategory.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        etDate.apply {
            setText(dateFormat.format(calendar.time))
            setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        setText(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    private fun setupSaveButton(view: android.view.View) {
        view.findViewById<android.widget.Button>(R.id.btnSave).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val description = etDescription.text.toString()
            val category = actvCategory.text.toString()
            val date = etDate.text.toString()

            if (amount == null || description.isEmpty() || category.isEmpty() || date.isEmpty()) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please fill all fields",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // TODO: Save transaction to database
            // For now, just dismiss the dialog
            dismiss()
        }
    }
} 