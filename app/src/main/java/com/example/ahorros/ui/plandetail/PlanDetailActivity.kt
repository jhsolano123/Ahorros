package com.example.ahorros.ui.plandetail

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

/**
 * Activity que muestra el detalle de un plan de ahorro.
 * Incluye información del plan, lista de miembros, pagos y progreso.
 */
class PlanDetailActivity : ComponentActivity() {

    private val viewModel: PlanDetailViewModel by viewModels {
        val planId = intent.getStringExtra(EXTRA_PLAN_ID) ?: ""
        PlanDetailViewModelFactory(planId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val planId = intent.getStringExtra(EXTRA_PLAN_ID)
        if (planId == null) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface {
                    PlanDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { finish() },
                        onAddPaymentClick = {
                            val intent = Intent(this, com.example.ahorros.ui.addpayment.AddPaymentActivity::class.java)
                            intent.putExtra("planId", planId)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando volvemos de otra pantalla
        viewModel.loadAllData()
    }

    companion object {
        const val EXTRA_PLAN_ID = "extra_plan_id"
    }
}
