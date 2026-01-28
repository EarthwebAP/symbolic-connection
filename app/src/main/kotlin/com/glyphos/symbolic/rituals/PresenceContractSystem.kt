package com.glyphos.symbolic.rituals

import android.util.Log
import com.glyphos.symbolic.core.models.PresenceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 7: Presence Contract System
 *
 * Agreements that are binding when presence conditions are met.
 * - Presence-bound contracts
 * - Conditional agreement activation
 * - Multi-party contracts
 * - Contract fulfillment verification
 */
class PresenceContractSystem {
    companion object {
        private const val TAG = "PresenceContractSystem"
    }

    private val _activeContracts = MutableStateFlow<List<PresenceContract>>(emptyList())
    val activeContracts: StateFlow<List<PresenceContract>> = _activeContracts.asStateFlow()

    private val _contractHistory = MutableStateFlow<List<ContractEvent>>(emptyList())
    val contractHistory: StateFlow<List<ContractEvent>> = _contractHistory.asStateFlow()

    data class PresenceContract(
        val id: String = UUID.randomUUID().toString(),
        val title: String,
        val description: String,
        val initiator: String,
        val parties: List<String>,
        val terms: String,
        val requiredPresence: PresenceState,
        val createdAt: Long = System.currentTimeMillis(),
        val activatedAt: Long? = null,
        val expiredAt: Long? = null,
        val status: ContractStatus = ContractStatus.PENDING,
        val signatures: Map<String, Long> = emptyMap() // PartyId -> SignatureTime
    )

    enum class ContractStatus {
        PENDING,        // Waiting for parties
        ACTIVE,         // All parties present and presence matches
        SUSPENDED,      // Presence requirements no longer met
        COMPLETED,      // Contract fulfilled
        CANCELLED,      // Cancelled by parties
        EXPIRED         // Contract expired
    }

    data class ContractEvent(
        val id: String = UUID.randomUUID().toString(),
        val contractId: String,
        val timestamp: Long = System.currentTimeMillis(),
        val eventType: String, // "CREATED", "ACTIVATED", "SIGNED", "SUSPENDED", "COMPLETED"
        val party: String? = null,
        val details: String? = null
    )

    suspend fun createContract(
        title: String,
        description: String,
        initiator: String,
        parties: List<String>,
        terms: String,
        requiredPresence: PresenceState
    ): PresenceContract {
        val contract = PresenceContract(
            title = title,
            description = description,
            initiator = initiator,
            parties = parties,
            terms = terms,
            requiredPresence = requiredPresence
        )

        _activeContracts.value = _activeContracts.value + contract

        logContractEvent(
            ContractEvent(
                contractId = contract.id,
                eventType = "CREATED",
                party = initiator
            )
        )

        Log.d(TAG, "Contract created: $title (${contract.id})")
        return contract
    }

    suspend fun signContract(contractId: String, party: String): Boolean {
        val contract = _activeContracts.value.firstOrNull { it.id == contractId }
        if (contract == null) {
            Log.w(TAG, "Contract not found: $contractId")
            return false
        }

        if (!contract.parties.contains(party)) {
            Log.w(TAG, "Party not authorized to sign: $party")
            return false
        }

        val updatedSignatures = contract.signatures.toMutableMap()
        updatedSignatures[party] = System.currentTimeMillis()

        val newStatus = if (updatedSignatures.size == contract.parties.size) {
            ContractStatus.ACTIVE
        } else {
            contract.status
        }

        _activeContracts.value = _activeContracts.value.map { c ->
            if (c.id == contractId) {
                c.copy(
                    signatures = updatedSignatures,
                    status = newStatus,
                    activatedAt = if (newStatus == ContractStatus.ACTIVE) System.currentTimeMillis() else c.activatedAt
                )
            } else {
                c
            }
        }

        logContractEvent(
            ContractEvent(
                contractId = contractId,
                eventType = "SIGNED",
                party = party
            )
        )

        Log.d(TAG, "Contract signed by $party: $contractId")
        return true
    }

