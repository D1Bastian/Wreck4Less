package com.example.wreck4less.data.repository.api

import com.example.wreck4less.data.model.AdminAuditResponse
import com.example.wreck4less.data.model.AuthTokenResponse
import com.example.wreck4less.data.model.CustomerHistoryResponse
import com.example.wreck4less.data.model.CustomerProfileResponse
import com.example.wreck4less.data.model.DriverRosterResponse
import com.example.wreck4less.data.model.AdminActionResponse
import com.example.wreck4less.data.model.AdminExportResponse
import com.example.wreck4less.data.model.AdminReassignRequest
import com.example.wreck4less.data.model.AdminRateRequest
import com.example.wreck4less.data.model.AdminCancelRequest
import com.example.wreck4less.data.model.DriverHistoryResponse
import com.example.wreck4less.data.model.DriverNearbyResponse
import com.example.wreck4less.data.model.DriverAssignmentResponse
import com.example.wreck4less.data.model.DriverUpdateRequest
import com.example.wreck4less.data.model.DriverUpdateResponse
import com.example.wreck4less.data.model.JobStatusResponse
import com.example.wreck4less.data.model.LoginRequest
import com.example.wreck4less.data.model.ManagerConfirm
import com.example.wreck4less.data.model.ManagerQueueResponse
import com.example.wreck4less.data.model.PaymentFinalize
import com.example.wreck4less.data.model.PaymentResponse
import com.example.wreck4less.data.model.RegisterRequest
import com.example.wreck4less.data.model.WreckResponse
import com.example.wreck4less.data.model.WreckSubmission
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WreckApi {

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body payload: RegisterRequest
    ): Response<AuthTokenResponse>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body payload: LoginRequest
    ): Response<AuthTokenResponse>

    @POST("api/v1/wreck/submit")
    suspend fun submitWreck(
        @Body submission: WreckSubmission
    ): Response<WreckResponse>

    @GET("api/v1/jobs/{job_id}")
    suspend fun getJob(
        @Path("job_id") jobId: String
    ): Response<JobStatusResponse>

    @GET("api/v1/manager/queue")
    suspend fun managerQueue(): Response<ManagerQueueResponse>

    @POST("api/v1/manager/dispatch")
    suspend fun managerDispatch(
        @Body action: ManagerConfirm
    ): Response<Map<String, Any>>

    @POST("api/v1/payment/finalize")
    suspend fun finalizePayment(
        @Body payment: PaymentFinalize
    ): Response<PaymentResponse>

    @GET("api/v1/driver/assignment/{driver_id}")
    suspend fun driverAssignment(
        @Path("driver_id") driverId: String
    ): Response<DriverAssignmentResponse>

    @POST("api/v1/driver/update")
    suspend fun driverUpdate(
        @Body update: DriverUpdateRequest
    ): Response<DriverUpdateResponse>

    @GET("api/v1/admin/fleet/status")
    suspend fun adminAudit(): Response<AdminAuditResponse>

    @GET("api/v1/customer/profile")
    suspend fun customerProfile(): Response<CustomerProfileResponse>

    @GET("api/v1/customer/history")
    suspend fun customerHistory(): Response<CustomerHistoryResponse>

    @GET("api/v1/admin/drivers")
    suspend fun adminDrivers(): Response<DriverRosterResponse>

    @POST("api/v1/admin/jobs/{job_id}/reassign")
    suspend fun adminReassign(
        @Path("job_id") jobId: String,
        @Body payload: AdminReassignRequest
    ): Response<AdminActionResponse>

    @POST("api/v1/admin/jobs/{job_id}/rate")
    suspend fun adminUpdateRate(
        @Path("job_id") jobId: String,
        @Body payload: AdminRateRequest
    ): Response<AdminActionResponse>

    @POST("api/v1/admin/jobs/{job_id}/cancel")
    suspend fun adminCancelJob(
        @Path("job_id") jobId: String,
        @Body payload: AdminCancelRequest
    ): Response<AdminActionResponse>

    @GET("api/v1/admin/export")
    suspend fun adminExport(): Response<AdminExportResponse>

    @GET("api/v1/driver/history")
    suspend fun driverHistory(): Response<DriverHistoryResponse>

    @GET("api/v1/driver/nearby")
    suspend fun driverNearby(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_m") radiusM: Int
    ): Response<DriverNearbyResponse>
}
