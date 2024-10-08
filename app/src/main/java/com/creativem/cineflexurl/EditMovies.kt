package com.creativem.cineflexurl.dialogFragment
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.creativem.cineflexurl.R
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class EditMovies : DialogFragment() {
    private lateinit var titleEditText: EditText
    private lateinit var synopsisEditText: EditText
    private lateinit var imageUrlEditText: EditText
    private lateinit var streamUrlEditText: EditText
    private lateinit var uploadText: TextView
    private lateinit var previewImageView: ImageView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var pedidosImageView: ImageView
    private lateinit var noviesImageView: ImageView
        private var movieId: String? = null

    companion object {
        fun newInstance(movieId: String): EditMovies {
            val fragment = EditMovies()
            val args = Bundle()
            args.putString("movieId", movieId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog) // Asegúrate de tener este estilo definido
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asegúrate de tener un layout específico para el diálogo
        return inflater.inflate(R.layout.nuevamovies, container, false) // Cambia a tu layout de diálogo

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(R.color.white) // Cambia a un color de fondo opaco
            // Ocultar la barra de estado
            dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

            // Establecer otras configuraciones aquí si es necesario
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener los componentes del layout
        titleEditText = view.findViewById(R.id.titleEditText)
        synopsisEditText = view.findViewById(R.id.synopsisEditText)
        imageUrlEditText = view.findViewById(R.id.imageUrlEditText)
        streamUrlEditText = view.findViewById(R.id.streamUrlEditText)
        uploadText = view.findViewById(R.id.uploadText)
        previewImageView = view.findViewById(R.id.previewImageView)
        pedidosImageView = view.findViewById(R.id.pedidos);
        noviesImageView = view.findViewById(R.id.movies);
        pedidosImageView.setVisibility(View.GONE); // O View.INVISIBLE
        noviesImageView.setVisibility(View.GONE); // O View.INVISIBLE
        // Obtener el ID de la película
        movieId = arguments?.getString("movieId")

        // Cargar los datos de la película
        loadMovieData()

        // Validar imagen al perder el foco
        imageUrlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val imageUrl = imageUrlEditText.text.toString().trim()
                if (isValidUrl(imageUrl)) {
                    loadImage(imageUrl)
                } else {
                    Toast.makeText(context, "URL de imagen inválida", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Validar video al perder el foco
        streamUrlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val streamUrl = streamUrlEditText.text.toString().trim()
                if (!validateVideoUrl(streamUrl)) {
                    Toast.makeText(context, "URL de video inválida. Intenta con otra.", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Configurar el botón de guardar
        uploadText.setOnClickListener {
            saveMovie()
        }
    }

    // Cargar los datos de la película
    private fun loadMovieData() {
        movieId?.let { id ->
            db.collection("movies").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Asigna los valores de Firestore a los EditText
                        titleEditText.setText(document.getString("title"))
                        synopsisEditText.setText(document.getString("synopsis"))
                        imageUrlEditText.setText(document.getString("imageUrl"))
                        streamUrlEditText.setText(document.getString("streamUrl"))
                        loadImage(document.getString("imageUrl") ?: "")
                    } else {
                        Toast.makeText(context, "No se encontró la película", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al cargar los datos de la película", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Cargar imagen con Glide
    private fun loadImage(url: String) {
        Glide.with(this)
            .load(url)
            .error(R.drawable.ic_error) // Agrega un ícono de error si no se puede cargar la imagen
            .into(previewImageView)
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

    // Validar URL de video
    private fun validateVideoUrl(url: String?): Boolean {
        // Verifica si el URL es nulo o vacío
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "Por favor ingrese una URL válida.", Toast.LENGTH_LONG).show()
            return false
        }

        // Verifica si el URL tiene un esquema válido
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(context, "La URL debe comenzar con 'http://' o 'https://'.", Toast.LENGTH_LONG).show()
            return false
        }

        // Intenta construir la URL con OkHttp
        return try {
            url.toHttpUrlOrNull()?.let {
                true // Si se construye exitosamente, la URL es válida
            } ?: run {
                Toast.makeText(context, "La URL ingresada no es válida.", Toast.LENGTH_LONG).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "La URL ingresada no es válida.", Toast.LENGTH_LONG).show()
            false
        }
    }

    // Guardar los cambios de la película
    private fun saveMovie() {
        val title = titleEditText.text.toString()
        val synopsis = synopsisEditText.text.toString()
        val imageUrl = imageUrlEditText.text.toString()
        val streamUrl = streamUrlEditText.text.toString()

        if (title.isEmpty() || synopsis.isEmpty() || imageUrl.isEmpty() || streamUrl.isEmpty()) {
            Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
            return
        }

        // Cambia la creación del hashMap a MutableMap<String, Any>
        val movieData: MutableMap<String, Any> = hashMapOf(
            "title" to title,
            "synopsis" to synopsis,
            "imageUrl" to imageUrl,
            "streamUrl" to streamUrl
        )

        // Guardar datos en Firestore
        movieId?.let { id ->
            db.collection("movies").document(id).set(movieData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Película actualizada correctamente", Toast.LENGTH_LONG).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al actualizar la película: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}