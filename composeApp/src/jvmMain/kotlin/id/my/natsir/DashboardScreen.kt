package id.my.natsir

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo93project.composeapp.generated.resources.Res
import echo93project.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(session: StoreSession, onLogout: () -> Unit) {
    var currentScreen by remember { mutableStateOf("Home") }

    // Warna Sesuai Tema Premium Blue
    val primaryBlue = Color(0xFF1B4CC3)
    val brightCyan = Color(0xFF60EFFF)
    val glassWhite = Color.White.copy(alpha = 0.12f)

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF2C0B4D), primaryBlue, Color(0xFF880E4F))
    )

    if (currentScreen == "Home") {
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- 1. HEADER ATAS ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Render Logo SVG langsung tanpa Try-Catch aman karena resource dijamin ada
                    Icon(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "Logo",
                        tint = brightCyan,
                        modifier = Modifier.height(45.dp)
                    )

                    // Status Icons (Kanan Atas)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        StatusIndicator("Internet", Icons.Default.Wifi, true, brightCyan)
                        StatusIndicator("Server", Icons.Default.Storage, true, brightCyan)
                        StatusIndicator("Toko", Icons.Default.Store, true, brightCyan)
                        StatusIndicator(session.userEmail.substringBefore("@"), Icons.Default.Person, true, brightCyan)
                        IconButton(onClick = onLogout) { Icon(Icons.Default.PowerSettingsNew, "Exit", tint = Color.White) }
                    }
                }

                // --- 2. KONTEN UTAMA (SPLIT ROW) ---
                Row(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 40.dp)) {

                    // --- PANEL KIRI (Info & Notifikasi) ---
                    Column(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                        val now = LocalDateTime.now()
                        Text(now.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), color = Color.White, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(now.format(DateTimeFormatter.ofPattern("HH:mm")), color = Color.White, fontSize = 80.sp, fontWeight = FontWeight.ExtraLight)
                            Spacer(Modifier.width(20.dp))
                            Column {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(32.dp))
                                Text("Cerah & Berawan", color = Color.White, fontSize = 14.sp)
                            }
                        }

                        Spacer(Modifier.height(30.dp))

                        // Panel Notifikasi Pesanan Online
                        Text("NOTIFIKASI PESANAN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NotificationItem("Gojek", "Ada Pesanan Baru!", Color(0xFF00AA13))
                            NotificationItem("Grab", "Driver telah sampai", Color(0xFF00B14F))
                            NotificationItem("Shopee", "Pesanan dibatalkan", Color(0xFFEE4D2D))
                        }
                    }

                    // --- PANEL KANAN (GRID MENU 4x2) ---
                    Column(
                        modifier = Modifier.weight(2.5f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Baris 1
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            MenuBox("Meja", Icons.Default.TableBar, brightCyan) { currentScreen = "Meja" }
                            MenuBox("Kasir", Icons.Default.FlashOn, brightCyan) { currentScreen = "Kasir" }
                            MenuBox("Delivery", Icons.Default.DeliveryDining, brightCyan) { currentScreen = "Paket" }
                            MenuBox("Dapur", Icons.Default.Restaurant, brightCyan) { currentScreen = "Mutfak" }
                        }
                        Spacer(Modifier.height(20.dp))
                        // Baris 2
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            MenuBox("Produk", Icons.Default.Category, brightCyan) { currentScreen = "Produk" }
                            MenuBox("Stok", Icons.Default.Inventory, brightCyan) { currentScreen = "Inventaris" }
                            MenuBox("Customer", Icons.Default.People, brightCyan) { currentScreen = "Cariler" }
                            MenuBox("Laporan", Icons.Default.BarChart, brightCyan) { currentScreen = "Raporlar" }
                        }
                    }
                }

                // --- 3. FOOTER BAWAH ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tenant ID: ${session.storeId} | CS: 089677928195", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        Button(
                            onClick = { currentScreen = "Setup" },
                            colors = ButtonDefaults.buttonColors(backgroundColor = glassWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.SettingsApplications, null, tint = brightCyan)
                            Text(" SETUP TOKO", color = Color.White, fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { /* Settings */ },
                            modifier = Modifier.clip(CircleShape).background(glassWhite)
                        ) {
                            Icon(Icons.Default.Settings, null, tint = Color.White)
                        }
                    }
                }
            }
        }
    } else {
        // Tampilan Sub-Menu (Inventaris, dll)
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
            TopAppBar(
                title = { Text(currentScreen, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { currentScreen = "Home" }) { Icon(Icons.Default.ArrowBack, null) } },
                backgroundColor = primaryBlue, contentColor = Color.White, elevation = 0.dp
            )
            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    "Inventaris" -> InventoryScreen(session = session)
                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Modul $currentScreen Sedang Dikembangkan", color = primaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(label: String, icon: ImageVector, isActive: Boolean, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (isActive) color else Color.Gray, modifier = Modifier.size(18.dp))
        Text(label.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NotificationItem(platform: String, msg: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(35.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(platform, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(msg, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}

@Composable
fun MenuBox(title: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(width = 130.dp, height = 130.dp).clip(RoundedCornerShape(25.dp)).clickable { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.1f),
        elevation = 0.dp
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(45.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}