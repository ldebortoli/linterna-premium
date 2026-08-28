package com.linternapremium.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linternapremium.app.model.ErrorTarget
import com.linternapremium.app.model.LinternaState

private val PremiumGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFF9D6CFF),
        Color(0xFFE26D9E),
        Color(0xFFF3B65F),
    ),
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
) {
    val celebration = remember { Animatable(0f) }
    LaunchedEffect(state.celebrationSequence) {
        if (state.celebrationSequence > 0) {
            celebration.snapTo(0f)
            celebration.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            celebration.animateTo(0f, tween(900))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0D10), Color(0xFF11161C), Color(0xFF090B0E)),
                ),
            )
            .drawBehind {
                if (celebration.value > 0f) {
                    drawCircle(
                        brush = PremiumGradient,
                        radius = size.minDimension * celebration.value,
                        center = Offset(size.width / 2f, size.height * 0.4f),
                        alpha = 0.16f * (1f - celebration.value / 2f),
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
                FlashlightHero(isOn = state.isTorchOn)
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
                        TurnOnPanel(state = state, onTurnOn = onTurnOn)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
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
                text = "Premium",
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
private fun FlashlightHero(isOn: Boolean) {
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
    Box(
        modifier = Modifier
            .size(204.dp)
            .drawBehind {
                if (isOn) {
                    drawCircle(
                        color = Color(0xFFF4D478),
                        radius = size.minDimension * 0.48f * pulse,
                        alpha = 0.10f,
                    )
                    drawCircle(
                        color = Color(0xFFF4D478),
                        radius = size.minDimension * 0.34f * pulse,
                        alpha = 0.18f,
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(126.dp),
            color = if (isOn) Color(0xFFF1D170) else Color(0xFF1D232A),
            shape = CircleShape,
            shadowElevation = if (isOn) 14.dp else 0.dp,
        ) {
            FlashlightGlyph(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(27.dp),
                color = if (isOn) Color(0xFF302913) else Color(0xFF9BA6B1),
            )
        }
    }
}

@Composable
private fun TurnOnPanel(state: LinternaState, onTurnOn: () -> Unit) {
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
            text = if (state.isTorchOn) "Linterna encendida al máximo" else "La linterna ya está apagada",
            color = if (state.isTorchOn) Color(0xFFF3D27A) else MaterialTheme.colorScheme.onBackground,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (state.isTorchOn) "Elegí con qué nivel de elegancia querés apagarla." else "Podés completar Premium o volver sin pagar.",
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        if (state.errorTarget == ErrorTarget.PREMIUM) state.error?.let { ActionError(it) }
        PremiumButton(
            owned = state.isPremiumOwned,
            onClick = onPremium,
        )
        if (state.isTorchOn) {
            if (state.errorTarget == ErrorTarget.NORMAL) state.error?.let { ActionError(it) }
            TextButton(onClick = onNormalOff, modifier = Modifier.padding(top = 6.dp)) {
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
private fun PremiumButton(owned: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "Apagado Premium"
            },
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .background(PremiumGradient)
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
