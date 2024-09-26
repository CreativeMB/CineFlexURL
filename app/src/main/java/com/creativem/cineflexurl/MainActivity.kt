package com.creativem.cineflexurl

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var synopsisEditText: EditText
    private lateinit var imageUrlEditText: EditText
    private lateinit var streamUrlEditText: EditText
    private lateinit var uploadText: TextView

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        titleEditText = findViewById(R.id.titleEditText)
        synopsisEditText = findViewById(R.id.synopsisEditText)
        imageUrlEditText = findViewById(R.id.imageUrlEditText)
        streamUrlEditText = findViewById(R.id.streamUrlEditText)
        uploadText = findViewById(R.id.uploadText)

        uploadText.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val synopsis = synopsisEditText.text.toString().trim()
            val imageUrl = imageUrlEditText.text.toString().trim()
            val streamUrl = streamUrlEditText.text.toString().trim()

            if (title.isEmpty() || synopsis.isEmpty() || imageUrl.isEmpty() || streamUrl.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                uploadMovie(title, synopsis, imageUrl, streamUrl)
            }
        }
    }

    private fun uploadMovie(
        title: String,
        synopsis: String,
        imageUrl: String,
        streamUrl: String
    ) {
        val movie = hashMapOf(
            "title" to title,
            "synopsis" to synopsis,
            "imageUrl" to imageUrl,
            "streamUrl" to streamUrl
        )

        db.collection("movies")
            .add(movie)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos enviados, Abre Cine Flex en tu TV para ver Contenido", Toast.LENGTH_LONG).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al enviar los datos: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun clearFields() {
        titleEditText.text.clear()
        synopsisEditText.text.clear()
        imageUrlEditText.text.clear()
        streamUrlEditText.text.clear()
    }
}
