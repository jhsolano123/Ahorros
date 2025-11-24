package com.example.ahorros.ui.plandetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ahorros.data.model.CreateMemberRequest
import com.example.ahorros.data.model.Member
import com.example.ahorros.data.model.Payment
import com.example.ahorros.data.model.Plan
import com.example.ahorros.data.repository.MembersRepository
import com.example.ahorros.data.repository.PaymentsRepository
import com.example.ahorros.data.repository.PlansRepository
import com.example.ahorros.util.Resource
import kotlinx.coroutines.launch

/**
 * Estado de la UI para el detalle del plan.
 * Incluye información del plan, miembros, pagos y cálculos de progreso.
 */
data class PlanDetailUiState(
    val isLoading: Boolean = false,
    val plan: Plan? = null,
    val members: List<Member> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val totalCollected: Long = 0,
    val progressPercentage: Float = 0f,
    val error: String? = null,
    val memberCreationSuccess: Boolean = false,
    val memberCreationError: String? = null
)

/**
 * ViewModel para la pantalla de detalle del plan.
 * Gestiona la lógica de negocio y cálculos de progreso del plan.
 */
class PlanDetailViewModel(
    private val plansRepository: PlansRepository,
    private val membersRepository: MembersRepository,
    private val paymentsRepository: PaymentsRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(PlanDetailUiState())
    val uiState: LiveData<PlanDetailUiState> = _uiState

    /**
     * Carga todos los datos del plan: información básica, miembros y pagos.
     * Calcula el progreso automáticamente.
     */
    fun loadPlanDetails(planId: String) {
        _uiState.value = _uiState.value?.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            // Cargar plan
            when (val planResult = plansRepository.getPlanById(planId)) {
                is Resource.Success -> {
                    val plan = planResult.data
                    _uiState.value = _uiState.value?.copy(plan = plan)
                    
                    // Cargar miembros
                    loadMembers(planId)
                    
                    // Cargar pagos
                    loadPayments(planId)
                }
                is Resource.Error -> {
                    _uiState.value = PlanDetailUiState(
                        isLoading = false,
                        error = planResult.message
                    )
                }
                is Resource.Loading -> {
                    // Ya está en loading
                }
            }
        }
    }

    /**
     * Carga los miembros del plan.
     */
    private suspend fun loadMembers(planId: String) {
        when (val membersResult = membersRepository.getMembersByPlan(planId)) {
            is Resource.Success -> {
                _uiState.value = _uiState.value?.copy(members = membersResult.data)
            }
            is Resource.Error -> {
                // No bloqueamos la UI si falla la carga de miembros
                _uiState.value = _uiState.value?.copy(members = emptyList())
            }
            is Resource.Loading -> {}
        }
    }

    /**
     * Carga los pagos del plan y calcula el progreso.
     */
    private suspend fun loadPayments(planId: String) {
        when (val paymentsResult = paymentsRepository.getPaymentsByPlan(planId)) {
            is Resource.Success -> {
                val payments = paymentsResult.data
                val totalCollected = calculateTotalCollected(payments)
                val progressPercentage = calculateProgress(totalCollected)
                
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    payments = payments,
                    totalCollected = totalCollected,
                    progressPercentage = progressPercentage
                )
            }
            is Resource.Error -> {
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    payments = emptyList(),
                    totalCollected = 0,
                    progressPercentage = 0f
                )
            }
            is Resource.Loading -> {}
        }
    }

    /**
     * Calcula el total recaudado sumando todos los pagos.
     */
    private fun calculateTotalCollected(payments: List<Payment>): Long {
        return payments.sumOf { it.amount }
    }

    /**
     * Calcula el porcentaje de progreso del plan.
     * Retorna un valor entre 0 y 100.
     */
    private fun calculateProgress(totalCollected: Long): Float {
        val plan = _uiState.value?.plan ?: return 0f
        if (plan.targetAmount == 0L) return 0f
        
        val percentage = (totalCollected.toFloat() / plan.targetAmount.toFloat()) * 100f
        return percentage.coerceIn(0f, 100f)
    }

    /**
     * Crea un nuevo miembro para el plan.
     */
    fun createMember(name: String, contributionPerMonth: Long) {
        val planId = _uiState.value?.plan?.id ?: return
        
        // Validaciones
        if (name.isBlank()) {
            _uiState.value = _uiState.value?.copy(
                memberCreationError = "El nombre no puede estar vacío"
            )
            return
        }
        
        if (contributionPerMonth <= 0) {
            _uiState.value = _uiState.value?.copy(
                memberCreationError = "La contribución debe ser mayor a 0"
            )
            return
        }
        
        viewModelScope.launch {
            val request = CreateMemberRequest(
                name = name,
                planId = planId,
                contributionPerMonth = contributionPerMonth
            )
            
            when (val result = membersRepository.createMember(request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value?.copy(
                        memberCreationSuccess = true,
                        memberCreationError = null
                    )
                    // Recargar miembros
                    loadMembers(planId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value?.copy(
                        memberCreationSuccess = false,
                        memberCreationError = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    /**
     * Limpia los estados de creación de miembro.
     */
    fun clearMemberCreationState() {
        _uiState.value = _uiState.value?.copy(
            memberCreationSuccess = false,
            memberCreationError = null
        )
    }
}
