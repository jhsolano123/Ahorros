package com.example.ahorros.data.repository

import com.example.ahorros.data.model.Payment
import com.example.ahorros.data.remote.PlanApiService
import com.example.ahorros.util.Resource
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementación del repositorio de pagos.
 * Maneja la comunicación con la API y el manejo de errores.
 */
class PaymentsRepositoryImpl(
    private val apiService: PlanApiService
) : PaymentsRepository {

    override suspend fun getPaymentsByPlan(planId: String): Resource<List<Payment>> {
        return try {
            val response = apiService.getPaymentsByPlan(planId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Success(emptyList())
                }
            } else {
                // El backend retorna 404 cuando no hay pagos, lo tratamos como lista vacía
                if (response.code() == 404) {
                    Resource.Success(emptyList())
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
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
