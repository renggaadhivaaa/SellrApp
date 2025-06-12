package com.example.sellr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sellr.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var foodAdapter: FoodAdapter
    private var foodList: List<FoodItem> = listOf()

    private val auth = FirebaseAuth.getInstance()
    // URL Database
    private val database = FirebaseDatabase.getInstance("https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    private val cartNode = "carts"
    private val TAG = "MainActivity_DB_FIX"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i(TAG, "onCreate: Firebase Database URL: ${database.toString()}")

        foodList = getHardcodedProductListWithNewImages() // Gunakan fungsi baru
        setupRecyclerView()
        setupToolbar()

        binding.fabCart.setOnClickListener {
            Log.d(TAG, "Tombol Keranjang (FAB) diklik.")
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun getHardcodedProductListWithNewImages(): List<FoodItem> {
        // Mencoba URL dari sumber yang sangat publik dan stabil (misal: Wikipedia Commons, Unsplash, Pixabay direct links)
        return listOf(
            FoodItem(
                id = "HRD001",
                name = "Nasi Goreng Ayam Spesial",
                price = 28000,
                imageUrl = "https://i.pinimg.com/736x/94/82/ab/9482ab2e248d249e7daa7fd6924c8d3b.jpg"
            ),
            FoodItem(
                id = "HRD002",
                name = "Ayam Bakar Kecap",
                price = 32000,
                imageUrl = "https://i.pinimg.com/736x/08/77/a7/0877a7d7d769099216823f067373a0fa.jpg"
            ),
            FoodItem(
                id = "HRD003",
                name = "Soto Ayam",
                price = 20000,
                imageUrl = "https://i.pinimg.com/736x/c6/28/e5/c628e596829de0d478045472c8d2b260.jpg"
            ),
            FoodItem(
                id = "HRD004",
                name = "Gado-Gado" +
                        "" +
                        "" +
                        "" +
                        "",
                price = 22000,
                imageUrl = "https://i.pinimg.com/736x/3a/ec/9e/3aec9effb6ac339895b8ef4e281b2acf.jpg"
            ),
            FoodItem(
                id = "HRD005",
                name = "Es Jeruk Manis",
                price = 8000,
                imageUrl = "https://i.pinimg.com/736x/9a/10/98/9a10985db487e939ce8a4fc8dd6eb7d3.jpg"
            ),
            FoodItem(
                id = "HRD006",
                name = "Mie Goreng Jawa",
                price = 26000,
                imageUrl = "https://i.pinimg.com/736x/0f/76/e8/0f76e8e797bf5d4e40f004475ffdbe16.jpg"
            )
        )
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_history -> {
                    Log.d(TAG, "Menu Riwayat Pesanan diklik.")
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.menu_logout -> {
                    Log.d(TAG, "Menu Logout diklik.")
                    auth.signOut()
                    Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(foodList) { selectedFoodItem ->
            Log.i(TAG, "Tombol 'Tambah' untuk item '${selectedFoodItem.name}' diklik di adapter.")
            addToCart(selectedFoodItem)
        }
        binding.rvFood.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = foodAdapter
        }
    }

    private fun addToCart(foodItem: FoodItem) {
        Log.i(TAG, "Memulai proses addToCart untuk: ${foodItem.name} (ID: ${foodItem.id})")
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "addToCart gagal: Pengguna belum login.")
            Toast.makeText(this, "Anda belum login. Silakan login terlebih dahulu.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        Log.d(TAG, "Pengguna saat ini: ${currentUser.uid}")
        val cartItemRef = database.child(cartNode).child(currentUser.uid).child(foodItem.id)
        Log.d(TAG, "Referensi Firebase untuk item keranjang: ${cartItemRef.toString()}")

        cartItemRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange dipanggil untuk item ${foodItem.id}. Snapshot ada: ${snapshot.exists()}, Value: ${snapshot.value}")
                if (snapshot.exists()) {
                    val currentCartItem = snapshot.getValue(CartItem::class.java)
                    val currentQuantity = currentCartItem?.quantity ?: 0
                    val newQuantity = currentQuantity + 1
                    Log.d(TAG, "Item ${foodItem.id} sudah ada. Kuantitas lama: $currentQuantity, Kuantitas baru: $newQuantity")

                    // Update quantity saja
                    snapshot.ref.child("quantity").setValue(newQuantity)
                        .addOnSuccessListener {
                            Log.i(TAG, "Kuantitas untuk ${foodItem.name} berhasil diperbarui menjadi $newQuantity.")
                            Toast.makeText(this@MainActivity, "${foodItem.name} kuantitas +1", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Gagal memperbarui kuantitas untuk ${foodItem.name}: ${e.message}", e)
                            Toast.makeText(this@MainActivity, "Gagal update kuantitas: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Log.d(TAG, "Item ${foodItem.id} belum ada di keranjang. Membuat item baru.")
                    val newCartItem = CartItem(foodItem = foodItem, quantity = 1)
                    cartItemRef.setValue(newCartItem)
                        .addOnSuccessListener {
                            Log.i(TAG, "${foodItem.name} berhasil ditambahkan ke keranjang.")
                            Toast.makeText(this@MainActivity, "${foodItem.name} ditambahkan", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Gagal menambahkan ${foodItem.name} ke keranjang: ${e.message}", e)
                            Toast.makeText(this@MainActivity, "Gagal menambahkan: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Operasi database addToCart dibatalkan untuk ${foodItem.id}: ${error.message}", error.toException())
                Toast.makeText(this@MainActivity, "Database error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
