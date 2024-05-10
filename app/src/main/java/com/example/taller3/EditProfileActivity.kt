package com.example.taller3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    // Inicialización correcta de FirebaseAuth y FirebaseDatabase
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Define los EditText y Button
        val editTextFirstName = findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = findViewById<EditText>(R.id.editTextLastName)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        buttonSave.setOnClickListener {
            val firstName = editTextFirstName.text.toString().trim()
            val lastName = editTextLastName.text.toString().trim()

            updateUserData(firstName, lastName) // Actualiza los datos del usuario
        }
    }

    private fun updateUserData(firstName: String, lastName: String) {
        val userId = auth.currentUser?.uid  // Obtiene el UID del usuario actual
        if (userId != null) {
            val userUpdates = mapOf(
                "firstName" to firstName,
                "lastName" to lastName
            )

            // Actualiza los datos en Firebase
            database.reference.child("users").child(userId).updateChildren(userUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos actualizados correctamente.", Toast.LENGTH_SHORT).show()
                    finish()  // Cierra esta actividad y regresa a MainActivity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar datos: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuario no identificado.", Toast.LENGTH_SHORT).show()
        }
    }
}