package id.my.natsir

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource

import echo93project.composeapp.generated.resources.Res
import echo93project.composeapp.generated.resources.logo

@Composable
fun LoginScreen(
    isLoading: Boolean,       // <-- Menerima status loading dari main.kt
    onLoginClick: () -> Unit  // <-- Memicu aksi klik yang dikontrol oleh main.kt
) {
    // State internal 'isLoggingIn' & 'coroutineScope' dihapus karena sudah diatur di main.kt

    val primaryBlue = Color(0xFF1B4CC3)
    val brightCyan = Color(0xFF60EFFF)
    val darkBlue = Color(0xFF0B1E4F)

    val backgroundBrush = Brush.verticalGradient(colors = listOf(primaryBlue, darkBlue))

    Box(
        modifier = Modifier.fillMaxSize().background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        // Glassmorphism Card
        Card(
            modifier = Modifier.width(420.dp).clip(RoundedCornerShape(32.dp)),
            backgroundColor = Color.White.copy(alpha = 0.1f),
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- LOGO SVG ---
                Icon(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "Logo Hawari POS",
                    tint = brightCyan,
                    modifier = Modifier.height(60.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("WELCOME", color = brightCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("HDM POS SYSTEM", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

                Spacer(modifier = Modifier.height(12.dp))
                Text("Silakan login menggunakan akun Google Anda", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onLoginClick, // <-- Menjalankan fungsi login terpusat
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = brightCyan, contentColor = primaryBlue),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading // <-- Tombol otomatis aktif lagi jika isLoading == false dari main.kt
                ) {
                    Text(
                        text = if (isLoading) "PROSES AUTENTIKASI..." else "LOGIN DENGAN GMAIL",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}