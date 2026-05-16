package id.my.natsir

import com.google.cloud.Timestamp
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Representasi data sesi toko yang sukses divalidasi
data class StoreSession(
    val storeId: String,
    val userEmail: String,
    val role: String // "SUPERUSER" atau "USERADMIN"
)

// Wrapper status hasil otentikasi tingkat lanjut
sealed class AuthResult {
    data class Success(val session: StoreSession) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object AuthRepository {

    suspend fun validateStoreAccess(email: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val db = FirestoreClient.getFirestore()

            // 1. Pindai apakah email berada di barisan Array 'superuser'
            var querySnapshot = db.collection("stores")
                .whereArrayContains("superuser", email)
                .get().get()

            var doc = querySnapshot.documents.firstOrNull()
            var detectedRole = "SUPERUSER"

            // 2. Jika nihil, pindai ke barisan Array 'useradmin'
            if (doc == null) {
                querySnapshot = db.collection("stores")
                    .whereArrayContains("useradmin", email)
                    .get().get()
                doc = querySnapshot.documents.firstOrNull()
                detectedRole = "USERADMIN"
            }

            // 3. Jika email tidak ditemukan di dokumen toko mana pun
            if (doc == null) {
                return@withContext AuthResult.Error("Akses ditolak. Email Anda tidak terdaftar di toko manapun.")
            }

            // 4. Ekstrak data validasi operasional toko
            val storeId = doc.id
            val isActived = (doc.getLong("actived") ?: 0L).toInt()
            val expiredTimestamp = doc.getTimestamp("expired") ?: Timestamp.now()

            // 5. Validasi Parameter Keaktifan Toko
            if (isActived != 1) {
                return@withContext AuthResult.Error("Akses ditangguhkan. Toko '$storeId' berstatus tidak aktif.")
            }

            // 6. Validasi Parameter Kadaluarsa Sistem
            if (expiredTimestamp.compareTo(Timestamp.now()) < 0) {
                return@withContext AuthResult.Error("Masa langganan aplikasi untuk toko '$storeId' telah habis.")
            }

            // Berhasil lolos seluruh gerbang otentikasi SaaS
            AuthResult.Success(
                StoreSession(
                    storeId = storeId,
                    userEmail = email,
                    role = detectedRole
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.Error("Gagal menghubungkan ke server validasi: ${e.localizedMessage}")
        }
    }
}