package id.my.natsir

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform