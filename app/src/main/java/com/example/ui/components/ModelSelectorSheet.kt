package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AIModelOption
import com.example.data.model.AvailableModels
import com.example.ui.theme.RimaCyan
import com.example.ui.theme.RimaFuchsia
import com.example.ui.theme.RimaIndigo
import com.example.ui.theme.RimaIndigoLight
import com.example.ui.theme.RimaViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorSheet(
    selectedModelId: String,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = RimaCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Rima AI Model",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AvailableModels.list.forEach { model ->
                    ModelOptionCard(
                        model = model,
                        isSelected = model.id == selectedModelId,
                        onClick = {
                            onModelSelected(model.id)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelOptionCard(
    model: AIModelOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (model.id) {
        "gemini-3.5-flash" -> Icons.Default.ElectricBolt
        "gemini-3.1-pro-preview" -> Icons.Default.Psychology
        else -> Icons.Default.Image
    }

    val iconGradient = when (model.id) {
        "gemini-3.5-flash" -> listOf(RimaCyan, RimaIndigo)
        "gemini-3.1-pro-preview" -> listOf(RimaIndigo, RimaViolet)
        else -> listOf(RimaViolet, RimaFuchsia)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.horizontalGradient(listOf(RimaIndigo, RimaCyan))
                } else {
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(iconGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RimaIndigo.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = model.badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RimaIndigoLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = model.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(RimaCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
