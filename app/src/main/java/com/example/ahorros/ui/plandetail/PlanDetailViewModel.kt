package com.example.ahorros.ui.plandetail

import androidx.lifecycle.ViewModel
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.lifecycle.viewModelScope
import com.example.ahorros.data.model.Member
import com.example.ahorros.data.model.Payment
import com.example.ahorros.data.model.Plan
import com.example.ahorros.data.model.CreateMemberRequest
import com.example.ahorros.data.repository.MembersRepositoryImpl
import com.example.ahorros.data.repository.PaymentsRepositoryImpl
import com.example.ahorros.data.repository.PlansRepositoryImpl
import com.example.ahorros.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//  UI STATE
data class PlanDetailUiState(
    val plan: Plan? = null,
    val members: List<Member> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val memberCreationError: String? = null,      // <-- nuevo
    val memberCreationSuccess: Boolean = false     // <-- nuevo
) {
    val totalCollected: Long
        get() = payments.sumOf { it.amount }

    val progressPercentage: Int
        get() = if (plan?.targetAmount != null && plan.targetAmount > 0) {
            ((totalCollected.toDouble() / plan.targetAmount) * 100).toInt().coerceIn(0, 100)
        } else 0
}



class PlanDetailViewModel(
    private val planId: String,
    private val plansRepository: PlansRepositoryImpl,
    private val membersRepository: MembersRepositoryImpl,
    private val paymentsRepository: PaymentsRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanDetailUiState())
    val uiState: StateFlow<PlanDetailUiState> = _uiState

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            loadPlan()
            loadMembers()
            loadPayments()

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun loadPlan() {
        when (val result = plansRepository.getPlanById(planId)) {
            is Resource.Success -> _uiState.value =
                _uiState.value.copy(plan = result.data)
            is Resource.Error -> _uiState.value =
                _uiState.value.copy(errorMessage = result.message)
            else -> {}
        }
    }

    private suspend fun loadMembers() {
        when (val result = membersRepository.getMembersByPlan(planId)) {
            is Resource.Success -> _uiState.value =
                _uiState.value.copy(members = result.data ?: emptyList())
            is Resource.Error -> _uiState.value =
                _uiState.value.copy(errorMessage = result.message)
            else -> {}
        }
    }

    private suspend fun loadPayments() {
        when (val result = paymentsRepository.getPaymentsByPlan(planId)) {
            is Resource.Success -> _uiState.value =
                _uiState.value.copy(payments = result.data ?: emptyList())
            is Resource.Error -> _uiState.value =
                _uiState.value.copy(errorMessage = result.message)
            else -> {}
        }
    }
    fun clearMemberCreationState() {
        _uiState.value = _uiState.value.copy(
            memberCreationError = null,
            memberCreationSuccess = false
        )
    }

    fun createMember(name: String, contribution: Long) {
        viewModelScope.launch {
            if (name.isBlank() || contribution <= 0) {
                _uiState.value = _uiState.value.copy(memberCreationError = "Nombre o contribución inválida")
                return@launch
            }

            // Crear el request para el repositorio
            val request = CreateMemberRequest(
                name = name,
                contributionPerMonth = contribution,
                planId = planId
            )

            // Llamar al repositorio
            when (val result = membersRepository.createMember(request)) {
                is Resource.Success -> {
                    val newMember = result.data ?: return@launch

                    // Asegurarse de que la lista no tenga nulls
                    val currentMembers = _uiState.value.members.filterNotNull()

                    _uiState.value = _uiState.value.copy(
                        memberCreationSuccess = true,
                        members = currentMembers + newMember
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(memberCreationError = result.message)
                }
                else -> {}
            }
        }
    }



    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
