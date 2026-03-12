package com.example.wreck4less.data.repository.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.wreck4less.data.repository.model.TowRequest
import com.example.wreck4less.data.repository.model.JobResponse

interface JobApi {

    @POST("v1/jobs/request")
    suspend fun createJob(
        @Body request: TowRequest
    ): Response<JobResponse>
}
