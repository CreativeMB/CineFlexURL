package com.creativem.cineflexurl

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creativem.cineflexurl.adapter.MoviesAdapter
import com.creativem.cineflexurl.dialogFragment.EditMovies
import com.creativem.cineflexurl.modelo.Movie
import com.google.firebase.firestore.FirebaseFirestore

class VerMovies : AppCompatActivity() {
    private lateinit var recyclerViewMovies: RecyclerView
    private lateinit var moviesAdapter: MoviesAdapter
    private var movieList: MutableList<Movie> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vermovies)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        recyclerViewMovies = findViewById(R.id.recyclerViewMovies)
        recyclerViewMovies.layoutManager = LinearLayoutManager(this)

        // Proporcionar ambas funciones para eliminar y editar la película
        moviesAdapter = MoviesAdapter(movieList, { movieId -> deleteMovie(movieId) }, { movieId -> editMovie(movieId) }, true)
        recyclerViewMovies.adapter = moviesAdapter

        loadMovies()
    }

    private fun loadMovies() {
        movieList.clear() // Limpiar la lista antes de agregar nuevas películas
        db.collection("movies").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val movie = document.toObject(Movie::class.java).copy(id = document.id) // Agregar el ID
                    movieList.add(movie)
                }
                moviesAdapter.notifyDataSetChanged() // Notificar al adaptador de cambios
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar las películas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteMovie(movieId: String) {
        db.collection("movies").document(movieId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Película eliminada", Toast.LENGTH_SHORT).show()
                loadMovies() // Recargar películas
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar la película", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editMovie(movieId: String) {

        val dialog = EditMovies.newInstance(movieId)
        dialog.show(supportFragmentManager, "EditMovieDialog")
    }
}