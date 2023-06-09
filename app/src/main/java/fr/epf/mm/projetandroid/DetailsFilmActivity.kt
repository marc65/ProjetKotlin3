package fr.epf.mm.projetandroid

import SearchResult
import TmdbApiService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class DetailsFilmActivity : AppCompatActivity() {

    private lateinit var movieTitleTextView: TextView
    private lateinit var originalLanguageTextView: TextView
    private lateinit var overviewTextView: TextView
    private lateinit var popularityTextView: TextView
    private lateinit var productionCompaniesTextView: TextView
    private lateinit var releaseDateTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var voteAverageTextView: TextView
    private lateinit var posterImageView: ImageView
    private lateinit var recommendedMoviesLayout: LinearLayout
    private lateinit var recommendedMoviesRecyclerView: RecyclerView
    private val movieService: TmdbApiService = ApiManager.create()
    private var recommendedMovies: List<Movie> = emptyList()
    private val apiKey = "0b96ae7ab4d6f3ff74468ec58e787def"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_film)

        movieTitleTextView = findViewById(R.id.movieTitleTextView)
        originalLanguageTextView = findViewById(R.id.originalLanguageTextView)
        overviewTextView = findViewById(R.id.overviewTextView)
        popularityTextView = findViewById(R.id.popularityTextView)
        productionCompaniesTextView = findViewById(R.id.productionCompaniesTextView)
        releaseDateTextView = findViewById(R.id.releaseDateTextView)
        durationTextView = findViewById(R.id.durationTextView)
        voteAverageTextView = findViewById(R.id.voteAverageTextView)
        posterImageView = findViewById(R.id.posterImageView)
        recommendedMoviesRecyclerView = findViewById(R.id.recommendedMoviesRecyclerView)
        recommendedMoviesRecyclerView.layoutManager = LinearLayoutManager(this)

        val movieId = intent.getIntExtra("movie_id", -1)
        if (movieId != -1) {
            fetchMovieDetails(movieId)
            GlobalScope.launch(Dispatchers.Main) {
                fetchRecommendedMovies(movieId)
            }
        } else {
            // Gérer le cas où l'ID du film n'est pas valide
        }
    }

    private fun fetchMovieDetails(movieId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = movieService.getMovieDetails(movieId, apiKey)
                if (response.isSuccessful) {
                    val movie = response.body()
                    if (movie != null) {
                        withContext(Dispatchers.Main) {
                            showMovieDetails(movie)
                        }
                    } else {
                        // Gérer le cas où le film est null
                    }
                } else {
                    val errorMessage = response.message()
                    // Gérer l'erreur de requête
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Gérer l'exception
            }
        }
    }

    private fun showMovieDetails(movie: MovieDetails) {
        movieTitleTextView.text = movie.title
        originalLanguageTextView.text = "Original Language: ${movie.originalLanguage}"
        overviewTextView.text = "Overview: ${movie.overview}"
        popularityTextView.text = "Popularity: ${movie.popularity}"
        productionCompaniesTextView.text = "Production Companies: ${getProductionCompaniesString(movie.productionCompanies)}"
        releaseDateTextView.text = "Release Date: ${movie.releaseDate}"
        durationTextView.text = "Duration: ${movie.runtime} min"
        voteAverageTextView.text = "Vote Average: ${movie.voteAverage}"
        val collection = movie.collection
        val posterPath = collection?.posterPath
        if (posterPath != null) {
            val posterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
            Glide.with(this@DetailsFilmActivity)
                .load(posterUrl)
                .placeholder(R.drawable.placeholder)
                .into(posterImageView)
        } else {
            posterImageView.setImageResource(R.drawable.placeholder)
        }
    }

    private fun getProductionCompaniesString(companies: List<Map<String, String>>): String {
        val stringBuilder = StringBuilder()
        for (company in companies) {
            val companyName = company["name"]
            if (!companyName.isNullOrEmpty()) {
                stringBuilder.append(companyName)
                stringBuilder.append(", ")
            }
        }
        if (stringBuilder.isNotEmpty()) {
            stringBuilder.deleteCharAt(stringBuilder.length - 2)
        }
        return stringBuilder.toString()
    }

    private suspend fun fetchRecommendedMovies(movieId: Int) {
        try {
            val response: Response<SearchResult> = withContext(Dispatchers.IO) {
                movieService.getRecommendedMovies(movieId, apiKey)
            }

            if (response.isSuccessful) {
                val movieResponse = response.body()
                if (movieResponse != null) {
                    val recommendedMovies = movieResponse.results
                    showRecommendedMovies(recommendedMovies)
                }
            } else {
                // Gérer l'erreur de requête
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Gérer l'exception
        }
    }

    /*private fun showRecommendedMovies(recommendedMovies: List<Movie>) {
        val adapter = RecommendedMoviesAdapter(recommendedMovies)
        recommendedMoviesRecyclerView.adapter = adapter

        for (movie in recommendedMovies) {
            val movieView = LayoutInflater.from(this).inflate(R.layout.movie_item, recommendedMoviesLayout, false)

            val movieTitleTextView = movieView.findViewById<TextView>(R.id.titleTextView)
            movieTitleTextView.text = movie.title

            recommendedMoviesLayout.addView(movieView)
        }
    }*/

    private fun showRecommendedMovies(recommendedMovies: List<Movie>) {
        val adapter = RecommendedMoviesAdapter(recommendedMovies)
        recommendedMoviesRecyclerView.adapter = adapter

        recommendedMoviesLayout.removeAllViews() // Supprimer les vues précédentes (au cas où)

        for (movie in recommendedMovies) {
            val movieView = LayoutInflater.from(this).inflate(R.layout.movie_item, recommendedMoviesLayout, false)

            val movieTitleTextView = movieView.findViewById<TextView>(R.id.titleTextView)
            movieTitleTextView.text = movie.title

            recommendedMoviesLayout.addView(movieView)
        }
    }



}
