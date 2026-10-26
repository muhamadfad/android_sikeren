package com.example.sikeren.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sikeren.theme.*

/**
 * World-Class Floating Pill Navigation Dock
 * Features dynamic pill expansion with spring physics,
 * tactile ripple response, and frosted glass elevation.
 */
@Composable
fun BottomFloatingDock(
    currentTab: Int, // 0: Home, 1: Kegiatan, 2: QR Absensi, 3: Profil
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = CardBg,
            shadowElevation = 12.dp,
            modifier = Modifier
                .height(64.dp)
                .border(1.dp, BorderLight, RoundedCornerShape(36.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab 0: Home
                PillDockItem(
                    icon = if (currentTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                    label = "Beranda",
                    isSelected = currentTab == 0,
                    onClick = { onTabSelected(0) }
                )

                // Tab 1: Kegiatan
                PillDockItem(
                    icon = if (currentTab == 1) Icons.AutoMirrored.Filled.EventNote else Icons.AutoMirrored.Outlined.EventNote,
                    label = "Kegiatan",
                    isSelected = currentTab == 1,
                    onClick = { onTabSelected(1) }
                )

                // Tab 2: QR Scanner
                PillDockItem(
                    icon = if (currentTab == 2) Icons.Default.QrCodeScanner else Icons.Outlined.QrCodeScanner,
                    label = "Scan QR",
                    isSelected = currentTab == 2,
                    onClick = { onTabSelected(2) }
                )

                // Tab 3: Profil
                PillDockItem(
                    icon = if (currentTab == 3) Icons.Default.Person else Icons.Outlined.Person,
                    label = "Profil",
                    isSelected = currentTab == 3,
                    onClick = { onTabSelected(3) }
                )
            }
        }
    }
}

@Composable
private fun PillDockItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) BpsNavy else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pillDockBg"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pillDockColor"
    )

    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(animatedBg)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 24.dp)
            ) { onClick() }
            .padding(horizontal = if (isSelected) 16.dp else 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedContentColor,
                modifier = Modifier.size(22.dp)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedContentColor
                    )
                }
            }
        }
    }
}
