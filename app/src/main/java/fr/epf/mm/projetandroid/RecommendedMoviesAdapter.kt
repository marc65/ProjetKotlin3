package fr.epf.mm.projetandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.epf.mm.projetandroid.R

class RecommendedMoviesAdapter(private val movies: List<Movie>) : RecyclerView.Adapter<RecommendedMoviesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.movieTitleTextView.text = movie.title
        // Configurez d'autres éléments de l'interface utilisateur pour afficher les détails du film recommandé
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val movieTitleTextView: TextView = itemView.findViewById(R.id.movieTitleTextView)
        // Référencez les autres vues de l'élément de film recommandé ici
    }
}
