package com.example.sellr

// Tidak perlu Parcelable jika hanya untuk Firebase dan tidak dikirim via Intent
data class Order(
    val orderId: String = "",
    val items: List<CartItem> = listOf(), // List dari CartItem
    val totalPrice: Long = 0,
    val timestamp: Long = 0,
    val userId: String = ""
) {
    // Constructor kosong diperlukan untuk Firebase
    constructor() : this("", listOf(), 0, 0, "")
}