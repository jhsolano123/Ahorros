package com.example.ahorros.data.model

/**
 * Request para crear un nuevo miembro en un plan.
 */
data class CreateMemberRequest(
    val name: String,
    val planId: String,
    val contributionPerMonth: Long
)
