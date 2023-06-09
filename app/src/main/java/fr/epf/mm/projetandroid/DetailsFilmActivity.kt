package fr.epf.mm.projetandroid
import MovieAdapter
import SearchResult
import TmdbApiService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
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
    private lateinit var recommendedMoviesRecyclerView: RecyclerView
    private lateinit var recommendedMoviesAdapter: MovieAdapter
    val movieService: TmdbApiService = ApiManager.create()
    private var recommendedMovies: List<Movie> = emptyList()


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

        val movieId = intent.getIntExtra("movie_id", -1)
        if (movieId != -1) {
            fetchMovieDetails(movieId)
            GlobalScope.launch(Dispatchers.Main) {
                fetchRecommendedMovies(movieId)
            }
        } else {
        }


    }

    private fun fetchMovieDetails(movieId: Int) {
        val apiKey = "0b96ae7ab4d6f3ff74468ec58e787def"
        val service = ApiManager.tmdbApiService

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = service.getMovieDetails(movieId, apiKey)
                if (response.isSuccessful) {
                    val movie = response.body()
                    if (movie != null) {
                        withContext(Dispatchers.Main) {
                            showMovieDetails(movie)
                        }
                    } else {
                    }
                } else {
                    val errorMessage = response.message()
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
        showRecommendedMovies(recommendedMovies)
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

    private fun showRecommendedMovies(recommendedMovies: List<Movie>) {
        /*if (::recommendedMoviesAdapter.isInitialized) {
            recommendedMoviesAdapter.setData(recommendedMovies)
            recommendedMoviesAdapter.notifyDataSetChanged()
        }*/

        this.recommendedMovies = recommendedMovies
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recommendedMoviesAdapter = MovieAdapter(recommendedMovies)
        recyclerView.adapter = recommendedMoviesAdapter
    }
    private suspend fun fetchRecommendedMovies(movieId: Int) {
        try {
            val response: Response<SearchResult> = withContext(Dispatchers.IO) {
                movieService.getRecommendedMovies(movieId, "0b96ae7ab4d6f3ff74468ec58e787def")
            }

            if (response.isSuccessful) {
                val movieResponse = response.body()
                if (movieResponse != null) {
                    val recommendedMovies = movieResponse.results
                    showRecommendedMovies(recommendedMovies)
                }
            } else {
            }
        } catch (e: Exception) {
        }
    }

}
