package id.my.natsir

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import echo93project.composeapp.generated.resources.Res
import echo93project.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource


// Warna utama untuk tema (Berasumsi dari inspirasi gambar)
val PrimaryColor = Color(0xFF19375F) // Biru tua tombol/teks judul signin
val AccentColor = Color(0xFF327DF3)  // Biru muda Welcome
val BackgroundTextColor = Color.White
val FormTextColor = Color(0xFF2C3E50) // Warna teks form gelap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenRedesign() {
    // 2. State Stateful
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // State input form Login
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // State input form Register (Inputan spesifik)
    var regStoreName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }

    // 3. Popup Berhasil Pendaftaran
    if (showSuccessDialog) {
        SuccessRegistrationDialog(
            onDismiss = {
                showSuccessDialog = false
                // 4. Redirect ke halaman Login setelah popup ditutup
                authMode = AuthMode.LOGIN
                // Bersihkan inputan register
                regStoreName = ""
                regEmail = ""
                regPhone = ""
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        // Kontainer Utama dengan Background Image (User minta background menggunakan image)
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image. Ganti resource Anda jika ada (misal: login_background.jpg)
            // Image(
            //     painter = painterResource(Res.drawable.login_background),
            //     contentDescription = "Background Image",
            //     modifier = Modifier.fillMaxSize(),
            //     contentScale = ContentScale.Crop
            // )

            // Layout Kiri - Kanan seperti image_6.png. Area Kiri di kontainer utama.
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp) // Padding kontainer utama dari sisi layar
                        .clip(RoundedCornerShape(24.dp))
                        // Menggunakan gradien biru sebagai pengganti untuk visual di kontainer kiri
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(AccentColor, PrimaryColor) // Biru muda ke biru tua
                            )
                        )
                        .height(600.dp) // Tinggi kontainer utama
                ) {
                    // Area Kiri - Welcome (Teks)
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .padding(48.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Gunakan logo yang sudah ada di drawable
                        Icon(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = "Project Logo",
                            tint = BackgroundTextColor,
                            modifier = Modifier.size(70.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "WELCOME",
                            color = BackgroundTextColor,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Echo93 POS System", // Headline project
                            color = BackgroundTextColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aplikasi Kasir Premium dari PT. Hawari Digital Multimedia Inovasi untuk solusi bisnis Anda. Copyright © 2026.", // Deskripsi
                            color = BackgroundTextColor.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                    }

                    // Area Kanan - Form (Sign In / Sign Up)
                    Crossfade(
                        targetState = authMode,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                            .background(Color.White)
                            .fillMaxHeight()
                    ) { currentMode ->
                        Column(
                            modifier = Modifier
                                .padding(48.dp)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (currentMode == AuthMode.LOGIN) {
                                // --- Form LOGIN (Mengacu image_6.png) ---
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Sign in",
                                        color = PrimaryColor,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Silakan masuk ke akun Anda",
                                        color = FormTextColor.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                }

                                // Input Email (Login)
                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    label = { Text("Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Input Password (Login)
                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    label = { Text("Password") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                        val description = if (passwordVisible) "Sembunyikan password" else "Tampilkan password"
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(imageVector = image, contentDescription = description, tint = PrimaryColor)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Forgot Password link
                                TextButton(
                                    onClick = { /* Logika lupa password */ },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        color = PrimaryColor,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(32.dp))

                                // Tombol Sign In (Login)
                                Button(
                                    onClick = { /* Logika Login Firebase */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Text("Sign in", color = BackgroundTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                // Teks ke halaman Register
                                Row(horizontalArrangement = Arrangement.Center) {
                                    Text("Don't have an account?", fontSize = 12.sp)
                                    TextButton(
                                        onClick = { authMode = AuthMode.REGISTER },
                                        contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("Sign up", color = PrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                            } else {
                                // --- Form REGISTER (Inputan baru sesuai permintaan user) ---
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Daftar Akun",
                                        color = PrimaryColor,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Buat akun toko baru untuk menggunakan aplikasi.",
                                        color = FormTextColor.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                }

                                // Input Nama Toko
                                OutlinedTextField(
                                    value = regStoreName,
                                    onValueChange = { regStoreName = it },
                                    label = { Text("Nama Toko") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Input Email (Register)
                                OutlinedTextField(
                                    value = regEmail,
                                    onValueChange = { regEmail = it },
                                    label = { Text("Email Akun") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Input No HP
                                OutlinedTextField(
                                    value = regPhone,
                                    onValueChange = { regPhone = it },
                                    label = { Text("No HP") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                                Spacer(modifier = Modifier.height(32.dp))

                                // Tombol Register (Daftar Sekarang)
                                Button(
                                    onClick = {
                                        // 5. Logika pendaftaran. Di sini cukup tampilkan popup berhasil.
                                        // Idealnya, data regStoreName, regEmail, regPhone dikirim ke backend/Firebase.
                                        showSuccessDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Text("Daftar Sekarang", color = BackgroundTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                // Teks kembali ke halaman Login
                                Row(horizontalArrangement = Arrangement.Center) {
                                    Text("Already have an account?", fontSize = 12.sp)
                                    TextButton(
                                        onClick = { authMode = AuthMode.LOGIN },
                                        contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("Sign in", color = PrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// 3. Popup "Pendaftaran Berhasil"
@Composable
fun SuccessRegistrationDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ikon Berhasil
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.CheckCircle,
                    contentDescription = "Success Icon",
                    tint = AccentColor,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Daftar Berhasil!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = FormTextColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Akun toko Anda berhasil didaftarkan. Silakan login menggunakan email yang telah didaftarkan.",
                    textAlign = TextAlign.Center,
                    color = FormTextColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                // 4. Tombol redirect kembali ke Login
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Masuk", color = BackgroundTextColor)
                }
            }
        }
    }
}