package com.example.sellr

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodItem(
    val id: String = "", // Pastikan ini unik untuk setiap produk hardcoded
    val name: String = "",
    val price: Long = 0,
    val imageUrl: String = ""
) : Parcelable