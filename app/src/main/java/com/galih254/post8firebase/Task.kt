package com.galih254.post8firebase

// Pastikan nama variabel SAMA PERSIS dengan key di Firebase
data class Task(
    var id: String? = null,
    var title: String? = null,
    var description: String? = null,
    var date: String? = null,
    var isDone: Boolean = false // Ganti isDone jika di firebase namanya isCompleted
) {
    // Di Kotlin, constructor kosong dibuat otomatis jika semua var punya default value (null/false)
}