package com.linternapremium.app.model

internal enum class CelebrationSoundKind { PEOPLE, PRIZE }

internal data class CelebrationSoundCue(
    val assetId: Int,
    val startMillis: Int,
    val durationMillis: Int,
    val gain: Float,
    val kind: CelebrationSoundKind,
)

// Alternate a short real celebration, a pause, and the original victory fanfare.
// At most one prize and one people clip at once. Mixkit 462 was explicitly rejected.
internal val PREMIUM_REAL_SOUND_CUES = listOf(
    CelebrationSoundCue(1934, 0, 1350, 0.40f, CelebrationSoundKind.PRIZE),
    CelebrationSoundCue(2011, 200, 850, 0.48f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(1928, 3350, 1300, 0.40f, CelebrationSoundKind.PRIZE),
    CelebrationSoundCue(459, 3500, 1150, 0.48f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(531, 6400, 1400, 0.56f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(1934, 9300, 1350, 0.40f, CelebrationSoundKind.PRIZE),
    CelebrationSoundCue(2012, 9350, 950, 0.48f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(437, 12400, 1400, 0.48f, CelebrationSoundKind.PEOPLE),
    CelebrationSoundCue(1928, 12400, 1300, 0.40f, CelebrationSoundKind.PRIZE),
)

internal const val PREMIUM_VICTORY_START_IN_ROUND_MILLIS = 1980
internal const val PREMIUM_REAL_SOUND_FADE_IN_MILLIS = 30
internal const val PREMIUM_REAL_SOUND_FADE_OUT_MILLIS = 140
