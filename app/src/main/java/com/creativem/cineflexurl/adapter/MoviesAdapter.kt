package com.creativem.cineflexurl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.creativem.cineflexurl.R
import com.creativem.cineflexurl.modelo.Movie

class MoviesAdapter(
    private val movieList: List<Movie>,
    private val onDeleteClick: (String) -> Unit // Se agrega un lambda para manejar la eliminación
) : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie: Movie = movieList[position]
        holder.titleTextView.text = movie.title // Asigna el título de la película
        holder.contentTextView.text = movie.contenido // Muestra el contenido de la película

        // Configurar el botón de eliminar
        holder.deleteButton.setOnClickListener {
            onDeleteClick(movie.id ?: "") // Llamar a la función para eliminar
        }
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView = itemView.findViewById(R.id.titleTextView) // Nuevo TextView para el título
        var contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        var deleteButton: TextView = itemView.findViewById(R.id.deleteButton) // Botón para eliminar
    }
}