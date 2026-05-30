package pt.ligix.app.data.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pt.ligix.app.util.Constants
import pt.ligix.app.util.SessionTokenProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .addHeader("apikey", Constants.SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")

            SessionTokenProvider.accessToken?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .build()

    // Gson que NÃO serializa campos nulos
    private val gson = GsonBuilder()
        .create()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl("${Constants.SUPABASE_URL}/rest/v1/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SupabaseApi::class.java)
    }
}
