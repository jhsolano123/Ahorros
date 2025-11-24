package com.example.ahorros.data.repository


import com.example.ahorros.data.model.Plan
import com.example.ahorros.util.Resource

interface PlansRepository {
    suspend fun getPlans(): Resource<List<Plan>>
    suspend fun getPlanById(planId: String): Resource<Plan>
}