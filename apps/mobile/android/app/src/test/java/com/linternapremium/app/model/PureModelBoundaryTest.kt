package com.linternapremium.app.model

import com.linternapremium.app.ports.TorchPort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PureModelBoundaryTest {
    @Test
    fun `a dismissed celebration keeps the idle torch state and its acknowledgement`() {
        val idle = LinternaState()
        val dismissed = idle.copy(celebrationSequence = 3, dismissedCelebrationSequence = 3)
        assertFalse(idle.isTorchOn)
        assertFalse(dismissed.isTorchOn)
        assertEquals(dismissed.celebrationSequence, dismissed.dismissedCelebrationSequence)
    }

    @Test
    fun `optional torch observation is a safe no-op on ports without observation`() {
        val torch = object : TorchPort {
            override fun turnOnAtMaximum() = TorchResult.Success
            override fun setRelativeStrength(relativeStrength: Float) = TorchResult.Success
            override fun turnOff() = TorchResult.Success
        }
        var notified = false
        torch.observeState { notified = true }
        torch.observeState(null)
        assertFalse(notified)
    }

    @Test
    fun `audio primitives discard samples before and after a short destination buffer`() {
        // The public fixed plan stays in range. Exercise its defensive clipping independently
        // so a future sound-plan change cannot turn an offset error into an array write failure.
        val methods = listOf(
            "addReelClick" to arrayOf<Any>(-0.001f, 1),
            "addTrumpetTone" to arrayOf<Any>(-0.001f, 0.02f, 440f, 0.2f),
            "addCrowdCheer" to arrayOf<Any>(-0.001f, 1),
            "addVowel" to arrayOf<Any>(-0.001f, 0.02f, 164f, 340f, 880f, 0.1f),
            "addRolledR" to arrayOf<Any>(-0.001f, 0.02f, 164f, 1),
            "addTone" to arrayOf<Any>(-0.001f, 0.02f, 440f, 0.2f),
        )
        methods.forEach { (name, arguments) ->
            val method = PremiumCasinoSoundSynthesizer::class.java.declaredMethods.single { it.name == name }
            method.isAccessible = true
            val destination = FloatArray(64)
            method.invoke(PremiumCasinoSoundSynthesizer, destination, *arguments)
            assertTrue(name, destination.any { it != 0f })
            assertTrue(name, destination.all { it.isFinite() && kotlin.math.abs(it) <= 1f })
            method.invoke(PremiumCasinoSoundSynthesizer, FloatArray(0), *arguments)
        }
    }
}
