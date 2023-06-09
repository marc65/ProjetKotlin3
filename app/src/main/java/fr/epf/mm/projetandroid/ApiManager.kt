package fr.epf.mm.projetandroid
import TmdbApiService
import com.google.android.gms.common.api.ApiException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiManager {
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tmdbApiService: TmdbApiService by lazy {
        retrofit.create(TmdbApiService::class.java)
    }
    fun create(): TmdbApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(TmdbApiService::class.java)
    }
    suspend fun getRecommendedMovies(movieId: Int, apiKey: String): List<Movie> {
        val service = tmdbApiService
        val response = service.getRecommendedMovies(movieId, apiKey)
        if (response.isSuccessful) {
            return response.body()?.results ?: emptyList()
        } else {
            throw ApiException("Failed to fetch recommended movies")
        }
    }

}
