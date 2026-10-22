package com.example.sikeren.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sikeren.theme.*

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
        // Floating Pill Dock Container matching mockup
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = GlassWhite,
            shadowElevation = 8.dp,
            modifier = Modifier.height(64.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab 0: Home
                DockItem(
                    icon = if (currentTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                    isSelected = currentTab == 0,
                    onClick = { onTabSelected(0) }
                )

                // Tab 1: Kegiatan / Calendar
                DockItem(
                    icon = if (currentTab == 1) Icons.Default.EventNote else Icons.Outlined.EventNote,
                    isSelected = currentTab == 1,
                    onClick = { onTabSelected(1) }
                )

                // Tab 2: QR Scanner
                DockItem(
                    icon = if (currentTab == 2) Icons.Default.QrCodeScanner else Icons.Outlined.QrCodeScanner,
                    isSelected = currentTab == 2,
                    onClick = { onTabSelected(2) }
                )

                // Tab 3: Profil
                DockItem(
                    icon = if (currentTab == 3) Icons.Default.Person else Icons.Outlined.Person,
                    isSelected = currentTab == 3,
                    onClick = { onTabSelected(3) }
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isSelected) OrangePrimary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}
