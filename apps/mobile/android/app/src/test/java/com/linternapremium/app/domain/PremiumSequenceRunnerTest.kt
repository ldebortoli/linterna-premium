package com.linternapremium.app.domain

import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.ports.TorchPort
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumSequenceRunnerTest {
    @Test
    fun `sequence follows the premium curve and always turns off`() = runBlocking {
        val torch = SequenceTorch()
        val pauses = mutableListOf<Long>()

        val result = PremiumSequenceRunner(torch, pause = { pauses += it }).run()

        assertEquals(TorchResult.Success, result)
        assertEquals(listOf(0.35f, 1f, 0.22f, 1f, 0.68f, 0.38f, 0.16f), torch.strengths)
        assertEquals(listOf(420L, 520L, 420L, 520L, 260L, 280L, 320L), pauses)
        assertEquals(1, torch.offCalls)
    }

    @Test
    fun `sequence stops on a strength failure but still turns off`() = runBlocking {
        val torch = SequenceTorch(failStrengthAt = 2)

        val result = PremiumSequenceRunner(torch, pause = {}).run()

        assertTrue(result is TorchResult.Failure)
        assertEquals(2, torch.strengths.size)
        assertEquals(1, torch.offCalls)
    }

    @Test
    fun `sequence reports a final turn off failure`() = runBlocking {
        val torch = SequenceTorch(turnOffResult = TorchResult.Failure("no se apago"))

        val result = PremiumSequenceRunner(torch, pause = {}).run()

        assertEquals(TorchResult.Failure("no se apago"), result)
        assertEquals(1, torch.offCalls)
    }

    @Test
    fun `cancelling the sequence still turns the torch off`() = runBlocking {
        val torch = SequenceTorch()
        var cancelled = false

        try {
            PremiumSequenceRunner(torch, pause = { throw CancellationException("app en segundo plano") }).run()
        } catch (_: CancellationException) {
            cancelled = true
        }

        assertTrue(cancelled)
        assertEquals(1, torch.strengths.size)
        assertEquals(1, torch.offCalls)
    }

    private class SequenceTorch(
        private val failStrengthAt: Int? = null,
        private val turnOffResult: TorchResult = TorchResult.Success,
    ) : TorchPort {
        val strengths = mutableListOf<Float>()
        var offCalls = 0

        override fun turnOnAtMaximum(): TorchResult = TorchResult.Success

        override fun setRelativeStrength(relativeStrength: Float): TorchResult {
            strengths += relativeStrength
            return if (strengths.size == failStrengthAt) {
                TorchResult.Failure("fallo de potencia")
            } else {
                TorchResult.Success
            }
        }

        override fun turnOff(): TorchResult {
            offCalls += 1
            return turnOffResult
        }
    }
}
