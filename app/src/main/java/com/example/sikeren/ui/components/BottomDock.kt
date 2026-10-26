package com.example.sikeren.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sikeren.theme.*

/**
 * Pure Minimalist Floating Bottom Dock
 * Zero layout-shifting, NO jarring text expansion, fixed harmonious geometry.
 * Transitions are buttery smooth (soft crossfade + color tween) across all devices.
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
            shape = RoundedCornerShape(32.dp),
            color = CardBg,
            shadowElevation = 8.dp,
            modifier = Modifier
                .height(58.dp)
                .border(1.dp, BorderLight, RoundedCornerShape(32.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab 0: Home
                IconDockItem(
                    icon = if (currentTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Beranda",
                    isSelected = currentTab == 0,
                    onClick = { onTabSelected(0) }
                )

                // Tab 1: Kegiatan
                IconDockItem(
                    icon = if (currentTab == 1) Icons.AutoMirrored.Filled.EventNote else Icons.AutoMirrored.Outlined.EventNote,
                    contentDescription = "Kegiatan",
                    isSelected = currentTab == 1,
                    onClick = { onTabSelected(1) }
                )

                // Tab 2: QR Scanner
                IconDockItem(
                    icon = if (currentTab == 2) Icons.Default.QrCodeScanner else Icons.Outlined.QrCodeScanner,
                    contentDescription = "Scan QR",
                    isSelected = currentTab == 2,
                    onClick = { onTabSelected(2) }
                )

                // Tab 3: Profil
                IconDockItem(
                    icon = if (currentTab == 3) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = "Profil",
                    isSelected = currentTab == 3,
                    onClick = { onTabSelected(3) }
                )
            }
        }
    }
}

@Composable
private fun IconDockItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Soft, smooth color transition (zero jerky motion, zero layout jump)
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) BpsNavy else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "dockBg"
    )
    val animatedTint by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(durationMillis = 220),
        label = "dockTint"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(animatedBg)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 22.dp)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = animatedTint,
            modifier = Modifier.size(22.dp)
        )
    }
}
