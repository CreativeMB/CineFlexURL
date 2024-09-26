package com.creativem.cineflexurl
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
class MainActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var synopsisEditText: EditText
    private lateinit var imageUrlEditText: EditText
    private lateinit var streamUrlEditText: EditText
    private lateinit var uploadText: TextView
    private lateinit var openWebsiteTextView: TextView

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
        openWebsiteTextView = findViewById(R.id.url)

        uploadText.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val synopsis = synopsisEditText.text.toString().trim()
            val imageUrl = imageUrlEditText.text.toString().trim()
            val streamUrl = streamUrlEditText.text.toString().trim()

            if (title.isEmpty() || synopsis.isEmpty() || imageUrl.isEmpty() || streamUrl.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            } else if (!isValidUrl(imageUrl) || !isValidUrl(streamUrl)) {
                Toast.makeText(this, "Las URLs no son válidas", Toast.LENGTH_SHORT).show()
            } else {
                uploadMovie(title, synopsis, imageUrl, streamUrl)
            }
        }

        openWebsiteTextView.setOnClickListener {
            openWebPage("https://castr.com/hlsplayer/")
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            Uri.parse(url)
            true
        } catch (e: Exception) {
            false
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
            "streamUrl" to streamUrl,
            "createdAt" to Timestamp.now() // Agregar la fecha de creación
        )

        db.collection("movies")
            .add(movie)
            .addOnSuccessListener {
                Toast.makeText(this, "Película subida correctamente", Toast.LENGTH_LONG).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al enviar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        titleEditText.text.clear()
        synopsisEditText.text.clear()
        imageUrlEditText.text.clear()
        streamUrlEditText.text.clear()
    }

    private fun openWebPage(url: String) {
        Log.d("MainActivity", "Intentando abrir la URL: $url") // Para depuración
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)

        // Agregando un verificador para asegurarnos que existe un paquete que pueda manejar el intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se puede abrir la página: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}