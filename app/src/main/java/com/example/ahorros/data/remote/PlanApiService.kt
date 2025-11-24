package com.example.ahorros.data.remote

import com.example.ahorros.data.model.CreateMemberRequest
import com.example.ahorros.data.model.Member
import com.example.ahorros.data.model.Payment
import com.example.ahorros.data.model.Plan
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlanApiService {
    // Plans
    @GET("plans")
    suspend fun getPlans(): Response<List<Plan>>
    
    @GET("plans/{id}")
    suspend fun getPlanById(@Path("id") planId: String): Response<Plan>
    
    // Members
    @GET("members/plan/{planId}")
    suspend fun getMembersByPlan(@Path("planId") planId: String): Response<List<Member>>
    
    @POST("members")
    suspend fun createMember(@Body request: CreateMemberRequest): Response<Member>
    
    // Payments
    @GET("payments/plan/{planId}")
    suspend fun getPaymentsByPlan(@Path("planId") planId: String): Response<List<Payment>>
}