package com.example.taller3

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadAvailableUsers()

        val editTextFirstName = findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = findViewById<EditText>(R.id.editTextLastName)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextIdentification = findViewById<EditText>(R.id.editTextIdentification)
        val editTextLat = findViewById<EditText>(R.id.editTextLat)
        val editTextLong = findViewById<EditText>(R.id.editTextLong)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        buttonRegister.setOnClickListener {
            val firstName = editTextFirstName.text.toString().trim()
            val lastName = editTextLastName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val identification = editTextIdentification.text.toString().trim()
            val latitude = editTextLat.text.toString().trim()
            val longitude = editTextLong.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password, firstName, lastName, identification, latitude, longitude)
            } else {
                Toast.makeText(this, "Email o Password no pueden estar vacios", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun loadAvailableUsers() {
        val usersRef = database.reference.child("users").orderByChild("available").equalTo(true)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                snapshot.children.forEach { child ->
                    val user = child.getValue(User::class.java)
                    user?.let { userList.add(it) }
                }
                recyclerView.adapter = UserAdapter(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load users: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun registerUser(email: String, password: String, firstName: String, lastName: String, identification: String, latitude: String, longitude: String) {
        val lat = latitude.toDoubleOrNull() ?: 0.0  // Convierte a Double, o usa 0.0 si falla la conversión
        val lon = longitude.toDoubleOrNull() ?: 0.0
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userDetails = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "identification" to identification,
                        "latitude" to lat,  // Usa Double aquí
                        "longitude" to lon,
                        "available" to true
                    )
                    database.reference.child("users").child(userId).setValue(userDetails)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registro de usuario correcto.", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Fallo al guardar los datos: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Autenticación fallida: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_edit_profile -> {
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_set_available -> {
                setAvailable()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAvailable() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.reference.child("users").child(userId).child("available").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Ahora estás disponible.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al intentar establecer disponible.", Toast.LENGTH_SHORT).show()
                }
        }
    }

}

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val identification: String = "",
    val latitude: Double = 0.0,  // Asegúrate de que estos valores sean realmente `Double` en Firebase
    val longitude: Double = 0.0, // y no estén almacenados como `String`.
    val available: Boolean = false
)
class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewEmail: TextView = itemView.findViewById(R.id.textViewEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.textViewName.text = "${user.firstName} ${user.lastName}"
        holder.textViewEmail.text = user.email
    }

    override fun getItemCount() = userList.size
}