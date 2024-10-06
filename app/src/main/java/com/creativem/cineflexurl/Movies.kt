package com.creativem.cineflexurl

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creativem.cineflexurl.adapter.MoviesAdapter
import com.creativem.cineflexurl.modelo.Movie
import com.google.firebase.firestore.FirebaseFirestore

class Movies : AppCompatActivity() {
    private lateinit var recyclerViewMovies: RecyclerView
    private lateinit var moviesAdapter: MoviesAdapter
    private var movieList: MutableList<Movie> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movies)

        recyclerViewMovies = findViewById(R.id.recyclerViewMovies)
        recyclerViewMovies.layoutManager = LinearLayoutManager(this)

        moviesAdapter = MoviesAdapter(movieList) { movieId ->
            deleteMovie(movieId)
        }
        recyclerViewMovies.adapter = moviesAdapter

        loadMovies()
    }

    private fun loadMovies() {
        db.collection("movies").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val movie = document.toObject(Movie::class.java).copy(id = document.id) // Agregar el ID
                    movieList.add(movie)
                }
                moviesAdapter.notifyDataSetChanged()
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
}