package id.my.natsir

enum class ItemType {
    BARANG_JUAL,
    BAHAN_BAKU
}

data class Product(
    val id: String = "",
    val storeId: String = "", // <-- Kunci pemisah kepemilikan data tenant toko
    val sku: String = "",
    val name: String = "",
    val type: ItemType = ItemType.BARANG_JUAL,
    val category: String = "",
    val stock: Int = 0,
    val costPrice: Double = 0.0,
    val sellPrice: Double = 0.0
)