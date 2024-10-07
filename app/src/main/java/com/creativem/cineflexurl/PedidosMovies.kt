package com.creativem.cineflexurl
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creativem.cineflexurl.adapter.MoviesAdapter
import com.creativem.cineflexurl.modelo.Movie
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class PedidosMovies : AppCompatActivity() {
    private lateinit var recyclerViewPedidos: RecyclerView
    private lateinit var moviesAdapter: MoviesAdapter
    private var movieList: MutableList<Movie> = mutableListOf()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidosmovies)

        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos)
        recyclerViewPedidos.layoutManager = LinearLayoutManager(this)

        db = FirebaseFirestore.getInstance()

        // Proporcionar la implementación del lambda para onDeleteClick
        moviesAdapter = MoviesAdapter(movieList) { movieId ->
            deleteMovie(movieId)
        }
        recyclerViewPedidos.adapter = moviesAdapter

        loadMovies()
    }

    private fun loadMovies() {
        movieList.clear()
        db.collection("pedidosmovies").get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val movie: Movie = document.toObject(Movie::class.java).copy(id = document.id)
                        movieList.add(movie)
                    }
                    moviesAdapter.notifyDataSetChanged()
                } else {
                    Log.e("PedidosMovies", "Error getting documents: ", task.exception)
                }
            }
            .addOnFailureListener { e ->
                Log.e("PedidosMovies", "Error loading movies", e)
            }
    }

    private fun deleteMovie(movieId: String) {
        db.collection("pedidosmovies").document(movieId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Pedido eliminado", Toast.LENGTH_SHORT).show()
                loadMovies() // Recargar películas después de eliminar
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar la película", Toast.LENGTH_SHORT).show()
                Log.e("PedidosMovies", "Error deleting movie", e)
            }
    }
}