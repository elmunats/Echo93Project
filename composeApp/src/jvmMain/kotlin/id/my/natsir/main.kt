package id.my.natsir

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

// Enum untuk navigasi internal form di dalam kartu login
enum class AuthMode { LOGIN, REGISTER }

// Definisi Palet Warna Premium (Sesuai mockup split-screen)
val PrimaryBlue = Color(0xFF19375F)   // Biru gelap untuk tombol utama & judul
val LightAccentBlue = Color(0xFF327DF3) // Biru terang untuk gradien welcome panel
val MutedTextDark = Color(0xFF2C3E50)   // Warna teks form input

// State Global untuk memantau crash sistem di luar UI Thread utama
private var globalCrashMessage by mutableStateOf<String?>(null)

// 1. Inisialisasi Firebase Admin SDK
fun initFirebase() {
    try {
        val serviceAccount = Thread.currentThread().contextClassLoader
            .getResourceAsStream("serviceAccountKey.json")

        if (serviceAccount == null) {
            println("Gagal: File serviceAccountKey.json tidak ditemukan di folder resources!")
            return
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
            println("Firebase Berhasil Diinisialisasi dari Resource Application!")
        }
    } catch (e: Exception) {
        println("Gagal inisialisasi Firebase: ${e.message}")
    }
}

