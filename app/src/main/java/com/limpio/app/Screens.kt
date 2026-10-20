package com.limpio.app

import android.text.format.Formatter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(28.dp)

// ───────────────────────── Pantalla principal de revisión ─────────────────────────

@Composable
fun ReviewScreen(vm: LimpioViewModel, onDelete: () -> Unit) {
    val context = LocalContext.current
    val current = vm.current ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Limpio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${vm.index + 1} de ${vm.photos.size}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { vm.index.toFloat() / vm.photos.size.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )

        // Zona de tarjetas (la siguiente foto se ve detrás)
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            vm.next?.let { next ->
                key(next.id) {
                    PhotoCard(
                        photo = next,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = 0.94f
                            scaleY = 0.94f
                        },
                    )
                }
            }
            key(current.id) {
                SwipeCard(
                    photo = current,
                    modifier = Modifier.fillMaxSize(),
                    onSwiped = { delete -> vm.decide(delete) },
                )
            }
        }

        Text(
            text = "← Desliza para eliminar      Desliza para conservar →",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Botones (alternativa al deslizamiento)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = { vm.decide(true) },
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = DeleteRed),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Eliminar", tint = Color.White)
            }
            Spacer(Modifier.width(20.dp))
            FilledTonalIconButton(
                onClick = { vm.undo() },
                enabled = vm.canUndo,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Deshacer")
            }
            Spacer(Modifier.width(20.dp))
            FilledIconButton(
                onClick = { vm.decide(false) },
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = KeepGreen),
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Conservar", tint = Color.White)
            }
        }

        // Botón para borrar las fotos marcadas
        if (vm.pending.isNotEmpty()) {
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 14.dp).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeleteRed, contentColor = Color.White),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Eliminar ${vm.pending.size} (${Formatter.formatShortFileSize(context, vm.pendingBytes)})",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Spacer(Modifier.height(14.dp))
        }
    }
}

// ───────────────────────── Tarjeta con gesto de deslizar ─────────────────────────

@Composable
private fun PhotoCard(photo: Photo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SwipeCard(
    photo: Photo,
    modifier: Modifier = Modifier,
    onSwiped: (delete: Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }

    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val threshold = screenWidthPx * 0.30f
    val progress = (offsetX.value / threshold).coerceIn(-1f, 1f)

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .graphicsLayer { rotationZ = offsetX.value / screenWidthPx * 14f }
            .pointerInput(photo.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > threshold -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    offsetX.animateTo(screenWidthPx * 1.4f, tween(180))
                                    onSwiped(false)
                                }

                                offsetX.value < -threshold -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    offsetX.animateTo(-screenWidthPx * 1.4f, tween(180))
                                    onSwiped(true)
                                }

                                else -> offsetX.animateTo(0f, spring())
                            }
                        }
                    },
                    onDragCancel = { scope.launch { offsetX.animateTo(0f, spring()) } },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    },
                )
            },
    ) {
        PhotoCard(photo = photo, modifier = Modifier.fillMaxSize())

        if (progress != 0f) {
            val keep = progress > 0f
            val tint = if (keep) KeepGreen else DeleteRed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardShape)
                    .background(tint.copy(alpha = abs(progress) * 0.35f))
            )
            Text(
                text = if (keep) "CONSERVAR" else "ELIMINAR",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                modifier = Modifier
                    .align(if (keep) Alignment.TopStart else Alignment.TopEnd)
                    .padding(28.dp)
                    .graphicsLayer {
                        alpha = abs(progress)
                        rotationZ = if (keep) -12f else 12f
                    }
                    .background(tint, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}

// ───────────────────────── Otras pantallas ─────────────────────────

@Composable
fun FinishedScreen(vm: LimpioViewModel, onDelete: () -> Unit, onRestart: () -> Unit) {
    val context = LocalContext.current
    val hasPending = vm.pending.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (hasPending) "¡Revisión terminada!" else "¡Todo limpio!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Conservadas: ${vm.keptTotal}\nEliminadas: ${vm.deletedTotal}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))

        if (hasPending) {
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeleteRed, contentColor = Color.White),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Eliminar ${vm.pending.size} (${Formatter.formatShortFileSize(context, vm.pendingBytes)})",
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { vm.undo() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("Deshacer la última") }
        } else {
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Text("Volver a empezar")
            }
        }
    }
}

@Composable
fun PermissionScreen(onGrant: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Limpio",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Para mostrarte tus fotos necesito acceso a tu galería.\n" +
                "Nada sale de tu teléfono y no se borra nada sin tu confirmación.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onGrant, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("Dar acceso a mis fotos")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Abrir ajustes")
        }
    }
}

@Composable
fun MessageScreen(title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, textAlign = TextAlign.Center)
    }
}
