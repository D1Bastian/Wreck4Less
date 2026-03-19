package com.example.wreck4less.data.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val role: String? = null,
    val bootstrap_token: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthTokenResponse(
    val token: String,
    val role: String,
    val user_id: String,
    val full_name: String,
    val expires_in_minutes: Int
)

data class JobStatusResponse(
    val job_id: String,
    val status: String,
    val user_id: String,
    val intel: WreckIntel,
    val created_at: String,
    val confirmed_rate: Double,
    val driver_id: String?,
    val driver_location: DriverLocation?,
    val cancel_reason: String? = null
)

data class DriverLocation(
    val lat: Double,
    val lng: Double
)

data class ManagerQueueResponse(
    val jobs: List<JobStatusResponse>
)

data class ManagerConfirm(
    val job_id: String,
    val assigned_driver_id: String,
    val approved_rate: Double
)

data class PaymentFinalize(
    val job_id: String,
    val method: String,
    val amount: Double
)

data class PaymentResponse(
    val status: String,
    val tracking_active: Boolean,
    val comm_options: List<String>
)

data class DriverAssignmentResponse(
    val job: JobStatusResponse?
)

data class DriverUpdateRequest(
    val job_id: String,
    val status: String,
    val lat: Double? = null,
    val lng: Double? = null
)

data class DriverUpdateResponse(
    val status: String
)

data class AdminAuditResponse(
    val active_dispatches: Int,
    val registry: Map<String, JobStatusResponse>
)

data class CustomerProfileResponse(
    val user_id: String,
    val email: String,
    val full_name: String,
    val role: String
)

data class CustomerHistoryResponse(
    val jobs: List<JobStatusResponse>
)

data class DriverRosterEntry(
    val id: String,
    val email: String,
    val full_name: String,
    val online: Boolean,
    val active_job_id: String?
)

data class DriverRosterResponse(
    val drivers: List<DriverRosterEntry>
)

data class AdminActionResponse(
    val status: String
)

data class AdminExportResponse(
    val csv: String
)

data class AdminReassignRequest(
    val driver_id: String
)

data class AdminRateRequest(
    val approved_rate: Double
)

data class AdminCancelRequest(
    val reason: String? = null
)

data class DriverHistoryResponse(
    val jobs: List<JobStatusResponse>
)

data class DriverNearbyResponse(
    val jobs: List<JobStatusResponse>
)
