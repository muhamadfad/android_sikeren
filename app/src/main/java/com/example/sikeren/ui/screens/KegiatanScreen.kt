package com.example.sikeren.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sikeren.model.KegiatanItem
import com.example.sikeren.model.KegiatanStats
import com.example.sikeren.theme.*

@Composable
fun KegiatanScreen(
    stats: KegiatanStats,
    items: List<KegiatanItem>,
    onBack: () -> Unit,
    onSelectFilter: (String) -> Unit,
    onSelectKegiatan: (KegiatanItem) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = items.filter {
        val matchesSearch = searchQuery.isBlank() || 
            it.judul.contains(searchQuery, ignoreCase = true) || 
            it.tempat.contains(searchQuery, ignoreCase = true) ||
            it.agenda.contains(searchQuery, ignoreCase = true)
        
        val matchesFilter = when (selectedFilter) {
            "today" -> it.isToday
            "upcoming" -> it.isUpcoming
            "rapat" -> it.jenis == "Rapat"
            "kegiatan" -> it.jenis == "Kegiatan"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardBg)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Daftar Kegiatan & Rapat",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(1.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Cari kegiatan, rapat, nomor surat..." else searchQuery,
                        color = if (searchQuery.isEmpty()) TextMuted else TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }

            // Filter Tabs Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    KegiatanFilterChip(
                        title = "Semua (${stats.totalAll})",
                        isSelected = selectedFilter == "all",
                        onClick = {
                            selectedFilter = "all"
                            onSelectFilter("all")
                        }
                    )
                }
                item {
                    KegiatanFilterChip(
                        title = "Hari Ini (${stats.totalToday})",
                        isSelected = selectedFilter == "today",
                        onClick = {
                            selectedFilter = "today"
                            onSelectFilter("today")
                        }
                    )
                }
                item {
                    KegiatanFilterChip(
                        title = "Mendatang (${stats.totalUpcoming})",
                        isSelected = selectedFilter == "upcoming",
                        onClick = {
                            selectedFilter = "upcoming"
                            onSelectFilter("upcoming")
                        }
                    )
                }
                item {
                    KegiatanFilterChip(
                        title = "Rapat (${stats.totalRapat})",
                        isSelected = selectedFilter == "rapat",
                        onClick = {
                            selectedFilter = "rapat"
                            onSelectFilter("rapat")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Kegiatan / Agenda List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada kegiatan yang cocok.",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    items(filteredList) { item ->
                        EventCard(item = item, onClick = { onSelectKegiatan(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    item: KegiatanItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.jenis == "Rapat") BpsBlueLight else AccentGreenSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.jenis,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.jenis == "Rapat") BpsNavy else AccentGreen
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.jamDisplay,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.judul,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.tempat,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun KegiatanFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) BpsNavy else CardBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}
