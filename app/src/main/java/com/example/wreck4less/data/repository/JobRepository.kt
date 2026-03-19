package com.example.wreck4less.data.repository

import com.example.wreck4less.data.model.*
import com.example.wreck4less.data.repository.api.OverpassInstance
import com.example.wreck4less.data.repository.api.RetrofitInstance
import com.example.wreck4less.data.repository.model.OverpassElement
import retrofit2.Response

data class GasStation(
    val id: Long,
    val name: String,
    val lat: Double,
    val lon: Double
)

class JobRepository {

    suspend fun register(payload: RegisterRequest): Response<AuthTokenResponse> {
        return RetrofitInstance.api.register(payload)
    }

    suspend fun login(payload: LoginRequest): Response<AuthTokenResponse> {
        return RetrofitInstance.api.login(payload)
    }

    suspend fun submitWreck(submission: WreckSubmission): Response<WreckResponse> {
        return RetrofitInstance.api.submitWreck(submission)
    }

    suspend fun getJob(jobId: String): Response<JobStatusResponse> {
        return RetrofitInstance.api.getJob(jobId)
    }

    suspend fun managerQueue(): Response<ManagerQueueResponse> {
        return RetrofitInstance.api.managerQueue()
    }

    suspend fun managerDispatch(action: ManagerConfirm): Response<Map<String, Any>> {
        return RetrofitInstance.api.managerDispatch(action)
    }

    suspend fun finalizePayment(payment: PaymentFinalize): Response<PaymentResponse> {
        return RetrofitInstance.api.finalizePayment(payment)
    }

    suspend fun driverAssignment(driverId: String): Response<DriverAssignmentResponse> {
        return RetrofitInstance.api.driverAssignment(driverId)
    }

    suspend fun driverUpdate(update: DriverUpdateRequest): Response<DriverUpdateResponse> {
        return RetrofitInstance.api.driverUpdate(update)
    }

    suspend fun adminAudit(): Response<AdminAuditResponse> {
        return RetrofitInstance.api.adminAudit()
    }

    suspend fun customerProfile(): Response<CustomerProfileResponse> {
        return RetrofitInstance.api.customerProfile()
    }

    suspend fun customerHistory(): Response<CustomerHistoryResponse> {
        return RetrofitInstance.api.customerHistory()
    }

    suspend fun adminDrivers(): Response<DriverRosterResponse> {
        return RetrofitInstance.api.adminDrivers()
    }

    suspend fun adminReassign(jobId: String, payload: AdminReassignRequest): Response<AdminActionResponse> {
        return RetrofitInstance.api.adminReassign(jobId, payload)
    }

    suspend fun adminUpdateRate(jobId: String, payload: AdminRateRequest): Response<AdminActionResponse> {
        return RetrofitInstance.api.adminUpdateRate(jobId, payload)
    }

    suspend fun adminCancelJob(jobId: String, payload: AdminCancelRequest): Response<AdminActionResponse> {
        return RetrofitInstance.api.adminCancelJob(jobId, payload)
    }

    suspend fun adminExport(): Response<AdminExportResponse> {
        return RetrofitInstance.api.adminExport()
    }

    suspend fun findGasStations(lat: Double, lon: Double, radiusMeters: Int): List<GasStation> {
        val query = """
            [out:json];
            node(around:$radiusMeters,$lat,$lon)["amenity"="fuel"];
            out;
        """.trimIndent()

        val response = OverpassInstance.api.search(query)
        return response.elements
            .mapNotNull { element -> element.toGasStationOrNull() }
            .sortedBy { it.name }
    }
}

private fun OverpassElement.toGasStationOrNull(): GasStation? {
    val lat = this.lat ?: return null
    val lon = this.lon ?: return null
    val name = this.tags?.get("name") ?: this.tags?.get("brand") ?: "Fuel Station"
    return GasStation(
        id = this.id,
        name = name,
        lat = lat,
        lon = lon
    )
}
