package com.example.sellr

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val foodItem: FoodItem = FoodItem(), // Pastikan ini adalah objek FoodItem, bukan hanya ID
    var quantity: Int = 0
) : Parcelable {
    // Constructor kosong diperlukan untuk Firebase deserialization
    constructor() : this(FoodItem(), 0)
}