package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val localeId = Locale("id", "ID")
    private val currencyFormat = NumberFormat.getCurrencyInstance(localeId).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatRupiah(amount: Double): String {
        return try {
            val formatted = currencyFormat.format(amount)
            // standard clean format "Rp 25.000"
            formatted.replace("Rp", "Rp ").trim()
        } catch (e: Exception) {
            "Rp " + String.format(Locale.US, "%,.0f", amount).replace(",", ".")
        }
    }

    fun formatRupiah(amount: Long): String = formatRupiah(amount.toDouble())

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", localeId)
        return sdf.format(Date(timestamp))
    }
}
