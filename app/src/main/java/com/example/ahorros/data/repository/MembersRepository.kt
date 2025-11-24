package com.example.ahorros.data.repository

import com.example.ahorros.data.model.CreateMemberRequest
import com.example.ahorros.data.model.Member
import com.example.ahorros.util.Resource

/**
 * Interfaz del repositorio para gestionar miembros de planes.
 */
interface MembersRepository {
    suspend fun getMembersByPlan(planId: String): Resource<List<Member>>
    suspend fun createMember(request: CreateMemberRequest): Resource<Member>
}
