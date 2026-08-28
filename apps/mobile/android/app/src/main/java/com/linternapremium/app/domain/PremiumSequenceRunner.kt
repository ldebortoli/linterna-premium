package com.linternapremium.app.domain

import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.ports.TorchPort

class PremiumSequenceRunner(
    private val torch: TorchPort,
    private val pause: suspend (Long) -> Unit,
) {
    private val steps = listOf(
        0.35f to 420L,
        1.00f to 520L,
        0.22f to 420L,
        1.00f to 520L,
        0.68f to 260L,
        0.38f to 280L,
        0.16f to 320L,
    )

    suspend fun run(): TorchResult {
        var sequenceResult: TorchResult = TorchResult.Success
        try {
            for ((strength, durationMillis) in steps) {
                val stepResult = torch.setRelativeStrength(strength)
                if (stepResult is TorchResult.Failure) {
                    sequenceResult = stepResult
                    break
                }
                pause(durationMillis)
            }
        } finally {
            val offResult = torch.turnOff()
            if (sequenceResult is TorchResult.Success) sequenceResult = offResult
        }
        return sequenceResult
    }
}
