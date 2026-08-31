package com.linternapremium.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linternapremium.app.model.ErrorTarget
import com.linternapremium.app.model.LinternaState
import com.linternapremium.app.localization.AppLanguage
import com.linternapremium.app.localization.LinternaText
import com.linternapremium.app.localization.LinternaTextCatalog
import com.linternapremium.app.localization.TextKey
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

private val PremiumGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFF9D6CFF),
        Color(0xFFE26D9E),
        Color(0xFFF3B65F),
    ),
)

private val FireworkColors = listOf(
    Color(0xFFFFD166),
    Color(0xFFE26D9E),
    Color(0xFF9D6CFF),
    Color(0xFF6DD6C2),
    Color(0xFFFF8A5B),
    Color(0xFF65B8FF),
)

private val ReelSymbols = listOf("7", "★", "♦", "♛", "●")

@Composable
fun LinternaPremiumScreen(
    state: LinternaState,
    text: LinternaText,
    selectedLanguage: AppLanguage,
    adsReady: Boolean,
    adUnitId: String,
    isDemo: Boolean,
    onTurnOn: () -> Unit,
    onPremium: () -> Unit,
    onNormalOff: () -> Unit,
    onConfirmPurchase: () -> Unit,
    onDismissPurchase: () -> Unit,
    onDismissOffer: () -> Unit,
    onResetDemoPremium: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    val celebration = remember { Animatable(0f) }
    var celebrationVisible by remember { mutableStateOf(false) }
    LaunchedEffect(state.celebrationSequence, state.dismissedCelebrationSequence) {
        celebrationVisible = false
        if (state.celebrationSequence > state.dismissedCelebrationSequence) {
            try {
                celebrationVisible = true
                celebration.snapTo(0f)
                celebration.animateTo(1f, tween(3_000, easing = LinearEasing))
            } finally {
                celebrationVisible = false
            }
        }
    }
    val premiumGlow = if (celebrationVisible) premiumGlowAt(celebration.value) else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0D10), Color(0xFF11161C), Color(0xFF090B0E)),
                ),
            )
            .drawBehind {
                if (celebrationVisible) {
                    drawRect(
                        color = Color(0xFFFFE8A3),
                        alpha = 0.025f + premiumGlowAt(celebration.value) * 0.055f,
                    )
                    drawCircle(
                        brush = PremiumGradient,
                        radius = size.minDimension * (0.28f + celebration.value * 0.72f),
                        center = Offset(size.width / 2f, size.height * 0.4f),
                        alpha = 0.12f * (1f - celebration.value),
                    )
                }
            },
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (adsReady && !state.isPremiumOwned && adUnitId.isNotBlank()) {
                    LinternaAdBanner(
                        adUnitId = adUnitId,
                        isDemo = isDemo,
                        testAdLabel = text[TextKey.TEST_AD],
                        modifier = Modifier.navigationBarsPadding(),
                    )
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppHeader(isPremiumOwned = state.isPremiumOwned, text = text)
                LanguageSelector(
                    text = text,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected,
                )
                Spacer(Modifier.height(18.dp))
                FlashlightHero(isOn = state.isTorchOn, premiumGlow = premiumGlow, text = text)
                Spacer(Modifier.height(24.dp))

                AnimatedContent(
                    targetState = state.isTorchOn || state.showPremiumOffer,
                    label = "acciones-linterna",
                ) { showOffOptions ->
                    if (showOffOptions) {
                        OffOptions(
                            state = state,
                            text = text,
                            onPremium = onPremium,
                            onNormalOff = onNormalOff,
                            onDismissOffer = onDismissOffer,
                        )
                    } else {
                        TurnOnPanel(
                            state = state,
                            text = text,
                            isDemo = isDemo,
                            onTurnOn = onTurnOn,
                            onResetDemoPremium = onResetDemoPremium,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        if (celebrationVisible) {
            PremiumFireworks(
                progress = celebration.value,
                text = text,
                modifier = Modifier.matchParentSize(),
            )
        }
    }

    if (state.showPurchaseDialog) {
        PremiumPurchaseDialog(
            priceLabel = state.priceLabel,
            text = text,
            isDemo = isDemo,
            errorMessage = state.error.takeIf { state.errorTarget == ErrorTarget.PREMIUM },
            onConfirm = onConfirmPurchase,
            onDismiss = onDismissPurchase,
        )
    }
}

@Composable
private fun AppHeader(isPremiumOwned: Boolean, text: LinternaText) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "LINTERNA",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp,
            )
            Text(
                text = "PREMIUM",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Surface(
            color = if (isPremiumOwned) Color(0xFF302A19) else Color(0xFF1B2026),
            shape = RoundedCornerShape(999.dp),
        ) {
            Text(
                text = if (isPremiumOwned) text[TextKey.PREMIUM_ACTIVE] else text[TextKey.PLEBEIAN_EDITION],
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                color = if (isPremiumOwned) Color(0xFFF3D27A) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp,
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    text: LinternaText,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Box {
            TextButton(
                onClick = { expanded = true },
                modifier = Modifier.semantics {
                    contentDescription = text[TextKey.APP_LANGUAGE]
                },
            ) {
                Text(
                    text = "🌐 ${text[TextKey.LANGUAGES]} · ${selectedLanguage.nativeLabel}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                scrollState = rememberScrollState(),
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .widthIn(min = 220.dp, max = 320.dp),
            ) {
                LinternaTextCatalog.supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (language == selectedLanguage) {
                                    "✓ ${language.nativeLabel}"
                                } else {
                                    language.nativeLabel
                                },
                            )
                        },
                        onClick = {
                            expanded = false
                            onLanguageSelected(language)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashlightHero(isOn: Boolean, premiumGlow: Float?, text: LinternaText) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulso-linterna")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "intensidad-luz",
    )
    val visualIntensity = premiumGlow ?: if (isOn) pulse else 0f
    Box(
        modifier = Modifier
            .size(204.dp)
            .drawBehind {
                if (visualIntensity > 0.01f) {
                    drawCircle(
                        color = Color(0xFFF4D478),
                        radius = size.minDimension * (0.32f + 0.16f * visualIntensity),
                        alpha = 0.10f * visualIntensity,
                    )
                    drawCircle(
                        color = Color(0xFFF4D478),
                        radius = size.minDimension * (0.24f + 0.10f * visualIntensity),
                        alpha = 0.18f * visualIntensity,
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(126.dp),
            color = blendColor(Color(0xFF1D232A), Color(0xFFF1D170), visualIntensity),
            shape = CircleShape,
            shadowElevation = if (visualIntensity > 0.05f) 14.dp else 0.dp,
        ) {
            FlashlightGlyph(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(27.dp),
                color = blendColor(Color(0xFF9BA6B1), Color(0xFF302913), visualIntensity),
                text = text,
            )
        }
    }
}

@Composable
private fun TurnOnPanel(
    state: LinternaState,
    text: LinternaText,
    isDemo: Boolean,
    onTurnOn: () -> Unit,
    onResetDemoPremium: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text[TextKey.READY_TO_LIGHT],
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = text[TextKey.MAX_INTENSITY_HELP],
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
        )
        state.notice?.let { NoticeText(it) }
        if (state.errorTarget == ErrorTarget.TURN_ON) state.error?.let { ActionError(it) }
        Button(
            onClick = onTurnOn,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text[TextKey.TURN_ON], fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        if (isDemo && state.isPremiumOwned) {
            TextButton(
                onClick = onResetDemoPremium,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = text[TextKey.RESET_PLEBEIAN],
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
            Text(
                text = text[TextKey.RESET_HELP],
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OffOptions(
    state: LinternaState,
    text: LinternaText,
    onPremium: () -> Unit,
    onNormalOff: () -> Unit,
    onDismissOffer: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when {
                state.isPremiumCelebrating -> text[TextKey.PREMIUM_OFF_PROGRESS]
                state.isTorchOn -> text[TextKey.TORCH_ON_MAX]
                else -> text[TextKey.TORCH_ALREADY_OFF]
            },
            color = if (state.isTorchOn) Color(0xFFF3D27A) else MaterialTheme.colorScheme.onBackground,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = when {
                state.isPremiumCelebrating -> text[TextKey.CELEBRATION_HELP]
                state.isTorchOn -> text[TextKey.CHOOSE_ELEGANCE]
                else -> text[TextKey.COMPLETE_OR_RETURN]
            },
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        if (state.errorTarget == ErrorTarget.PREMIUM) state.error?.let { ActionError(it) }
        PremiumButton(
            owned = state.isPremiumOwned,
            text = text,
            enabled = !state.isPremiumCelebrating,
            onClick = onPremium,
        )
        if (state.isTorchOn) {
            if (state.errorTarget == ErrorTarget.NORMAL) state.error?.let { ActionError(it) }
            TextButton(
                onClick = onNormalOff,
                enabled = !state.isPremiumCelebrating,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Text(
                    text = text[TextKey.NORMAL_PLEBEIAN_OFF],
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
        } else {
            TextButton(onClick = onDismissOffer, modifier = Modifier.padding(top = 6.dp)) {
                Text(text[TextKey.BACK], color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PremiumButton(owned: Boolean, enabled: Boolean, text: LinternaText, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = text[TextKey.PREMIUM_OFF]
                if (!enabled) disabled()
            },
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (enabled) {
                        PremiumGradient
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF665B6D), Color(0xFF76636C)))
                    },
                )
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = text[TextKey.PREMIUM_OFF],
                    color = Color(0xFF211524),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 0.6.sp,
                )
                Text(
                    text = if (owned) text[TextKey.ALREADY_YOURS] else text[TextKey.FIVE_STAR_DARKNESS],
                    color = Color(0xCC211524),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(text = "✦", color = Color(0xFF211524), fontSize = 30.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun PremiumFireworks(progress: Float, text: LinternaText, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.semantics { contentDescription = text[TextKey.FIREWORKS_A11Y] },
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCelebrationConfetti(progress)
            drawMarqueeLights(progress)
            drawFirework(progress, 0.00f, 0.16f, 0.18f, FireworkColors[0])
            drawFirework(progress, 0.06f, 0.83f, 0.16f, FireworkColors[1])
            drawFirework(progress, 0.14f, 0.50f, 0.28f, FireworkColors[2])
            drawFirework(progress, 0.22f, 0.24f, 0.40f, FireworkColors[3])
            drawFirework(progress, 0.30f, 0.77f, 0.38f, FireworkColors[4])
            drawFirework(progress, 0.38f, 0.10f, 0.56f, FireworkColors[5])
            drawFirework(progress, 0.44f, 0.91f, 0.58f, FireworkColors[0])
            drawFirework(progress, 0.50f, 0.40f, 0.60f, FireworkColors[1])
            drawFirework(progress, 0.56f, 0.68f, 0.64f, FireworkColors[2])
            drawFirework(progress, 0.64f, 0.18f, 0.76f, FireworkColors[4])
            drawFirework(progress, 0.70f, 0.82f, 0.78f, FireworkColors[3])
        }
        PremiumSlotMachine(
            progress = progress,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 30.dp)
                .clearAndSetSemantics { },
        )
    }
}

@Composable
private fun PremiumSlotMachine(progress: Float, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.widthIn(min = 220.dp, max = 290.dp),
        color = Color(0xF221172A),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 18.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "✦  7 · 7 · 7  ✦",
                color = Color(0xFFFFD86A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
            )
            Row(
                modifier = Modifier.padding(top = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(3) { reelIndex ->
                    Surface(
                        modifier = Modifier.size(width = 58.dp, height = 64.dp),
                        color = Color(0xFFF8EEDA),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = reelSymbolAt(progress, reelIndex),
                                color = if (progress >= 0.78f) Color(0xFFE02C54) else Color(0xFF2A1C31),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun reelSymbolAt(progress: Float, reelIndex: Int): String {
    if (progress >= 0.78f) return "7"
    val frame = floor(progress.coerceIn(0f, 1f) * 8f).toInt()
    return ReelSymbols[(frame + reelIndex * 2) % ReelSymbols.size]
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCelebrationConfetti(progress: Float) {
    repeat(56) { index ->
        val lane = ((index * 47) % 101) / 100f
        val offset = ((index * 29) % 113) / 113f
        val fall = (offset + progress * (1.10f + (index % 5) * 0.09f)) % 1.12f
        val center = Offset(
            x = size.width * lane + sin((progress * 5f + index) * 1.7f) * 13.dp.toPx(),
            y = size.height * (fall - 0.06f),
        )
        val color = FireworkColors[index % FireworkColors.size]
        if (index % 4 == 0) {
            drawCircle(
                color = Color(0xFFFFD45C).copy(alpha = 0.90f),
                radius = (4 + index % 3).dp.toPx(),
                center = center,
            )
            drawCircle(
                color = Color(0xFFB97916).copy(alpha = 0.85f),
                radius = (2 + index % 2).dp.toPx(),
                center = center,
            )
        } else {
            rotate(degrees = progress * 540f + index * 31f, pivot = center) {
                drawRoundRect(
                    color = color.copy(alpha = 0.88f),
                    topLeft = Offset(center.x - 3.dp.toPx(), center.y - 6.dp.toPx()),
                    size = Size(6.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx()),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMarqueeLights(progress: Float) {
    val inset = 13.dp.toPx()
    val bulbRadius = 4.5.dp.toPx()
    val horizontalCount = 11
    val verticalCount = 18
    var lightIndex = 0

    fun bulb(center: Offset) {
        val wave = (sin(progress * (PI * 8.0) + lightIndex * 0.82) + 1.0).toFloat() / 2f
        val color = FireworkColors[lightIndex % FireworkColors.size]
        drawCircle(color.copy(alpha = 0.16f + wave * 0.28f), bulbRadius * 2.2f, center)
        drawCircle(color.copy(alpha = 0.38f + wave * 0.62f), bulbRadius, center)
        lightIndex += 1
    }

    repeat(horizontalCount) { index ->
        val x = inset + (size.width - inset * 2f) * index / (horizontalCount - 1)
        bulb(Offset(x, inset))
        bulb(Offset(x, size.height - inset))
    }
    repeat(verticalCount - 2) { index ->
        val y = inset + (size.height - inset * 2f) * (index + 1) / (verticalCount - 1)
        bulb(Offset(inset, y))
        bulb(Offset(size.width - inset, y))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFirework(
    progress: Float,
    delay: Float,
    centerX: Float,
    centerY: Float,
    color: Color,
) {
    val phase = ((progress - delay) / 0.30f).coerceIn(0f, 1f)
    if (phase <= 0f || phase >= 1f) return

    val center = Offset(size.width * centerX, size.height * centerY)
    val radius = size.minDimension * (0.04f + 0.18f * FastOutSlowInEasing.transform(phase))
    val alpha = (1f - phase) * 0.95f
    repeat(20) { index ->
        val angle = (2.0 * PI * index / 20.0).toFloat()
        val directionX = cos(angle)
        val directionY = sin(angle)
        val start = Offset(
            center.x + directionX * radius * 0.32f,
            center.y + directionY * radius * 0.32f,
        )
        val end = Offset(
            center.x + directionX * radius,
            center.y + directionY * radius,
        )
        drawLine(
            color = color.copy(alpha = alpha),
            start = start,
            end = end,
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.8f),
            radius = 1.7.dp.toPx(),
            center = end,
        )
    }
}

private fun premiumGlowAt(progress: Float): Float {
    val clamped = progress.coerceIn(0f, 1f)
    return when {
        clamped < 0.14f -> interpolate(1f, 0.35f, clamped / 0.14f)
        clamped < 0.31f -> interpolate(0.35f, 1f, (clamped - 0.14f) / 0.17f)
        clamped < 0.45f -> interpolate(1f, 0.22f, (clamped - 0.31f) / 0.14f)
        clamped < 0.62f -> interpolate(0.22f, 1f, (clamped - 0.45f) / 0.17f)
        clamped < 0.72f -> interpolate(1f, 0.68f, (clamped - 0.62f) / 0.10f)
        clamped < 0.82f -> interpolate(0.68f, 0.38f, (clamped - 0.72f) / 0.10f)
        clamped < 0.92f -> interpolate(0.38f, 0.16f, (clamped - 0.82f) / 0.10f)
        else -> interpolate(0.16f, 0f, (clamped - 0.92f) / 0.08f)
    }
}

private fun interpolate(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction.coerceIn(0f, 1f)

private fun blendColor(start: Color, end: Color, fraction: Float): Color {
    val amount = fraction.coerceIn(0f, 1f)
    return Color(
        red = interpolate(start.red, end.red, amount),
        green = interpolate(start.green, end.green, amount),
        blue = interpolate(start.blue, end.blue, amount),
        alpha = interpolate(start.alpha, end.alpha, amount),
    )
}

@Composable
private fun NoticeText(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Color(0xFF1C2823), RoundedCornerShape(12.dp))
            .padding(12.dp),
        color = Color(0xFFBFE7D2),
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ActionError(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(Color(0xFF382025), RoundedCornerShape(12.dp))
            .padding(11.dp),
        color = MaterialTheme.colorScheme.error,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PremiumPurchaseDialog(
    priceLabel: String?,
    text: LinternaText,
    isDemo: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text[TextKey.PREMIUM_OFF]) },
        text = {
            Column {
                Text(
                    text = if (isDemo) text[TextKey.LOCAL_TEST] else priceLabel ?: text[TextKey.PRICE_GOOGLE_PLAY],
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = text[TextKey.ONE_TIME_NO_SUBSCRIPTION],
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (isDemo) {
                        text[TextKey.DEMO_PURCHASE_HELP]
                    } else {
                        text[TextKey.PLAY_PURCHASE_HELP]
                    },
                    modifier = Modifier.padding(top = 16.dp),
                )
                errorMessage?.let {
                    Spacer(Modifier.height(16.dp))
                    ActionError(it)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(if (isDemo) text[TextKey.SIMULATE_PURCHASE] else text[TextKey.CONTINUE_PLAY])
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text[TextKey.NOT_NOW])
            }
        },
    )
}

@Composable
private fun FlashlightGlyph(modifier: Modifier = Modifier, color: Color, text: LinternaText) {
    Canvas(
        modifier = modifier.semantics { contentDescription = text[TextKey.FLASHLIGHT_ICON] },
    ) {
        rotate(-28f) {
            val width = size.width
            val height = size.height
            val path = Path().apply {
                moveTo(width * 0.24f, height * 0.08f)
                lineTo(width * 0.76f, height * 0.08f)
                lineTo(width * 0.84f, height * 0.29f)
                lineTo(width * 0.66f, height * 0.42f)
                lineTo(width * 0.66f, height * 0.88f)
                lineTo(width * 0.34f, height * 0.88f)
                lineTo(width * 0.34f, height * 0.42f)
                lineTo(width * 0.16f, height * 0.29f)
                close()
            }
            drawPath(path, color)
            drawLine(
                color = color.copy(alpha = 0.55f),
                start = Offset(width * 0.38f, height * 0.56f),
                end = Offset(width * 0.62f, height * 0.56f),
                strokeWidth = width * 0.08f,
                cap = StrokeCap.Round,
            )
        }
    }
}