@Composable
@Preview
fun DesktopApp() {
    val scope = rememberCoroutineScope()

    // Status Sesi & Navigasi
    var currentSession by remember { mutableStateOf<StoreSession?>(null) }
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // State Pembantu Dialog & Loading
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showRegisterSuccessDialog by remember { mutableStateOf(false) }

    // State Inputan Form Register baru sesuai permintaan Akang
    var regStoreName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {

            if (currentSession == null) {
                // =================================================================
                // 1. BACKGROUND UTAMA (MENGGUNAKAN IMAGE)
                // =================================================================
                // Catatan: Pastikan file gambar background sudah ditaruh di folder resources Anda.
                // Jika belum ada, sementara sistem akan menggunakan fallback warna abu-abu netral di bawah ini.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE2E8F0))
                ) {
                    try {
                        // Hilangkan komentar (uncomment) baris di bawah ini jika file gambar latar sudah siap
                        /*
                        Image(
                            painter = androidx.compose.ui.res.painterResource("background_login.jpg"),
                            contentDescription = "Background Latar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        */
                    } catch (e: Exception) { /* Fallback aman jika gambar tidak ditemukan */ }
                }

                // =================================================================
                // 2. WIDGET KONTEN DI TENGAH (SPLIT-SCREEN CARD)
                // =================================================================
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .width(950.dp)
                            .height(580.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                    ) {
                        // --- SISI KIRI: PANEL WELCOME GRADASI ---
                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(LightAccentBlue, PrimaryBlue)
                                    )
                                )
                                .padding(44.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = "Logo Aplikasi",
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "WELCOME",
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "HDM POS System",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sistem Manajemen Kasir Multi-Tenant Terintegrasi Cloud. Kelola operasional bisnis Anda secara aman, cepat, dan efisien dalam satu dasbor terpusat.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }

                        // --- SISI KANAN: PANEL FORM DINAMIS (LOGIN / REGISTER) ---
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(44.dp)
                        ) {
                            if (authMode == AuthMode.LOGIN) {
                                // -------------------------------------------------
                                // TAMPILAN FORM LOGIN
                                // -------------------------------------------------
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(text = "Sign in", color = PrimaryBlue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Silakan masuk dengan akun Google Anda yang terdaftar.", color = MutedTextDark.copy(alpha = 0.6f), fontSize = 13.sp)

                                        Spacer(modifier = Modifier.height(48.dp))

                                        // Tombol Utama Google Sign In (Disesuaikan dengan mekanisme browser Akang)
                                        Button(
                                            onClick = {
                                                isLoading = true
                                                scope.launch {
                                                    val email = loginWithGoogleBrowser()
                                                    if (email != null) {
                                                        java.awt.Window.getWindows().forEach { window ->
                                                            window.toFront()
                                                            window.requestFocus()
                                                        }
                                                        when (val result = AuthRepository.validateStoreAccess(email)) {
                                                            is AuthResult.Success -> { currentSession = result.session }
                                                            is AuthResult.Error -> { errorMessage = result.message }
                                                        }
                                                    }
                                                    isLoading = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Masuk Menggunakan Google", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Pindah ke Halaman Register
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Belum punya akun toko?", fontSize = 12.sp, color = MutedTextDark)
                                        TextButton(onClick = { authMode = AuthMode.REGISTER }) {
                                            Text("Daftar Sekarang", color = LightAccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                // -------------------------------------------------
                                // TAMPILAN FORM REGISTER (INPUTAN SESUAI PERMINTAAN)
                                // -------------------------------------------------
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(text = "Daftar Akun", color = PrimaryBlue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Lengkapi data di bawah untuk registrasi mitra baru.", color = MutedTextDark.copy(alpha = 0.6f), fontSize = 13.sp)

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Input 1: Nama Toko
                                        OutlinedTextField(
                                            value = regStoreName,
                                            onValueChange = { regStoreName = it },
                                            label = { Text("Nama Toko") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Input 2: Email
                                        OutlinedTextField(
                                            value = regEmail,
                                            onValueChange = { regEmail = it },
                                            label = { Text("Email Aktif") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Input 3: No HP
                                        OutlinedTextField(
                                            value = regPhone,
                                            onValueChange = { regPhone = it },
                                            label = { Text("No. Handphone") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                        )

                                        Spacer(modifier = Modifier.height(28.dp))

                                        // Tombol Eksekusi Registrasi
                                        Button(
                                            onClick = {
                                                if (regStoreName.isNotBlank() && regEmail.isNotBlank() && regPhone.isNotBlank()) {
                                                    // Menampilkan popup sukses registrasi
                                                    showRegisterSuccessDialog = true
                                                } else {
                                                    errorMessage = "Harap isi seluruh formulir pendaftaran!"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Daftar Sekarang", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Kembali ke Halaman Login
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Sudah memiliki akun?", fontSize = 12.sp, color = MutedTextDark)
                                        TextButton(onClick = { authMode = AuthMode.LOGIN }) {
                                            Text("Log in", color = LightAccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Overlay Loading saat browser / validasi beroperasi
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF60EFFF))
                    }
                }

                // Popup Dialog kendala akun internal
                if (errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { errorMessage = null },
                        title = { Text("Pemberitahuan Sistem", fontWeight = FontWeight.Bold) },
                        text = { Text(errorMessage!!) },
                        confirmButton = {
                            Button(onClick = { errorMessage = null }) { Text("Mengerti") }
                        }
                    )
                }

                // =================================================================
                // 3. POPUP BERHASIL REGISTER & REDIRECT KE LOGIN
                // =================================================================
                if (showRegisterSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Sukses", tint = Color(0xFF2EA44F))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pendaftaran Berhasil!", fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            Text("Toko '$regStoreName' telah berhasil diregistrasi ke sistem multi-tenant. Silakan kembali ke halaman utama untuk masuk menggunakan Google.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showRegisterSuccessDialog = false
                                    // Mengosongkan isian form register
                                    regStoreName = ""
                                    regEmail = ""
                                    regPhone = ""
                                    // Otomatis redirect mengembalikan tampilan ke halaman Login
                                    authMode = AuthMode.LOGIN
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                            ) {
                                Text("Kembali Ke Login", color = Color.White)
                            }
                        }
                    )
                }

            } else {
                // Alur Sukses Masuk -> Masuk ke Halaman Dashboard Utama
                DashboardScreen(
                    session = currentSession!!,
                    onLogout = { currentSession = null }
                )
            }

            // --- POPUP EMERGENCY LOG UNTUK ERROR GLOBAL SYSTEM ---
            if (globalCrashMessage != null) {
                AlertDialog(
                    onDismissRequest = { globalCrashMessage = null },
                    title = { Text("Sistem Mengalami Kesalahan Fatal", fontWeight = FontWeight.Bold, color = Color.Red) },
                    text = {
                        Text("Aplikasi mendeteksi error tidak terduga. File catatan kerusakan telah berhasil disimpan secara otomatis di Desktop Mac Anda dengan nama file 'hdm_pos_error.log'.\n\nDetail Singkat: $globalCrashMessage")
                    },
                    confirmButton = {
                        Button(onClick = { globalCrashMessage = null }) { Text("Tutup Dialog") }
                    }
                )
            }
        }
    }
}

fun main() = application {
    // 1. MEMASANG GLOBAL EXCEPTION HANDLER
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val fullErrorStackTrace = sw.toString()

        try {
            val desktopPath = System.getProperty("user.home") + "/Desktop/hdm_pos_error.log"
            File(desktopPath).writeText(fullErrorStackTrace)
        } catch (e: Exception) {
            println("Sistem gagal menulis log fisik ke Desktop: ${e.message}")
        }

        globalCrashMessage = throwable.localizedMessage ?: throwable.toString()
    }

    // Panggil inisialisasi Firebase Admin
    initFirebase()

    Window(onCloseRequest = ::exitApplication, title = "Sistem Admin Desktop") {
        DesktopApp()
    }
}