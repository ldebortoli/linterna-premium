package com.linternapremium.app.model

internal enum class CelebrationSoundKind { PEOPLE, PRIZE }

internal data class CelebrationSoundCue(
    val assetId: Int,
    val startMillis: Int,
    val gain: Float,
    val kind: CelebrationSoundKind,
)

// Full clips overlap before their audible tails; short voices cover quieter passages.
// Every occurrence starts at source offset zero and must finish naturally before second 15.
internal val PREMIUM_REAL_SOUND_CUES = listOf(
    CelebrationSoundCue(1934, 0, 0.30f, CelebrationSoundKind.PRIZE),
    CelebrationSoundCue(2011, 150, 0.36f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(459, 500, 0.30f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(2012, 3900, 0.36f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(1928, 4100, 0.30f, CelebrationSoundKind.PRIZE),
    CelebrationSoundCue(531, 6600, 0.28f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(2011, 7000, 0.34f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(437, 8600, 0.30f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(1934, 9150, 0.30f, CelebrationSoundKind.PRIZE),
    CelebrationSoundCue(2012, 13000, 0.36f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(2011, 14055, 0.36f, CelebrationSoundKind.PEOPLE),
)

internal val PREMIUM_APPROVED_CLIP_SAMPLES = mapOf(
    531 to 118_739,
    459 to 95_256,
    437 to 141_090,
    2012 to 22_822,
    2011 to 20_837,
    1934 to 90_357,
    1928 to 111_095,
)

internal const val PREMIUM_SYNTHETIC_GAIN = 0.30f
