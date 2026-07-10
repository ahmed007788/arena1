package com.arena.ai.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.arena.ai.data.remote.AgentLeaderboardEntry
import com.arena.ai.domain.model.*
import com.arena.ai.presentation.theme.*

// ═══════════════════════════════════════════════════════════════════════════════
// ARENA DESIGN SYSTEM COMPONENTS
// Based on agent-skills frontend-ui-engineering methodology
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────
// BUTTONS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ArenaAccentPrimary,
            contentColor = ArenaBackground,
            disabledContainerColor = ArenaSurfaceElevated,
            disabledContentColor = ArenaTextTertiary
        )
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ArenaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaAccentPrimary),
        border = BorderStroke(1.dp, ArenaAccentPrimary)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CARDS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ArenaBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun ArenaElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceTertiary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INPUT FIELDS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, color = ArenaTextTertiary) } },
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = ArenaTextSecondary) } },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ArenaAccentPrimary,
            unfocusedBorderColor = ArenaBorder,
            focusedLabelColor = ArenaAccentPrimary,
            unfocusedLabelColor = ArenaTextSecondary,
            focusedTextColor = ArenaTextPrimary,
            unfocusedTextColor = ArenaTextPrimary,
            cursorColor = ArenaAccentPrimary,
            focusedContainerColor = ArenaSurfaceElevated,
            unfocusedContainerColor = ArenaSurfaceElevated
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun ArenaSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "اكتب للبحث...",
    modifier: Modifier = Modifier
) {
    Surface(
        color = ArenaSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                null,
                tint = ArenaTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = ArenaTextPrimary,
                    fontSize = 14.sp
                ),
                placeholder = {
                    Text(placeholder, color = ArenaTextTertiary)
                },
                cursorBrush = SolidColor(ArenaAccentPrimary),
                singleLine = true
            )
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        "مسح",
                        tint = ArenaTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHIPS & TAGS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = leadingIcon?.let { { Icon(it, null, modifier = Modifier.size(16.dp)) } },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ArenaAccentPrimary.copy(alpha = 0.2f),
            selectedLabelColor = ArenaAccentPrimary,
            containerColor = ArenaSurfaceElevated,
            labelColor = ArenaTextSecondary
        ),
        border = if (selected) BorderStroke(1.dp, ArenaAccentPrimary) else null
    )
}

@Composable
fun ArenaStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ArenaBackground,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                color = ArenaAccentPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = ArenaTextSecondary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEADERBOARD COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaLeaderboardItem(
    entry: AgentLeaderboardEntry,
    isTopThree: Boolean,
    modifier: Modifier = Modifier
) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)  // Gold
        2 -> Color(0xFFC0C0C0)  // Silver
        3 -> Color(0xFFCD7F32)  // Bronze
        else -> ArenaTextSecondary
    }
    
    val rankEmoji = when (entry.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> null
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isTopThree) ArenaSurfaceTertiary else ArenaSurfaceElevated
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isTopThree) BorderStroke(1.dp, rankColor.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank indicator
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rankEmoji != null) {
                    Text(rankEmoji, fontSize = 24.sp)
                } else {
                    Text(
                        "#${entry.rank}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = rankColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Model info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.modelName,
                    style = MaterialTheme.typography.titleSmall,
                    color = ArenaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    entry.lab,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaTextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Metrics row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ArenaStatChip("نجاح", String.format("%.0f%%", entry.confirmedSuccess))
                    ArenaStatChip("إشادة", String.format("%.0f%%", entry.praiseVsComplaint))
                    ArenaStatChip("توجيه", String.format("%.0f%%", entry.steerability))
                }
            }
            
            // Score
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    String.format("%.1f%%", entry.netImprovement),
                    style = MaterialTheme.typography.headlineSmall,
                    color = ArenaAccentPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${formatSessionCount(entry.sessions)} جلسة",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaTextTertiary
                )
            }
        }
    }
}

private fun formatSessionCount(count: Int): String {
    return when {
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MODEL SELECTOR COMPONENT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaModelSelector(
    models: List<AIModel>,
    selectedModelId: String,
    onModelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedModel = models.find { it.id == selectedModelId }
    
    Box(modifier = modifier) {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountCircle,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = ArenaAccentPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedModel?.shortName ?: "اختر نموذج")
                }
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null)
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ArenaAccentPrimary.copy(alpha = 0.15f),
                selectedLabelColor = ArenaAccentPrimary
            )
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ArenaSurfaceElevated)
        ) {
            models.forEach { model ->
                val isSelected = model.id == selectedModelId
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ArenaAccentPrimary,
                                    unselectedColor = ArenaTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    model.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ArenaTextPrimary
                                )
                                Text(
                                    model.provider.uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArenaTextSecondary
                                )
                            }
                        }
                    },
                    onClick = {
                        onModelSelected(model.id)
                        expanded = false
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = ArenaAccentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LOADING & EMPTY STATES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "جارٍ التحميل..."
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = ArenaAccentPrimary,
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaTextSecondary
        )
    }
}

@Composable
fun ArenaEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(64.dp),
            tint = ArenaTextTertiary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = ArenaTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaTextSecondary
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(16.dp))
            action()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATED COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaShimmerCard(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        ArenaSurfaceElevated,
        ArenaSurfaceTertiary,
        ArenaSurfaceElevated
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = shimmerColors,
                        start = Offset(translateAnim - 200f, 0f),
                        end = Offset(translateAnim, 0f)
                    )
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATUS INDICATORS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaStatusIndicator(
    status: String,
    color: Color = ArenaAccentPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            status,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun ArenaStreamingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "dot")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(ArenaAccentPrimary.copy(alpha = alpha + index * 0.2f))
            )
            if (index < 2) Spacer(modifier = Modifier.width(4.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "جارٍ الكتابة",
            style = MaterialTheme.typography.labelSmall,
            color = ArenaAccentPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PROGRESS BARS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = ArenaAccentPrimary,
    backgroundColor: Color = ArenaSurfaceTertiary
) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BADGES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArenaBadge(
    text: String,
    color: Color = ArenaAccentPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ArenaProviderBadge(
    provider: String,
    modifier: Modifier = Modifier
) {
    val color = when (provider.lowercase()) {
        "anthropic" -> Color(0xFFFF6B6B)
        "openai" -> Color(0xFF10B981)
        "google" -> Color(0xFF4285F4)
        "deepseek" -> Color(0xFF6366F1)
        else -> ArenaAccentSecondary
    }
    
    ArenaBadge(text = provider, color = color, modifier = modifier)
}