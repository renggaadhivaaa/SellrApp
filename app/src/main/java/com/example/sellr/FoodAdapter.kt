package com.example.sellr

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sellr.databinding.ItemFoodBinding
import java.text.NumberFormat
import java.util.Locale

class FoodAdapter(
    private val foodList: List<FoodItem>,
    private val onAddToCartClicked: (FoodItem) -> Unit // Lambda untuk aksi klik
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(foodItem: FoodItem) {
            binding.tvFoodName.text = foodItem.name
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvFoodPrice.text = formatter.format(foodItem.price)

            Log.d("FoodAdapter", "Loading image for ${foodItem.name}: ${foodItem.imageUrl}")

            Glide.with(itemView.context)
                .load(foodItem.imageUrl)
                .placeholder(R.drawable.ic_placeholder_image) // Placeholder saat memuat
                .error(R.drawable.ic_error_image)       // Gambar jika terjadi error
                .into(binding.ivFoodImage)

            // Pastikan listener terpasang dengan benar
            binding.btnAddToCart.setOnClickListener {
                Log.d("FoodAdapter", "Tombol Tambah diklik untuk: ${foodItem.name}")
                onAddToCartClicked(foodItem) // Panggil lambda yang diteruskan dari Activity
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodList[position])
    }

    override fun getItemCount(): Int = foodList.size
}