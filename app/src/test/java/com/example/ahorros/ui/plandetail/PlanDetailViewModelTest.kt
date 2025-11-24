package com.example.ahorros.ui.plandetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.ahorros.data.model.Payment
import com.example.ahorros.data.model.Plan
import com.example.ahorros.data.repository.MembersRepository
import com.example.ahorros.data.repository.PaymentsRepository
import com.example.ahorros.data.repository.PlansRepository
import com.example.ahorros.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas unitarias para PlanDetailViewModel.
 * Valida el cálculo de progreso y la lógica de negocio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlanDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: PlanDetailViewModel
    private lateinit var mockPlansRepository: MockPlansRepository
    private lateinit var mockMembersRepository: MockMembersRepository
    private lateinit var mockPaymentsRepository: MockPaymentsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockPlansRepository = MockPlansRepository()
        mockMembersRepository = MockMembersRepository()
        mockPaymentsRepository = MockPaymentsRepository()
        
        viewModel = PlanDetailViewModel(
            mockPlansRepository,
            mockMembersRepository,
            mockPaymentsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `calcular progreso con 0 pagos debe retornar 0 porciento`() = runTest {
        // Arrange
        val plan = Plan(
            id = "1",
            name = "Test Plan",
            motive = "Test",
            targetAmount = 1000000,
            months = 12,
            createdAt = "2025-01-01"
        )
        mockPlansRepository.planToReturn = Resource.Success(plan)
        mockPaymentsRepository.paymentsToReturn = Resource.Success(emptyList())
        mockMembersRepository.membersToReturn = Resource.Success(emptyList())

        // Act
        viewModel.loadPlanDetails("1")

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(0L, uiState?.totalCollected)
        assertEquals(0f, uiState?.progressPercentage)
    }

    @Test
    fun `calcular progreso con 50 porciento de pagos`() = runTest {
        // Arrange
        val plan = Plan(
            id = "1",
            name = "Test Plan",
            motive = "Test",
            targetAmount = 1000000,
            months = 12,
            createdAt = "2025-01-01"
        )
        val payments = listOf(
            Payment("p1", "m1", "1", 500000, "2025-01-01")
        )
        mockPlansRepository.planToReturn = Resource.Success(plan)
        mockPaymentsRepository.paymentsToReturn = Resource.Success(payments)
        mockMembersRepository.membersToReturn = Resource.Success(emptyList())

        // Act
        viewModel.loadPlanDetails("1")

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(500000L, uiState?.totalCollected)
        assertEquals(50f, uiState?.progressPercentage)
    }

    @Test
    fun `calcular progreso con 100 porciento de pagos`() = runTest {
        // Arrange
        val plan = Plan(
            id = "1",
            name = "Test Plan",
            motive = "Test",
            targetAmount = 1000000,
            months = 12,
            createdAt = "2025-01-01"
        )
        val payments = listOf(
            Payment("p1", "m1", "1", 600000, "2025-01-01"),
            Payment("p2", "m1", "1", 400000, "2025-01-02")
        )
        mockPlansRepository.planToReturn = Resource.Success(plan)
        mockPaymentsRepository.paymentsToReturn = Resource.Success(payments)
        mockMembersRepository.membersToReturn = Resource.Success(emptyList())

        // Act
        viewModel.loadPlanDetails("1")

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(1000000L, uiState?.totalCollected)
        assertEquals(100f, uiState?.progressPercentage)
    }

    @Test
    fun `calcular progreso con mas de 100 porciento debe limitarse a 100`() = runTest {
        // Arrange
        val plan = Plan(
            id = "1",
            name = "Test Plan",
            motive = "Test",
            targetAmount = 1000000,
            months = 12,
            createdAt = "2025-01-01"
        )
        val payments = listOf(
            Payment("p1", "m1", "1", 800000, "2025-01-01"),
            Payment("p2", "m1", "1", 500000, "2025-01-02")
        )
        mockPlansRepository.planToReturn = Resource.Success(plan)
        mockPaymentsRepository.paymentsToReturn = Resource.Success(payments)
        mockMembersRepository.membersToReturn = Resource.Success(emptyList())

        // Act
        viewModel.loadPlanDetails("1")

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(1300000L, uiState?.totalCollected)
        assertEquals(100f, uiState?.progressPercentage) // Debe limitarse a 100%
    }

    @Test
    fun `validar creacion de miembro con nombre vacio debe fallar`() = runTest {
        // Arrange
        val plan = Plan(
            id = "1",
            name = "Test Plan",
            motive = "Test",
            targetAmount = 1000000,
            months = 12,
            createdAt = "2025-01-01"
        )
        mockPlansRepository.planToReturn = Resource.Success(plan)
        mockPaymentsRepository.paymentsToReturn = Resource.Success(emptyList())
        mockMembersRepository.membersToReturn = Resource.Success(emptyList())
        viewModel.loadPlanDetails("1")

        // Act
        viewModel.createMember("", 100000)

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals("El nombre no puede estar vacío", uiState?.memberCreationError)
    }

    @Test
    fun `validar creacion de miembro con contribucion 0 debe fallar`() = runTest {
        // Arrange
        val plan = Plan(
            id = "1",
            name = "Test Plan",
            motive = "Test",
            targetAmount = 1000000,
            months = 12,
            createdAt = "2025-01-01"
        )
        mockPlansRepository.planToReturn = Resource.Success(plan)
        mockPaymentsRepository.paymentsToReturn = Resource.Success(emptyList())
        mockMembersRepository.membersToReturn = Resource.Success(emptyList())
        viewModel.loadPlanDetails("1")

        // Act
        viewModel.createMember("Juan", 0)

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals("La contribución debe ser mayor a 0", uiState?.memberCreationError)
    }

    // Mock Repositories
    class MockPlansRepository : PlansRepository {
        var planToReturn: Resource<Plan> = Resource.Error("Not set")

        override suspend fun getPlans(): Resource<List<Plan>> {
            return Resource.Success(emptyList())
        }

        override suspend fun getPlanById(planId: String): Resource<Plan> {
            return planToReturn
        }
    }

    class MockMembersRepository : MembersRepository {
        var membersToReturn: Resource<List<com.example.ahorros.data.model.Member>> = Resource.Success(emptyList())

        override suspend fun getMembersByPlan(planId: String): Resource<List<com.example.ahorros.data.model.Member>> {
            return membersToReturn
        }

        override suspend fun createMember(request: com.example.ahorros.data.model.CreateMemberRequest): Resource<com.example.ahorros.data.model.Member> {
            return Resource.Success(
                com.example.ahorros.data.model.Member(
                    "1",
                    request.name,
                    request.planId,
                    request.contributionPerMonth,
                    "2025-01-01"
                )
            )
        }
    }

    class MockPaymentsRepository : PaymentsRepository {
        var paymentsToReturn: Resource<List<Payment>> = Resource.Success(emptyList())

        override suspend fun getPaymentsByPlan(planId: String): Resource<List<Payment>> {
            return paymentsToReturn
        }
    }
}
