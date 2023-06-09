package fr.epf.mm.projetandroid
import MovieAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button
    private var movies: List<Movie> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private var favoriteMovies: MutableList<Movie> = mutableListOf()
    private lateinit var adapter: MovieAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextSearch = findViewById(R.id.editTextSearch)
        buttonSearch = findViewById(R.id.buttonSearch)

        loadFavorites()

        buttonSearch.setOnClickListener {
            val query = editTextSearch.text.toString()
            searchMovies(query)
        }
        val buttonTopRated = findViewById<ImageButton>(R.id.buttonTopRated)
        buttonTopRated.setOnClickListener {
            getTopRatedMovies()
        }
        val PopularButton = findViewById<ImageButton>(R.id.popularButton)
        PopularButton.setOnClickListener {
            GlobalScope.launch {
                getPopularMovies()
            }
        }
    }

    private fun searchMovies(query: String) {
        val apiKey = "0b96ae7ab4d6f3ff74468ec58e787def"
        val service = ApiManager.tmdbApiService

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = service.searchMovies(apiKey, query)
                if (response.isSuccessful) {
                    val searchResult = response.body()
                    val movies = searchResult?.results
                    if (movies != null) {
                        withContext(Dispatchers.Main) {
                            showMovies(movies)
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

    private fun showMovies(movies: List<Movie>) {
        this.movies = movies
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MovieAdapter(movies)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { movie ->
            val intent = Intent(this@MainActivity, DetailsFilmActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            intent.putExtra("favorite_movies", ArrayList(movies.filter { it.isFavorite }))
            startActivity(intent)
        }
    }

    fun showFavorites(view: View) {
        val intent = Intent(this, FavoriteActivity::class.java)
        intent.putParcelableArrayListExtra("favorite_movies", ArrayList(favoriteMovies))
        startActivity(intent)

    }

    fun updateFavoriteMovies() {
        favoriteMovies.clear()
        favoriteMovies.addAll(movies.filter { it.isFavorite })

        handler.post {
            adapter.notifyDataSetChanged()
            saveFavorites()
        }
    }
    private fun saveFavorites() {
        val favoriteIds = favoriteMovies.map { it.id }
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("favoriteIds", favoriteIds.map { it.toString() }.toSet())
        editor.apply()
    }
    private fun loadFavorites() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val favoriteIds = sharedPreferences.getStringSet("favoriteIds", emptySet()) ?: emptySet()
        val favoriteMovieIds = favoriteIds.mapNotNull { it.toIntOrNull() }
        favoriteMovies.addAll(movies.filter { it.id in favoriteMovieIds })
        favoriteMovies.forEach { movie ->
            movie.isFavorite = true
        }
    }

    fun scanQRCode(view: View) {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scannez un code QR")
        integrator.setCameraId(0)
        integrator.initiateScan()
     }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val qrCodeValue = result.contents
            val movieId = qrCodeValue.substringAfterLast("/", "").substringBefore("?") // Extrait l'ID du film entre le dernier "/" et le "?"

            val intent = Intent(this@MainActivity, DetailsFilmActivity::class.java)
            intent.putExtra("movie_id", movieId.toInt()) // Convertir l'ID du film en Int
            startActivity(intent)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getTopRatedMovies() {
        val apiKey = "0b96ae7ab4d6f3ff74468ec58e787def" // Votre clé API TMDB
        val service = ApiManager.tmdbApiService

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = service.getTopRatedMovies(apiKey)
                if (response.isSuccessful) {
                    val topRatedMovies = response.body()?.results
                    if (topRatedMovies != null) {
                        withContext(Dispatchers.Main) {
                            showMovies(topRatedMovies)
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
    private fun getPopularMovies() {
        val apiKey = "0b96ae7ab4d6f3ff74468ec58e787def"
        val service = ApiManager.tmdbApiService

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = service.getPopularMovies(apiKey, 1)
                if (response.isSuccessful) {
                    val PopularMovies = response.body()?.results
                    if (PopularMovies != null) {
                        withContext(Dispatchers.Main) {
                            showMovies(PopularMovies)
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

    private fun getRecommendedMovies() {
        val apiKey = "0b96ae7ab4d6f3ff74468ec58e787def"
        val service = ApiManager.tmdbApiService

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = service.getPopularMovies(apiKey, 1)
                if (response.isSuccessful) {
                    val PopularMovies = response.body()?.results
                    if (PopularMovies != null) {
                        withContext(Dispatchers.Main) {
                            showMovies(PopularMovies)
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



}



