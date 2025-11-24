package com.example.ahorros.data.repository

import com.example.ahorros.data.model.Payment
import com.example.ahorros.util.Resource

/**
 * Interfaz del repositorio para gestionar pagos.
 */
interface PaymentsRepository {
    suspend fun getPaymentsByPlan(planId: String): Resource<List<Payment>>
}
