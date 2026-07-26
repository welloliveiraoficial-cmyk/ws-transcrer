package com.welloliveira.wstranscrer.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class StartUploadRequest(val fileName: String, val mimeType: String, val fileSize: Long)
data class StartUploadResponse(val uploadUrl: String)

data class CheckFileRequest(val name: String)
data class GoogleFile(val name: String?, val uri: String?, val mimeType: String?, val state: String?)

data class TranscribeRequest(val fileUri: String, val mimeType: String)
data class TranscribeResponse(val text: String?, val error: String?)

interface BackendApi {
    @POST("/api/start-upload")
    suspend fun startUpload(@Body body: StartUploadRequest): StartUploadResponse

    @POST("/api/check-file")
    suspend fun checkFile(@Body body: CheckFileRequest): GoogleFile

    @POST("/api/transcribe")
    suspend fun transcribe(@Body body: TranscribeRequest): TranscribeResponse

    companion object {
        fun criar(baseUrl: String): BackendApi =
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BackendApi::class.java)
    }
}
