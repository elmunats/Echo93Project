package id.my.natsir

import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object InventoryRepository {

    // Simpan produk dengan mengikat ID toko yang sedang aktif
    suspend fun saveProduct(product: Product): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirestoreClient.getFirestore()
            val id = product.id.ifEmpty { UUID.randomUUID().toString() }
            val productMap = mapOf<String, Any>(
                "id" to id,
                "storeId" to product.storeId, // <-- Menyimpan kepemilikan ID Toko
                "sku" to product.sku,
                "name" to product.name,
                "type" to product.type.name,
                "category" to product.category,
                "stock" to product.stock,
                "costPrice" to product.costPrice,
                "sellPrice" to product.sellPrice
            )
            db.collection("products").document(id).set(productMap).get()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Hanya menarik data produk milik toko yang sedang login saja
    suspend fun getAllProducts(storeId: String): List<Product> = withContext(Dispatchers.IO) {
        try {
            val db = FirestoreClient.getFirestore()
            // Menggunakan klausa whereEqualTo untuk isolasi ketat keamanan database
            val snapshot = db.collection("products")
                .whereEqualTo("storeId", storeId)
                .get().get()

            snapshot.documents.map { doc ->
                Product(
                    id = doc.getString("id") ?: "",
                    storeId = doc.getString("storeId") ?: "",
                    sku = doc.getString("sku") ?: "",
                    name = doc.getString("name") ?: "",
                    type = ItemType.valueOf(doc.getString("type") ?: "BARANG_JUAL"),
                    category = doc.getString("category") ?: "",
                    stock = (doc.getLong("stock") ?: 0L).toInt(),
                    costPrice = doc.getDouble("costPrice") ?: 0.0,
                    sellPrice = doc.getDouble("sellPrice") ?: 0.0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun doStockOpname(productId: String, previousStock: Int, realStock: Int, note: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirestoreClient.getFirestore()
            val pRef = db.collection("products").document(productId)
            pRef.update("stock", realStock).get()

            val logId = UUID.randomUUID().toString()
            val logMap = mapOf<String, Any>(
                "logId" to logId,
                "productId" to productId,
                "previousStock" to previousStock,
                "realStock" to realStock,
                "difference" to (realStock - previousStock),
                "note" to note,
                "timestamp" to System.currentTimeMillis()
            )
            pRef.collection("stock_history").document(logId).set(logMap).get()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}