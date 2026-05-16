package id.my.natsir

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun InventoryScreen(session: StoreSession) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    val primaryBlue = Color(0xFF1B4CC3)

    // State untuk Form Input Baru (Popup Dialog)
    var showAddDialog by remember { mutableStateOf(false) }
    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ItemType.BARANG_JUAL) }
    var costPrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var initialStock by remember { mutableStateOf("") }

    // Fungsi refresh data
    fun refreshData() {
        scope.launch { products = InventoryRepository.getAllProducts(session.storeId) }
    }

    LaunchedEffect(Unit) { refreshData() }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        // --- HEADER TAMPILAN ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MANAJEMEN STOK", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = primaryBlue)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { refreshData() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = primaryBlue)
                    Text(" REFRESH", color = primaryBlue, fontWeight = FontWeight.Bold)
                }

                // Tombol ini sekarang memicu Dialog Popup
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = primaryBlue)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                    Text(" TAMBAH BARANG", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TABEL HEADER BIRU ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = primaryBlue,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("SKU", color = Color.White, modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
                Text("NAMA BARANG", color = Color.White, modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("KATEGORI", color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("TIPE", color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("STOK", color = Color.White, modifier = Modifier.width(60.dp), fontWeight = FontWeight.Bold)
                Text("AKSI", color = Color.White, modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
            }
        }

        // --- TABEL ISI ---
        LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
            items(products) { product ->
                var showOpnameDialog by remember { mutableStateOf(false) }
                var inputRealStock by remember { mutableStateOf("") }
                var opnameNote by remember { mutableStateOf("") }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.sku, modifier = Modifier.width(100.dp))
                        Text(product.name, modifier = Modifier.weight(2f), fontWeight = FontWeight.SemiBold)
                        Text(product.category, modifier = Modifier.weight(1f))
                        Text(
                            product.type.name,
                            modifier = Modifier.weight(1f),
                            color = if(product.type == ItemType.BAHAN_BAKU) Color.Gray else Color(0xFF2E7D32)
                        )
                        Text(
                            "${product.stock}",
                            modifier = Modifier.width(60.dp),
                            fontWeight = FontWeight.Bold,
                            color = if(product.stock < 5) Color.Red else Color.Black
                        )

                        // Tombol Opname
                        Button(
                            onClick = { showOpnameDialog = true },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF57C00), contentColor = Color.White),
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text("Opname", fontSize = 11.sp)
                        }
                    }
                    Divider(color = Color(0xFFF1F5F9))
                }

                // --- DIALOG STOCK OPNAME ---
                if (showOpnameDialog) {
                    AlertDialog(
                        onDismissRequest = { showOpnameDialog = false },
                        title = { Text("Stock Opname: ${product.name}") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Stok Sistem saat ini: ${product.stock}")
                                OutlinedTextField(value = inputRealStock, onValueChange = { inputRealStock = it }, label = { Text("Jumlah Riil Fisik") })
                                OutlinedTextField(value = opnameNote, onValueChange = { opnameNote = it }, label = { Text("Keterangan Selisih") })
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                val real = inputRealStock.toIntOrNull() ?: product.stock
                                scope.launch {
                                    if (InventoryRepository.doStockOpname(product.id, product.stock, real, opnameNote)) {
                                        showOpnameDialog = false
                                        refreshData()
                                    }
                                }
                            }) { Text("Sesuaikan Stok") }
                        },
                        dismissButton = { TextButton(onClick = { showOpnameDialog = false }) { Text("Batal") } }
                    )
                }
            }
        }
    }

    // --- DIALOG TAMBAH BARANG BARU ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Item Baru", color = primaryBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("Kode SKU") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedType == ItemType.BARANG_JUAL, onClick = { selectedType = ItemType.BARANG_JUAL })
                            Text("Barang Jual", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedType == ItemType.BAHAN_BAKU, onClick = { selectedType = ItemType.BAHAN_BAKU })
                            Text("Bahan Baku", fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Harga Modal (Rp)") }, modifier = Modifier.fillMaxWidth())
                    if (selectedType == ItemType.BARANG_JUAL) {
                        OutlinedTextField(value = sellPrice, onValueChange = { sellPrice = it }, label = { Text("Harga Jual (Rp)") }, modifier = Modifier.fillMaxWidth())
                    }
                    OutlinedTextField(value = initialStock, onValueChange = { initialStock = it }, label = { Text("Stok Awal") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = Product(
                            sku = sku, name = name, category = category, type = selectedType,
                            stock = initialStock.toIntOrNull() ?: 0,
                            costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                            sellPrice = if (selectedType == ItemType.BARANG_JUAL) sellPrice.toDoubleOrNull() ?: 0.0 else 0.0
                        )
                        scope.launch {
                            if (InventoryRepository.saveProduct(p)) {
                                sku = ""; name = ""; category = ""; costPrice = ""; sellPrice = ""; initialStock = ""
                                showAddDialog = false
                                refreshData()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = primaryBlue, contentColor = Color.White)
                ) {
                    Text("Simpan Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal", color = Color.Gray) }
            }
        )
    }
}