package id.my.natsir

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

// State Global untuk memantau crash sistem di luar UI Thread utama
private var globalCrashMessage by mutableStateOf<String?>(null)

// 1. Inisialisasi Firebase Admin SDK
fun initFirebase() {
    try {
        // Membaca file JSON dari dalam internal package / resource aplikasi
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

    // Memegang objek utuh StoreSession hasil verifikasi Firestore multi-tenant
    var currentSession by remember { mutableStateOf<StoreSession?>(null) }

    // State pembantu untuk menampilkan info kendala login & status tombol loading
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) } // Disamakan namanya agar sinkron dengan LoginScreen

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            if (currentSession == null) {
                // Tampilkan layar login dengan parameter terpusat yang baru
                LoginScreen(
                    isLoading = isLoading,
                    onLoginClick = {
                        isLoading = true // Tombol langsung otomatis disable begitu diklik
                        scope.launch {
                            // A. Jalankan proses buka browser untuk Google Sign-In
                            val email = loginWithGoogleBrowser()

                            if (email != null) {
                                // B. Trik Redirect: Paksa jendela aplikasi Desktop naik ke depan browser setelah login
                                java.awt.Window.getWindows().forEach { window ->
                                    window.toFront()
                                    window.requestFocus()
                                }

                                // C. Jalankan validasi Multi-Tenant ke Firestore koleksi 'stores'
                                when (val result = AuthRepository.validateStoreAccess(email)) {
                                    is AuthResult.Success -> {
                                        currentSession = result.session
                                    }
                                    is AuthResult.Error -> {
                                        errorMessage = result.message
                                    }
                                }
                            }

                            // Matikan loading jika seluruh alur selesai (sukses, gagal, atau user cancel browser)
                            isLoading = false
                        }
                    }
                )

                // Indikator Loading overlay saat sistem sedang sibuk berinteraksi dengan database/browser
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF60EFFF))
                    }
                }

                // Popup Dialog jika akun diblokir, tidak terdaftar, atau kadaluarsa
                if (errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { errorMessage = null },
                        title = { Text("Gagal Masuk Sistem", fontWeight = FontWeight.Bold) },
                        text = { Text(errorMessage!!) },
                        confirmButton = {
                            Button(onClick = { errorMessage = null }) {
                                Text("Mengerti")
                            }
                        }
                    )
                }
            } else {
                // Berhasil masuk, lempar data sesi (storeId & email) ke dalam Dashboard
                DashboardScreen(
                    session = currentSession!!,
                    onLogout = { currentSession = null }
                )
            }

            // --- POPUP EMERGENCY LOG UNTUK ERROR GLOBAL LAINNYA ---
            if (globalCrashMessage != null) {
                AlertDialog(
                    onDismissRequest = { globalCrashMessage = null },
                    title = { Text("Sistem Mengalami Kesalahan Fatal", fontWeight = FontWeight.Bold, color = Color.Red) },
                    text = {
                        Text("Aplikasi mendeteksi error tidak terduga. File catatan kerusakan telah berhasil disimpan secara otomatis di Desktop Mac Anda dengan nama file 'hdm_pos_error.log'.\n\nDetail Singkat: $globalCrashMessage")
                    },
                    confirmButton = {
                        Button(onClick = { globalCrashMessage = null }) {
                            Text("Tutup Dialog")
                        }
                    }
                )
            }
        }
    }
}

fun main() = application {
    // 1. MEMASANG GLOBAL EXCEPTION HANDLER (Dipasang paling pertama sebelum proses apapun)
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val fullErrorStackTrace = sw.toString()

        try {
            // Secara otomatis menulis file log mentah langsung ke folder Desktop pengguna Mac
            val desktopPath = System.getProperty("user.home") + "/Desktop/hdm_pos_error.log"
            File(desktopPath).writeText(fullErrorStackTrace)
        } catch (e: Exception) {
            println("Sistem gagal menulis log fisik ke Desktop: ${e.message}")
        }

        // Update state agar memicu recomposition UI secara instan demi memunculkan popup AlertDialog
        globalCrashMessage = throwable.localizedMessage ?: throwable.toString()
    }

    // Panggil inisialisasi Firebase Admin
    initFirebase()

    Window(onCloseRequest = ::exitApplication, title = "Sistem Admin Desktop") {
        DesktopApp()
    }
}