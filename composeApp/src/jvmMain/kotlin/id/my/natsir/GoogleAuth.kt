package id.my.natsir

import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.oauth2.Oauth2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

suspend fun loginWithGoogleBrowser(): String? = withContext(Dispatchers.IO) {
    var receiver: LocalServerReceiver? = null
    try {
        val jsonFactory = GsonFactory.getDefaultInstance()
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

        // 1. Membaca file konfigurasi OAuth yang Anda unduh dari resources
        val inputStream = Thread.currentThread().contextClassLoader
            .getResourceAsStream("client_secret.json") ?: throw IllegalStateException("File client_secret.json tidak ditemukan di resources!")
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(inputStream))

        // 2. Meminta izin ke Google untuk membaca Email profil pengguna
        val scopes = listOf(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
        )

        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets, scopes
        ).setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
            .setAccessType("offline")
            .build()

        // 3. Menyalakan local server di port 8888 untuk menerima callback dari Browser
        receiver = LocalServerReceiver.Builder().setPort(8888).build()
        val redirectUri = receiver.redirectUri

        // 4. Membentuk URL Oauth login secara mandiri
        val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri)
        val url = authorizationUrl.build()

        println("Please open the following address in your browser:")
        println("  $url")
        println("Attempting to open that address in the default browser now...")

        // 5. MEMANGGIL NATIVE SHELL OPERATING SYSTEM (Solusi Anti-SIGKILL / Force Close Mac M1)
        val os = System.getProperty("os.name").lowercase()
        when {
            os.contains("mac") -> {
                // Membuka peramban default melalui perintah native OS tanpa perantara AWT JNI
                Runtime.getRuntime().exec(arrayOf("open", url))
            }
            os.contains("win") -> {
                Runtime.getRuntime().exec(arrayOf("cmd", "/c", "start", url))
            }
            else -> {
                // Jalur alternatif standar jika dijalankan pada Linux desktop biasa
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(java.net.URI(url))
                }
            }
        }

        // 6. Menghadang thread untuk menunggu kiriman kode verifikasi dari peramban web
        val code = receiver.waitForCode()

        // 7. Menukarkan auth code yang didapat untuk memperoleh token resmi
        val tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute()
        val credential = flow.createAndStoreCredential(tokenResponse, "user")

        // 8. Mengambil informasi profil (Email) menggunakan token yang didapat
        val oauth2 = Oauth2.Builder(httpTransport, jsonFactory, credential).setApplicationName("Sistem POS").build()
        val userInfo = oauth2.userinfo().get().execute()

        // Mengembalikan alamat email (misal: mnatsir@gmail.com)
        return@withContext userInfo.email

    } catch (e: Exception) {
        println("Proses login gagal atau dibatalkan: ${e.message}")
        e.printStackTrace()
        return@withContext null
    } finally {
        // Pastikan port local server dibersihkan kembali setelah proses berakhir
        try {
            receiver?.stop()
        } catch (ex: Exception) {
            println("Gagal menghentikan local receiver: ${ex.message}")
        }
    }
}