    suspend fun activateContractOnPresence(
        contractId: String,
        currentPresence: PresenceState
    ): Boolean {
        val contract = _activeContracts.value.firstOrNull { it.id == contractId }
        if (contract == null || contract.status != ContractStatus.PENDING) {
            return false
        }

        if (!currentPresence.matches(contract.requiredPresence)) {
            Log.d(TAG, "Presence does not match contract requirements")
            return false
        }

        _activeContracts.value = _activeContracts.value.map { c ->
            if (c.id == contractId) {
                c.copy(
                    status = ContractStatus.ACTIVE,
                    activatedAt = System.currentTimeMillis()
                )
            } else {
                c
            }
        }

        logContractEvent(
            ContractEvent(
                contractId = contractId,
                eventType = "ACTIVATED",
                details = "Presence requirements met"
            )
        )

        Log.d(TAG, "Contract activated by presence match: $contractId")
        return true
    }

    suspend fun suspendOnPresenceMismatch(
        contractId: String,
        currentPresence: PresenceState
    ) {
        val contract = _activeContracts.value.firstOrNull { it.id == contractId }
        if (contract == null || contract.status != ContractStatus.ACTIVE) {
            return
        }

        if (!currentPresence.matches(contract.requiredPresence)) {
            _activeContracts.value = _activeContracts.value.map { c ->
                if (c.id == contractId) {
                    c.copy(status = ContractStatus.SUSPENDED)
                } else {
                    c
                }
            }

            logContractEvent(
                ContractEvent(
                    contractId = contractId,
                    eventType = "SUSPENDED",
                    details = "Presence requirements no longer met"
                )
            )

            Log.d(TAG, "Contract suspended due to presence mismatch: $contractId")
        }
    }

    suspend fun completeContract(contractId: String): Boolean {
        val contract = _activeContracts.value.firstOrNull { it.id == contractId }
        if (contract == null) return false

        _activeContracts.value = _activeContracts.value.map { c ->
            if (c.id == contractId) {
                c.copy(
                    status = ContractStatus.COMPLETED,
                    expiredAt = System.currentTimeMillis()
                )
            } else {
                c
            }
        }

        logContractEvent(
            ContractEvent(
                contractId = contractId,
                eventType = "COMPLETED"
            )
        )

        Log.d(TAG, "Contract completed: $contractId")
        return true
    }

    fun getActiveContracts(party: String? = null): List<PresenceContract> {
        return _activeContracts.value.filter {
            it.status == ContractStatus.ACTIVE &&
            (party == null || it.parties.contains(party))
        }
    }

    fun getContractHistory(contractId: String): List<ContractEvent> {
        return _contractHistory.value.filter { it.contractId == contractId }
    }

    private suspend fun logContractEvent(event: ContractEvent) {
        _contractHistory.value = _contractHistory.value + event
    }

    fun getStatistics(): PresenceContractStatistics {
        val contracts = _activeContracts.value
        return PresenceContractStatistics(
            totalContracts = contracts.size,
            activeContracts = contracts.count { it.status == ContractStatus.ACTIVE },
            suspendedContracts = contracts.count { it.status == ContractStatus.SUSPENDED },
            completedContracts = contracts.count { it.status == ContractStatus.COMPLETED },
            totalEvents = _contractHistory.value.size
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Presence Contract System Status:
        - Total contracts: ${stats.totalContracts}
        - Active: ${stats.activeContracts}
        - Suspended: ${stats.suspendedContracts}
        - Completed: ${stats.completedContracts}
        - Total events: ${stats.totalEvents}
        """.trimIndent()
    }
}

data class PresenceContractStatistics(
    val totalContracts: Int,
    val activeContracts: Int,
    val suspendedContracts: Int,
    val completedContracts: Int,
    val totalEvents: Int
)
