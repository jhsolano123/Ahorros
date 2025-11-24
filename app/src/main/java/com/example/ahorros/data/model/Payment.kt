package com.example.ahorros.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un pago realizado por un miembro a un plan.
 */
data class Payment(
    @SerializedName("_id")
    val id: String,
    val memberId: String,
    val planId: String,
    val amount: Long,
    val date: String
)
