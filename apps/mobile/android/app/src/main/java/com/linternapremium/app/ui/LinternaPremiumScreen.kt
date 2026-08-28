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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import kotlin.math.PI
import kotlin.math.cos
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
)

@Composable
fun LinternaPremiumScreen(
    state: LinternaState,
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
                AppHeader(isPremiumOwned = state.isPremiumOwned)
                Spacer(Modifier.height(28.dp))
                FlashlightHero(isOn = state.isTorchOn, premiumGlow = premiumGlow)
                Spacer(Modifier.height(24.dp))

                AnimatedContent(
                    targetState = state.isTorchOn || state.showPremiumOffer,
                    label = "acciones-linterna",
                ) { showOffOptions ->
                    if (showOffOptions) {
                        OffOptions(
                            state = state,
                            onPremium = onPremium,
                            onNormalOff = onNormalOff,
                            onDismissOffer = onDismissOffer,
                        )
                    } else {
                        TurnOnPanel(
                            state = state,
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
                modifier = Modifier.matchParentSize(),
            )
        }
    }

    if (state.showPurchaseDialog) {
        PremiumPurchaseDialog(
            priceLabel = state.priceLabel,
            isDemo = isDemo,
            errorMessage = state.error.takeIf { state.errorTarget == ErrorTarget.PREMIUM },
            onConfirm = onConfirmPurchase,
            onDismiss = onDismissPurchase,
        )
    }
}

@Composable
private fun AppHeader(isPremiumOwned: Boolean) {
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
                text = if (isPremiumOwned) "PREMIUM ACTIVO" else "EDICION MORTAL",
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
private fun FlashlightHero(isOn: Boolean, premiumGlow: Float?) {
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
            )
        }
    }
}

@Composable
private fun TurnOnPanel(
    state: LinternaState,
    isDemo: Boolean,
    onTurnOn: () -> Unit,
    onResetDemoPremium: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Lista para iluminar",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Usaremos la mayor intensidad que permita tu teléfono.",
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
            Text("Encender linterna", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        if (isDemo && state.isPremiumOwned) {
            TextButton(
                onClick = onResetDemoPremium,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = "Restablecer edición mortal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
            Text(
                text = "Sólo borra el Premium simulado de este dispositivo.",
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
    onPremium: () -> Unit,
    onNormalOff: () -> Unit,
    onDismissOffer: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when {
                state.isPremiumCelebrating -> "Apagado Premium en curso"
                state.isTorchOn -> "Linterna encendida al máximo"
                else -> "La linterna ya está apagada"
            },
            color = if (state.isTorchOn) Color(0xFFF3D27A) else MaterialTheme.colorScheme.onBackground,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = when {
                state.isPremiumCelebrating -> "Fuegos artificiales, brillo ceremonial y oscuridad garantizada."
                state.isTorchOn -> "Elegí con qué nivel de elegancia querés apagarla."
                else -> "Podés completar Premium o volver sin pagar."
            },
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        if (state.errorTarget == ErrorTarget.PREMIUM) state.error?.let { ActionError(it) }
        PremiumButton(
            owned = state.isPremiumOwned,
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
                    text = "Apagado normal, de mortales",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
        } else {
            TextButton(onClick = onDismissOffer, modifier = Modifier.padding(top = 6.dp)) {
                Text("Volver", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PremiumButton(owned: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "Apagado Premium"
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
                    text = "APAGADO PREMIUM",
                    color = Color(0xFF211524),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 0.6.sp,
                )
                Text(
                    text = if (owned) "Ya es tuyo" else "Oscuridad cinco estrellas",
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
private fun PremiumFireworks(progress: Float, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.semantics { contentDescription = "Fuegos artificiales Premium" },
    ) {
        drawFirework(progress, 0.00f, 0.20f, 0.24f, FireworkColors[0])
        drawFirework(progress, 0.16f, 0.78f, 0.20f, FireworkColors[1])
        drawFirework(progress, 0.32f, 0.46f, 0.38f, FireworkColors[2])
        drawFirework(progress, 0.50f, 0.82f, 0.48f, FireworkColors[3])
        drawFirework(progress, 0.66f, 0.24f, 0.53f, FireworkColors[1])
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
    repeat(16) { index ->
        val angle = (2.0 * PI * index / 16.0).toFloat()
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
    priceLabel: String,
    isDemo: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apagado Premium") },
        text = {
            Column {
                Text(
                    text = if (isDemo) "Prueba local" else priceLabel,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Compra única · Sin suscripción",
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (isDemo) {
                        "Esta es una compra simulada: no se cobrará dinero ni se pedirá una tarjeta."
                    } else {
                        "Al continuar, Google Play abrirá el pago oficial para que revises y confirmes la compra."
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
                Text(if (isDemo) "Simular compra Premium" else "Continuar con Google Play")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ahora no")
            }
        },
    )
}

@Composable
private fun FlashlightGlyph(modifier: Modifier = Modifier, color: Color) {
    Canvas(
        modifier = modifier.semantics { contentDescription = "Icono de linterna" },
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
