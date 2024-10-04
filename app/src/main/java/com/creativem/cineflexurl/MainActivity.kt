package com.creativem.cineflexurl

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.net.Uri
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import java.io.IOException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
class MainActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var synopsisEditText: EditText
    private lateinit var imageUrlEditText: EditText
    private lateinit var streamUrlEditText: EditText
    private lateinit var uploadText: TextView
    private lateinit var openWebsiteTextView: TextView
    private lateinit var previewImageView: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Activar modo de pantalla completa
        setContentView(R.layout.activity_main)

        // Inicializar los componentes de la interfaz
        titleEditText = findViewById(R.id.titleEditText)
        synopsisEditText = findViewById(R.id.synopsisEditText)
        imageUrlEditText = findViewById(R.id.imageUrlEditText)
        streamUrlEditText = findViewById(R.id.streamUrlEditText)
        uploadText = findViewById(R.id.uploadText)
        openWebsiteTextView = findViewById(R.id.url)
        previewImageView = findViewById(R.id.previewImageView)

        // Validar imagen al perder el foco
        imageUrlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val imageUrl = imageUrlEditText.text.toString().trim()
                if (imageUrl.isEmpty()) {
                    Toast.makeText(this, "El campo de URL de imagen está vacío", Toast.LENGTH_LONG).show()
                    return@setOnFocusChangeListener
                }
                if (isValidUrl(imageUrl)) {
                    loadImage(imageUrl)
                } else {
                    Toast.makeText(this, "URL de imagen inválida", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Validar video al perder el foco
        streamUrlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val streamUrl = streamUrlEditText.text.toString().trim()
                if (streamUrl.isEmpty()) {
                    Toast.makeText(this, "El campo de URL de video está vacío", Toast.LENGTH_LONG).show()
                    return@setOnFocusChangeListener
                }
                if (validateVideoUrl(streamUrl)) {
                    Log.d("MainActivity", "URL de video válida")
                    Toast.makeText(this, "URL de video válida", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "URL de video inválida. Intenta con otra.", Toast.LENGTH_LONG).show()
                    Log.d("MainActivity", "URL de video inválida")
                }
            }
        }

        // Subir los datos al hacer clic en el texto de carga
        uploadText.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val synopsis = synopsisEditText.text.toString().trim()
            val imageUrl = imageUrlEditText.text.toString().trim()
            val streamUrl = streamUrlEditText.text.toString().trim()

            // Verificar si todos los campos están completos
            if (title.isEmpty() || synopsis.isEmpty() || imageUrl.isEmpty() || streamUrl.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validar la URL de la imagen antes de continuar
            if (!isValidUrl(imageUrl)) {
                Toast.makeText(this, "URL de imagen inválida", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validar la URL del video antes de subir
            if (validateVideoUrl(streamUrl)) {
                Log.d("MainActivity", "URL de video válida")
                uploadMovie(title, synopsis, imageUrl, streamUrl)
            } else {
                Toast.makeText(this, "URL de video inválida. Intenta con otra.", Toast.LENGTH_LONG).show()
            }
        }

        // Abrir la página web al hacer clic
        openWebsiteTextView.setOnClickListener {
            openWebPage("https://castr.com/hlsplayer/")
        }
    }

    // Validar si la URL es válida (sintácticamente correcta)
    private fun isValidUrl(url: String): Boolean {
        return try {
            Uri.parse(url)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Cargar imagen con Glide
    private fun loadImage(url: String) {
        Glide.with(this)
            .load(url)
            .error(R.drawable.ic_error) // Agrega un ícono de error si no se puede cargar la imagen
            .into(previewImageView)
    }

    private fun validateVideoUrl(url: String?): Boolean {
        // Verifica si el URL es nulo o vacío
        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "Por favor ingrese una URL válida.", Toast.LENGTH_LONG).show()
            return false
        }

        // Verifica si el URL tiene un esquema válido
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "La URL debe comenzar con 'http://' o 'https://'.", Toast.LENGTH_LONG).show()
            return false
        }

        // Intenta construir la URL con OkHttp
        return try {
            url.toHttpUrlOrNull()?.let {
                true // Si se construye exitosamente, la URL es válida
            } ?: run {
                Toast.makeText(this, "La URL ingresada no es válida.", Toast.LENGTH_LONG).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(this, "La URL ingresada no es válida.", Toast.LENGTH_LONG).show()
            false
        }
    }

    // Subir los datos a Firestore
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
                Toast.makeText(this, "Error al enviar los datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Limpiar campos luego de subir
    private fun clearFields() {
        titleEditText.text.clear()
        synopsisEditText.text.clear()
        imageUrlEditText.text.clear()
        streamUrlEditText.text.clear()
        previewImageView.setImageDrawable(null) // Limpia la imagen previa
    }

    // Abrir página web
    private fun openWebPage(url: String) {
        Log.d("MainActivity", "Intentando abrir la URL: $url") // Para depuración
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)

        // Agregando un verificador para asegurarnos que existe un paquete que pueda manejar el intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se puede abrir la página: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}

