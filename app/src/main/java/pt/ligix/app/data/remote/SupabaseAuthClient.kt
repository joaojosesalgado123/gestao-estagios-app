package pt.ligix.app.data.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import pt.ligix.app.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseAuthClient {
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", Constants.SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val gson = GsonBuilder().create()

    val api: SupabaseAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("${Constants.SUPABASE_URL}/auth/v1/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SupabaseAuthApi::class.java)
    }
}
