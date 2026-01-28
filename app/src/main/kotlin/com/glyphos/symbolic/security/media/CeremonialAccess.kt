package com.glyphos.symbolic.security.media

import android.util.Log
import com.glyphos.symbolic.core.models.AccessGrant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 1: Ceremonial Access Protocol
 *
 * Formal, time-limited access to sensitive resources.
 * - User makes ceremonial request for resources
 * - Resource owner grants temporary access
 * - Owner can revoke at any time
 * - All access is logged and audited
 * - Permissions automatically expire
 */
class CeremonialAccess {
    companion object {
        private const val TAG = "CeremonialAccess"
    }

    // Access requests pending approval
    internal data class AccessRequest(
        val requestId: String,
        val requesterId: String,
        val ownerId: String,
        val resources: List<String>,
        val reason: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val expiresAt: Long? = null,
        val status: RequestStatus = RequestStatus.PENDING
    )

    enum class RequestStatus {
        PENDING,      // Waiting for approval
        APPROVED,     // Approved by owner
        DENIED,       // Denied by owner
        REVOKED,      // Revoked by owner
        EXPIRED       // Request expired
    }

    // Track requests
    private val requests = mutableMapOf<String, AccessRequest>()
    private val _requestsFlow = MutableStateFlow<List<AccessRequest>>(emptyList())
    internal val pendingRequests: StateFlow<List<AccessRequest>> = _requestsFlow.asStateFlow()

    // Track grants
    private val grants = mutableMapOf<String, AccessGrant>()
    private val _grantsFlow = MutableStateFlow<List<AccessGrant>>(emptyList())
    val activeGrants: StateFlow<List<AccessGrant>> = _grantsFlow.asStateFlow()

    // Audit log
    internal data class AuditEntry(
        val timestamp: Long,
        val action: String,
        val actor: String,
        val target: String,
        val details: String? = null
    )

    private val auditLog = mutableListOf<AuditEntry>()

    /**
     * Create ceremonial access request
     * @param requesterId User requesting access
     * @param ownerId Resource owner
     * @param resources List of resource IDs
     * @param reason Optional reason for request
     * @return Request ID
     */
    suspend fun requestAccess(
        requesterId: String,
        ownerId: String,
        resources: List<String>,
        reason: String? = null,
        expiryMs: Long? = null
    ): String {
        require(requesterId != ownerId) { "Cannot request access from self" }
        require(resources.isNotEmpty()) { "Must request at least one resource" }

        val requestId = "req-${UUID.randomUUID()}"
        val expiresAt = expiryMs?.let { System.currentTimeMillis() + it }

        val request = AccessRequest(
            requestId = requestId,
            requesterId = requesterId,
            ownerId = ownerId,
            resources = resources,
            reason = reason,
            expiresAt = expiresAt
        )

        requests[requestId] = request
        _requestsFlow.value = requests.values.toList()

        logAccess(
            action = "REQUEST_CREATED",
            actor = requesterId,
            target = ownerId,
            details = "Requested access to ${resources.size} resources"
        )

        Log.d(TAG, "Access request created: $requestId from $requesterId to $ownerId")
        return requestId
    }

    /**
     * Get pending requests for a resource owner
     * @param ownerId Owner user ID
     * @return List of request IDs that are pending
     */
    suspend fun getPendingRequestIds(ownerId: String): List<String> {
        return requests.values.filter {
            it.ownerId == ownerId && it.status == RequestStatus.PENDING
        }.map { it.requestId }
    }

    /**
     * Check if a request exists
     * @param requestId Request ID
     * @return true if request exists
     */
    suspend fun requestExists(requestId: String): Boolean {
        return requests.containsKey(requestId)
    }

    /**
     * Accept ceremonial request and grant temporary access
     * @param requestId Request ID to approve
     * @param durationMs Duration of access in milliseconds
     * @return AccessGrant, or null if request not found
     */
    suspend fun acceptRequest(
        requestId: String,
        durationMs: Long = 24 * 60 * 60 * 1000  // Default 24 hours
    ): AccessGrant? {
        val request = requests[requestId] ?: return null

        if (request.status != RequestStatus.PENDING) {
            Log.w(TAG, "Cannot accept non-pending request: $requestId")
            return null
        }

        // Mark request as approved
        requests[requestId] = request.copy(status = RequestStatus.APPROVED)

        // Create access grant
        val grantId = "grant-${UUID.randomUUID()}"
        val expiresAt = System.currentTimeMillis() + durationMs

        val grant = AccessGrant(
            grantId = grantId,
            granteeId = request.requesterId,
            resources = request.resources,
            expiresAt = expiresAt,
            revocable = true
        )

        grants[grantId] = grant
        _grantsFlow.value = grants.values.toList()

        logAccess(
            action = "REQUEST_APPROVED",
            actor = request.ownerId,
            target = request.requesterId,
            details = "Granted ${request.resources.size} resources for ${durationMs}ms"
        )

        Log.d(TAG, "Request approved: $requestId -> $grantId")
        return grant
    }

    /**
     * Deny ceremonial request
     * @param requestId Request ID to deny
     * @param reason Optional reason for denial
     */
    suspend fun denyRequest(requestId: String, reason: String? = null) {
        val request = requests[requestId] ?: return

        requests[requestId] = request.copy(status = RequestStatus.DENIED)

        logAccess(
            action = "REQUEST_DENIED",
            actor = request.ownerId,
            target = request.requesterId,
            details = reason ?: "Request denied"
        )

        Log.d(TAG, "Request denied: $requestId")
    }

