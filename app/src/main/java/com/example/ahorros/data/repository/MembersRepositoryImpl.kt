package com.example.ahorros.data.repository

import com.example.ahorros.data.model.CreateMemberRequest
import com.example.ahorros.data.model.Member
import com.example.ahorros.data.remote.PlanApiService
import com.example.ahorros.util.Resource
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementación del repositorio de miembros.
 * Maneja la comunicación con la API y el manejo de errores.
 */
class MembersRepositoryImpl(
    private val apiService: PlanApiService
) : MembersRepository {

    override suspend fun getMembersByPlan(planId: String): Resource<List<Member>> {
        return try {
            val response = apiService.getMembersByPlan(planId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Success(emptyList())
                }
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: HttpException) {
            Resource.Error("Error de servidor: ${e.message()}")
        } catch (e: IOException) {
            Resource.Error("Error de conexión. Verifica tu red")
        } catch (e: Exception) {
            Resource.Error("Error inesperado: ${e.localizedMessage}")
        }
    }

    override suspend fun createMember(request: CreateMemberRequest): Resource<Member> {
        return try {
            val response = apiService.createMember(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respuesta vacía del servidor")
                }
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: HttpException) {
            Resource.Error("Error de servidor: ${e.message()}")
        } catch (e: IOException) {
            Resource.Error("Error de conexión. Verifica tu red")
        } catch (e: Exception) {
            Resource.Error("Error inesperado: ${e.localizedMessage}")
        }
    }
}
