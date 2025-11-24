package com.example.ahorros.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un miembro de un plan de ahorro.
 */
data class Member(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val planId: String,
    val contributionPerMonth: Long,
    val joinedAt: String
)
