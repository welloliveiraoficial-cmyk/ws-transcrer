package com.welloliveira.wstranscrer.update

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class VersaoRemota(
    val versionCode: Int,
    val versionName: String,
    val changelog: List<String> = emptyList(),
    val apkUrl: String
)

interface VersionApi {
    @GET("version.json")
    suspend fun buscarVersao(): VersaoRemota
}

class UpdateChecker(baseUrl: String) {
    private val api: VersionApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(VersionApi::class.java)

    suspend fun verificar(versionCodeInstalado: Int): VersaoRemota? = withContext(Dispatchers.IO) {
        try {
            val remota = api.buscarVersao()
            if (remota.versionCode > versionCodeInstalado) remota else null
        } catch (e: Exception) {
            null
        }
    }
}
