package com.example.sellr

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sellr.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var orderAdapter: OrderAdapter
    private val orderList = mutableListOf<Order>()
    private val auth = FirebaseAuth.getInstance()
    // URL Database
    private val database = FirebaseDatabase.getInstance("https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    private val ordersNode = "orders"
    private val TAG = "HistoryActivity_DB_FIX"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
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

        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Tombol kembali (toolbar) diklik.")
            finish()
        }
        setupRecyclerView()
        fetchOrderHistory(userId)
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView")
        orderAdapter = OrderAdapter(orderList)
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = orderAdapter
        }
    }

    private fun fetchOrderHistory(userId: String) {
        Log.d(TAG, "fetchOrderHistory untuk userId: $userId")
        val userOrdersRef = database.child(ordersNode).child(userId)
        Log.d(TAG, "Referensi riwayat pesanan pengguna: ${userOrdersRef.toString()}")

        // Mengurutkan berdasarkan timestamp (pesanan terbaru di atas setelah reverse)
        userOrdersRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "onDataChange untuk riwayat pesanan dipanggil. Snapshot ada: ${snapshot.exists()}, Value: ${snapshot.value}")
                orderList.clear()
                if(snapshot.exists()){
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null) {
                            Log.d(TAG, "Menambahkan pesanan ke orderList: ID ${order.orderId}")
                            orderList.add(order)
                        } else {
                            Log.w(TAG, "Gagal parse Order dari snapshot: ${orderSnapshot.key}")
                        }
                    }
                } else {
                    Log.d(TAG, "Snapshot riwayat pesanan tidak ada atau kosong.")
                }
                orderList.reverse() // Tampilkan pesanan terbaru di atas
                orderAdapter.notifyDataSetChanged()
                toggleEmptyView()
                Log.d(TAG, "Total pesanan di orderList setelah onDataChange: ${orderList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchOrderHistory dibatalkan: ${error.message}", error.toException())
                Toast.makeText(this@HistoryActivity, "Gagal memuat riwayat: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun toggleEmptyView() {
        if (orderList.isEmpty()) {
            Log.d(TAG, "toggleEmptyView: Riwayat kosong.")
            binding.tvNoHistory.visibility = View.VISIBLE
            binding.rvHistory.visibility = View.GONE
        } else {
            Log.d(TAG, "toggleEmptyView: Riwayat berisi pesanan.")
            binding.tvNoHistory.visibility = View.GONE
            binding.rvHistory.visibility = View.VISIBLE
        }
    }
}
