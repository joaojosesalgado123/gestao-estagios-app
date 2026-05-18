package pt.ligix.app.data.remote

import pt.ligix.app.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl("${Constants.SUPABASE_URL}/rest/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}
