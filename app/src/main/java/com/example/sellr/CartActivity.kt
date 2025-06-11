package com.example.sellr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sellr.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()
    private val auth = FirebaseAuth.getInstance()
    // URL Database
    private val database = FirebaseDatabase.getInstance("https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    private val cartNode = "carts"
    private val ordersNode = "orders"
    private val TAG = "CartActivity_DB_FIX"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i(TAG, "onCreate: Firebase Database URL: ${database.toString()}")

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "onCreate gagal: userId null. Pengguna tidak login.")
            Toast.makeText(this, "Sesi berakhir, silakan login kembali.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        Log.d(TAG, "Pengguna saat ini: $userId")

        setupRecyclerView(userId)
        fetchCartData(userId)

        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Tombol kembali (toolbar) diklik.")
            finish()
        }

        binding.btnCheckout.setOnClickListener {
            Log.i(TAG, "Tombol Checkout diklik.")
            if (cartList.isNotEmpty()) {
                Log.d(TAG, "Keranjang tidak kosong (${cartList.size} item). Memulai konfirmasi checkout.")
                showCheckoutConfirmationDialog(userId)
            } else {
                Log.w(TAG, "Tombol Checkout diklik, tetapi keranjang kosong.")
                Toast.makeText(this, "Keranjang Anda kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView(userId: String) {
        Log.d(TAG, "setupRecyclerView untuk userId: $userId")
        cartAdapter = CartAdapter(cartList,
            onIncrement = { cartItem -> updateQuantity(userId, cartItem, cartItem.quantity + 1) },
            onDecrement = { cartItem ->
                if (cartItem.quantity > 1) {
                    updateQuantity(userId, cartItem, cartItem.quantity - 1)
                } else {
                    Log.d(TAG, "Kuantitas item ${cartItem.foodItem.name} akan menjadi 0, menghapus dari keranjang.")
                    removeItemFromCart(userId, cartItem)
                }
            }
        )
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun fetchCartData(userId: String) {
        Log.d(TAG, "fetchCartData untuk userId: $userId")
        val userCartRef = database.child(cartNode).child(userId)
        Log.d(TAG, "Referensi keranjang pengguna: ${userCartRef.toString()}")

        userCartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "onDataChange untuk keranjang dipanggil. Snapshot ada: ${snapshot.exists()}, Value: ${snapshot.value}")
                cartList.clear()
                if (snapshot.exists()) {
                    for (cartSnapshot in snapshot.children) {
                        val cartItem = cartSnapshot.getValue(CartItem::class.java)
                        if (cartItem != null) {
                            Log.d(TAG, "Menambahkan item ke cartList: ${cartItem.foodItem.name} (Qty: ${cartItem.quantity})")
                            cartList.add(cartItem)
                        } else {
                            Log.w(TAG, "Gagal parse CartItem dari snapshot: ${cartSnapshot.key}")
                        }
                    }
                } else {
                    Log.d(TAG, "Snapshot keranjang tidak ada atau kosong.")
                }
                cartAdapter.notifyDataSetChanged() // Penting untuk refresh UI
                updateTotalPrice()
                toggleEmptyView()
                Log.d(TAG, "Total item di cartList setelah onDataChange: ${cartList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchCartData dibatalkan: ${error.message}", error.toException())
                Toast.makeText(this@CartActivity, "Gagal memuat keranjang: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateQuantity(userId: String, cartItem: CartItem, newQuantity: Int) {
        Log.d(TAG, "updateQuantity untuk ${cartItem.foodItem.name} menjadi $newQuantity")
        database.child(cartNode).child(userId).child(cartItem.foodItem.id).child("quantity").setValue(newQuantity)
            .addOnSuccessListener { Log.i(TAG, "Kuantitas untuk ${cartItem.foodItem.name} berhasil diupdate.") }
            .addOnFailureListener { e-> Log.e(TAG, "Gagal update kuantitas: ${e.message}") }
    }

    private fun removeItemFromCart(userId: String, cartItem: CartItem) {
        Log.d(TAG, "removeItemFromCart untuk ${cartItem.foodItem.name}")
        database.child(cartNode).child(userId).child(cartItem.foodItem.id).removeValue()
            .addOnSuccessListener { Log.i(TAG, "Item ${cartItem.foodItem.name} berhasil dihapus.") }
            .addOnFailureListener { e-> Log.e(TAG, "Gagal hapus item: ${e.message}") }
    }

    private fun updateTotalPrice() {
        val totalPrice = cartList.sumOf { (it.foodItem.price * it.quantity) }
        Log.d(TAG, "updateTotalPrice: Total harga baru adalah Rp $totalPrice")
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        binding.tvTotalPrice.text = formatter.format(totalPrice)
    }

    private fun toggleEmptyView() {
        if (cartList.isEmpty()) {
            Log.d(TAG, "toggleEmptyView: Keranjang kosong.")
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.rvCart.visibility = View.GONE
            binding.btnCheckout.isEnabled = false
        } else {
            Log.d(TAG, "toggleEmptyView: Keranjang berisi item.")
            binding.tvEmptyCart.visibility = View.GONE
            binding.rvCart.visibility = View.VISIBLE
            binding.btnCheckout.isEnabled = true
        }
    }

    private fun showCheckoutConfirmationDialog(userId: String) {
        Log.d(TAG, "Menampilkan dialog konfirmasi checkout.")
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pesanan")
            .setMessage("Anda yakin ingin menyelesaikan pesanan ini?")
            .setPositiveButton("Ya, Proses Pesanan") { dialog, _ ->
                Log.i(TAG, "Pengguna mengkonfirmasi pesanan. Memulai processCheckout.")
                processCheckout(userId)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                Log.d(TAG, "Pengguna membatalkan checkout.")
                dialog.dismiss()
            }
            .show()
    }

    private fun processCheckout(userId: String) {
        Log.i(TAG, "Memulai processCheckout untuk userId: $userId")
        if (cartList.isEmpty()) {
            Log.w(TAG, "processCheckout dihentikan: Keranjang kosong.")
            Toast.makeText(this, "Tidak ada item untuk di-checkout.", Toast.LENGTH_SHORT).show()
            return
        }

        val ordersUserRef = database.child(ordersNode).child(userId)
        val orderId = ordersUserRef.push().key

        if(orderId == null) {
            Log.e(TAG, "processCheckout gagal: Gagal membuat orderId (null).")
            Toast.makeText(this, "Gagal membuat ID pesanan. Coba lagi.", Toast.LENGTH_LONG).show()
            return
        }
        Log.d(TAG, "orderId berhasil dibuat: $orderId")

        val totalOrderPrice = cartList.sumOf { (it.foodItem.price * it.quantity) }
        val currentTimestamp = System.currentTimeMillis()
        val itemsForOrder = ArrayList(cartList)

        val newOrder = Order(
            orderId = orderId,
            items = itemsForOrder,
            totalPrice = totalOrderPrice,
            timestamp = currentTimestamp,
            userId = userId
        )
        Log.d(TAG, "Objek Order dibuat: $newOrder")

        ordersUserRef.child(orderId).setValue(newOrder)
            .addOnSuccessListener {
                Log.i(TAG, "Pesanan $orderId berhasil disimpan ke Firebase.")
                Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()

                val userCartRef = database.child(cartNode).child(userId)
                userCartRef.removeValue()
                    .addOnSuccessListener { Log.i(TAG, "Keranjang untuk pengguna $userId berhasil dihapus.") }
                    .addOnFailureListener { e -> Log.e(TAG, "Gagal menghapus keranjang $userId: ${e.message}", e) }

                Log.d(TAG, "Mengarahkan ke HistoryActivity.")
                val intent = Intent(this, HistoryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Gagal menyimpan pesanan $orderId ke Firebase: ${e.message}", e)
                Toast.makeText(this, "Gagal memproses pesanan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
