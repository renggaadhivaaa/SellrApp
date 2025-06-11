package com.example.sellr

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sellr.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    // ✅ Inisialisasi Firebase Auth langsung di sini
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener { signUpUser() }
        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    private fun signUpUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignUp.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            binding.progressBar.visibility = View.GONE
            binding.btnSignUp.isEnabled = true
            if (task.isSuccessful) {
                Toast.makeText(this, "Pendaftaran Berhasil, silakan login", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Pendaftaran Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}