package com.v2ray.ang.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.handler.TrafficMonitor
import com.v2ray.ang.ui.compose.CyberGreen
import com.v2ray.ang.ui.compose.CyberLime
import com.v2ray.ang.ui.compose.CyberTeal
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Slowly drifting neural-network dots and lines behind the main screen. */
@Composable
fun CyberBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bg")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing)),
        label = "t"
    )
    val seeds = remember { List(22) { Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()) } }
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val turn = t * 6.2831855f
        val pts = seeds.mapIndexed { i, s ->
            val a = turn + s.third * 6.28f
            Offset(
                (s.first + 0.06f * sin(a + i)).coerceIn(0f, 1f) * w,
                (s.second + 0.06f * cos(a * 0.8f + i)).coerceIn(0f, 1f) * h
            )
        }
        for (i in pts.indices) {
            for (j in i + 1 until pts.size) {
                val d = (pts[i] - pts[j]).getDistance()
                if (d < 260f) {
                    drawLine(
                        color = CyberGreen.copy(alpha = (1f - d / 260f) * 0.22f),
                        start = pts[i],
                        end = pts[j],
                        strokeWidth = 2f
                    )
                }
            }
        }
        pts.forEach { drawCircle(color = CyberLime.copy(alpha = 0.5f), radius = 3f, center = it) }
    }
}

private fun fmtBytes(b: Double, suffix: String = ""): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var v = b
    var i = 0
    while (v >= 1024.0 && i < units.lastIndex) {
        v /= 1024.0
        i++
    }
    val num = if (i == 0) "%.0f".format(v) else "%.1f".format(v)
    return num + " " + units[i] + suffix
}

@Composable
private fun SpeedCell(label: String, speed: Double, total: Long, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = fmtBytes(speed, "/s"),
            fontSize = 19.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "Total " + fmtBytes(total.toDouble()),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SpeedBox(isRunning: Boolean, up: Double, down: Double, upTotal: Long, downTotal: Long, seconds: Long) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f), shape)
            .border(1.dp, Brush.linearGradient(listOf(CyberGreen, CyberTeal)), shape)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpeedCell(
            "\u2191 UPLOAD",
            if (isRunning) up else 0.0,
            if (isRunning) upTotal else 0L,
            MaterialTheme.colorScheme.primary,
            Modifier.weight(1f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TIME",
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%02d:%02d:%02d".format(seconds / 3600, seconds % 3600 / 60, seconds % 60),
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        SpeedCell(
            "\u2193 DOWNLOAD",
            if (isRunning) down else 0.0,
            if (isRunning) downTotal else 0L,
            MaterialTheme.colorScheme.secondary,
            Modifier.weight(1f)
        )
    }
}

/** Big glossy connect orb with pulsing glow and a spinning arc while connected. */
@Composable
fun ConnectOrb(isRunning: Boolean, onClick: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "orb")
    val pulse by inf.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val rot by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "rot"
    )
    val base = if (isRunning) CyberGreen else Color(0xFF3A6A52)
    Box(
        modifier = Modifier
            .size(150.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = if (isRunning) "Disconnect" else "Connect",
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = center
            val r = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(base.copy(alpha = if (isRunning) 0.45f else 0.18f), Color.Transparent),
                    center = c,
                    radius = r
                ),
                radius = r * (if (isRunning) pulse + 0.05f else 0.95f),
                center = c
            )
            val sr = r * 0.62f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEFFFF6), base, Color(0xFF04281A)),
                    center = Offset(c.x - sr * 0.35f, c.y - sr * 0.4f),
                    radius = sr * 1.7f
                ),
                radius = sr,
                center = c
            )
            drawCircle(
                color = base.copy(alpha = 0.85f),
                radius = sr * 1.12f,
                center = c,
                style = Stroke(width = 3.dp.toPx())
            )
            if (isRunning) {
                val rr = sr * 1.3f
                drawArc(
                    color = CyberLime,
                    startAngle = rot,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(c.x - rr, c.y - rr),
                    size = Size(rr * 2f, rr * 2f),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = "GI",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = if (isRunning) Color(0xFF021208) else Color(0xFFDCFCE9),
            style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(3f, 4f), 6f))
        )
    }
}

/** Speed box + connect orb shown above the server list. */
@Composable
fun CyberHeader(isRunning: Boolean, onToggle: () -> Unit) {
    val snap by TrafficMonitor.state.collectAsStateWithLifecycle()
    var seconds by remember { mutableStateOf(0L) }
    LaunchedEffect(isRunning) {
        TrafficMonitor.reset()
        seconds = 0L
        if (isRunning) {
            while (true) {
                delay(1000)
                seconds += 1
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpeedBox(isRunning, snap.upSpeed, snap.downSpeed, snap.upTotal, snap.downTotal, seconds)
        Spacer(modifier = Modifier.height(6.dp))
        ConnectOrb(isRunning = isRunning, onClick = onToggle)
        Text(
            text = if (isRunning) "CONNECTED" else "TAP TO CONNECT",
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isRunning) CyberGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