    /**
     * Revoke access grant
     * @param grantId Grant ID to revoke
     * @return true if grant was revoked
     */
    suspend fun revokeAccess(grantId: String): Boolean {
        val grant = grants[grantId] ?: return false

        if (!grant.revocable) {
            Log.w(TAG, "Cannot revoke non-revocable grant: $grantId")
            return false
        }

        grants.remove(grantId)
        _grantsFlow.value = grants.values.toList()

        logAccess(
            action = "GRANT_REVOKED",
            actor = "SYSTEM",  // Could be owner
            target = grant.granteeId,
            details = "Revoked access to ${grant.resources.size} resources"
        )

        Log.d(TAG, "Access revoked: $grantId")
        return true
    }

    /**
     * Check if user has access to resource
     * @param userId User to check
     * @param resourceId Resource to access
     * @return true if user has valid grant for resource
     */
    suspend fun hasAccess(userId: String, resourceId: String): Boolean {
        val grant = grants.values.find { grant ->
            grant.granteeId == userId &&
            grant.resources.contains(resourceId) &&
            System.currentTimeMillis() < grant.expiresAt
        }

        return grant != null
    }

    /**
     * Get active grants for a user
     * @param userId User ID
     * @return List of valid access grants
     */
    suspend fun getUserGrants(userId: String): List<AccessGrant> {
        val now = System.currentTimeMillis()
        return grants.values.filter {
            it.granteeId == userId && now < it.expiresAt
        }
    }

    /**
     * Get all grants given by a user (owner perspective)
     * @param ownerId Owner user ID
     * @return List of grants given by this owner
     */
    suspend fun getGrantsGivenBy(ownerId: String): List<AccessGrant> {
        // In production, grants would track issuer
        return grants.values.toList()
    }

    /**
     * Get time remaining for grant
     * @param grantId Grant ID
     * @return Milliseconds until expiry, or 0 if expired
     */
    suspend fun getGrantTimeRemaining(grantId: String): Long {
        val grant = grants[grantId] ?: return 0
        val remaining = grant.expiresAt - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }

    /**
     * Extend grant duration
     * @param grantId Grant ID
     * @param additionalMs Additional milliseconds
     * @return New expiry time, or null if grant not found
     */
    suspend fun extendGrant(grantId: String, additionalMs: Long): Long? {
        val grant = grants[grantId] ?: return null
        val newExpiryTime = grant.expiresAt + additionalMs

        grants[grantId] = grant.copy(expiresAt = newExpiryTime)

        logAccess(
            action = "GRANT_EXTENDED",
            actor = "SYSTEM",
            target = grant.granteeId,
            details = "Extended access by ${additionalMs}ms"
        )

        return newExpiryTime
    }

    /**
     * Cleanup expired grants and requests
     * @return Number of items removed
     */
    suspend fun cleanupExpired(): Int {
        val now = System.currentTimeMillis()

        // Remove expired grants
        val expiredGrants = grants.filter { (_, grant) -> now > grant.expiresAt }.keys
        expiredGrants.forEach { grantId ->
            grants.remove(grantId)
        }

        // Mark expired requests
        requests.forEach { (id, request) ->
            if (request.expiresAt != null && now > request.expiresAt) {
                requests[id] = request.copy(status = RequestStatus.EXPIRED)
            }
        }

        _grantsFlow.value = grants.values.toList()
        _requestsFlow.value = requests.values.toList()

        return expiredGrants.size
    }

    /**
     * Log access event for audit trail
     * @param action Action performed
     * @param actor User performing action
     * @param target Target of action
     * @param details Additional details
     */
    private fun logAccess(
        action: String,
        actor: String,
        target: String,
        details: String? = null
    ) {
        auditLog.add(
            AuditEntry(
                timestamp = System.currentTimeMillis(),
                action = action,
                actor = actor,
                target = target,
                details = details
            )
        )

        Log.d(TAG, "Audit: $action by $actor on $target")
    }

    /**
     * Get audit log count
     * @return Number of audit entries
     */
    suspend fun getAuditLogSize(): Int {
        return auditLog.size
    }

    /**
     * Check if target has audit entries
     * @param targetId Target resource or user
     * @return true if entries exist for target
     */
    suspend fun hasAuditEntriesForTarget(targetId: String): Boolean {
        return auditLog.any { it.target == targetId }
    }

    /**
     * Get access statistics
     * @return Statistics summary
     */
    suspend fun getStatistics(): AccessStatistics {
        val now = System.currentTimeMillis()
        val activeGrants = grants.count { (_, grant) -> now < grant.expiresAt }
        val expiredGrants = grants.size - activeGrants
        val pendingRequests = requests.count { (_, req) -> req.status == RequestStatus.PENDING }

        return AccessStatistics(
            totalGrants = grants.size,
            activeGrants = activeGrants,
            expiredGrants = expiredGrants,
            totalRequests = requests.size,
            pendingRequests = pendingRequests,
            auditLogEntries = auditLog.size
        )
    }

    /**
     * Get status for logging/display
     * @return Status string
     */
    suspend fun getStatus(): String {
        val stats = getStatistics()
        return """
        Ceremonial Access Status:
        - Total grants: ${stats.totalGrants}
        - Active grants: ${stats.activeGrants}
        - Expired grants: ${stats.expiredGrants}
        - Total requests: ${stats.totalRequests}
        - Pending requests: ${stats.pendingRequests}
        - Audit entries: ${stats.auditLogEntries}
        """.trimIndent()
    }
}

/**
 * Access statistics
 */
data class AccessStatistics(
    val totalGrants: Int,
    val activeGrants: Int,
    val expiredGrants: Int,
    val totalRequests: Int,
    val pendingRequests: Int,
    val auditLogEntries: Int
)
