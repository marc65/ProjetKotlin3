import fr.epf.mm.projetandroid.MovieDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Response<SearchResult>

    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Response<MovieDetails>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String
    ): Response<SearchResult>

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int
    ): Response<SearchResult>

    @GET("movie/{movie_id}/recommendations")
    fun getRecommendedMovies(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<SearchResult>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): Response<SearchResult>


}