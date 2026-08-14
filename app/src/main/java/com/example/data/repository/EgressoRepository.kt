package com.example.data.repository

import com.example.data.local.EgressoDao
import com.example.data.model.EgressoEntity
import com.example.data.remote.FirebaseSyncService
import com.example.util.DeduplicationHelper
import com.example.util.DeduplicationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

data class SyncReport(
    val downloadedCount: Int = 0,
    val uploadedCount: Int = 0,
    val success: Boolean = true,
    val message: String = ""
)

class EgressoRepository(
    private val egressoDao: EgressoDao,
    private val cloudSyncService: FirebaseSyncService = FirebaseSyncService()
) {

    fun searchEgressos(
        query: String,
        curso: String = "",
        status: String = "",
        caixa: String = "",
        anoMin: Int = 0,
        anoMax: Int = 0,
        sortBy: String = "nome_asc"
    ): Flow<List<EgressoEntity>> {
        return egressoDao.searchEgressos(
            query = query.trim(),
            curso = curso,
            status = status,
            caixa = caixa,
            anoMin = anoMin,
            anoMax = anoMax,
            sortBy = sortBy
        )
    }

    val distinctCursos: Flow<List<String>> = egressoDao.getDistinctCursos()
    val distinctCaixas: Flow<List<String>> = egressoDao.getDistinctCaixas()
    val distinctStatus: Flow<List<String>> = egressoDao.getDistinctStatus()

    suspend fun pushSystemConfig(schoolName: String, email: String, password: String, operators: List<String>): Boolean {
        return cloudSyncService.pushSystemConfig(schoolName, email, password, operators)
    }

    suspend fun fetchSystemConfig(): Map<String, Any>? {
        return cloudSyncService.fetchSystemConfig()
    }

    suspend fun updateLocalFromCloud(cloudItems: List<EgressoEntity>) {
        if (cloudItems.isEmpty()) return
        for (cloudItem in cloudItems) {
            val localExisting = egressoDao.getByCodigo(cloudItem.codigo)
            if (localExisting == null) {
                egressoDao.insert(cloudItem)
            } else {
                egressoDao.update(cloudItem.copy(id = localExisting.id))
            }
        }
    }

    fun startRealtimeSync(onUpdateReceived: (Int) -> Unit = {}): com.google.firebase.firestore.ListenerRegistration? {
        return cloudSyncService.startRealtimeListener { cloudItems ->
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                updateLocalFromCloud(cloudItems)
                onUpdateReceived(cloudItems.size)
            }
        }
    }

    suspend fun insertEgressos(egressos: List<EgressoEntity>, userEmail: String? = null) {
        egressoDao.insertAll(egressos)
        // Push batch to Cloud
        cloudSyncService.pushEgressosBatch(egressos, userEmail)
    }

    suspend fun insertEgresso(egresso: EgressoEntity, userEmail: String? = null): Long {
        val insertedId = egressoDao.insert(egresso)
        val withId = if (egresso.id == 0L) egresso.copy(id = insertedId) else egresso
        cloudSyncService.pushEgresso(withId, userEmail)
        return insertedId
    }

    suspend fun updateEgresso(egresso: EgressoEntity, userEmail: String? = null) {
        egressoDao.update(egresso)
        cloudSyncService.pushEgresso(egresso, userEmail)
    }

    suspend fun updateBatchStatus(ids: List<Long>, newStatus: String, userEmail: String? = null): Int {
        if (ids.isEmpty()) return 0
        if (newStatus != "Ativo" && newStatus != "Arquivado Completo") return 0
        egressoDao.updateBatchStatus(ids, newStatus)
        val updatedList = egressoDao.getByIds(ids)
        cloudSyncService.pushEgressosBatch(updatedList, userEmail)
        return updatedList.size
    }

    suspend fun deleteEgresso(egresso: EgressoEntity) {
        egressoDao.delete(egresso)
        cloudSyncService.deleteEgresso(egresso.codigo, egresso.id)
    }

    suspend fun deduplicateAndMergeAll(userEmail: String? = null): DeduplicationResult {
        val allLocal = egressoDao.getAllList()
        if (allLocal.isEmpty()) {
            return DeduplicationResult(0, 0, 0, 0, emptyList())
        }

        val result = DeduplicationHelper.deduplicateAndMerge(allLocal)
        if (result.duplicatesRemovedCount > 0) {
            // Replace local records with the unified records
            egressoDao.deleteAll()
            egressoDao.insertAll(result.mergedEntities)

            // Sync updated merged records with cloud
            cloudSyncService.pushEgressosBatch(result.mergedEntities, userEmail)
        }
        return result
    }

    /**
     * Performs a bidirectional sync with the Cloud database.
     * 1. Pulls all items from Cloud and merges into local Room DB.
     * 2. Pushes local items to Cloud if they don't exist yet in the Cloud.
     */
    suspend fun syncWithCloud(userEmail: String?): SyncReport {
        try {
            // 1. Pull cloud items
            val cloudItems = cloudSyncService.pullAllEgressos()
            var downloaded = 0
            for (cloudItem in cloudItems) {
                val localExisting = egressoDao.getByCodigo(cloudItem.codigo)
                if (localExisting == null) {
                    egressoDao.insert(cloudItem)
                    downloaded++
                } else {
                    // Update local with cloud data preserving local id
                    egressoDao.update(cloudItem.copy(id = localExisting.id))
                }
            }

            // 2. Push any local items to cloud
            val localItems = egressoDao.getAllList()
            var uploaded = 0
            if (localItems.isNotEmpty()) {
                uploaded = cloudSyncService.pushEgressosBatch(localItems, userEmail)
            }

            return SyncReport(
                downloadedCount = downloaded,
                uploadedCount = uploaded,
                success = true,
                message = if (cloudItems.isEmpty() && localItems.isEmpty()) {
                    "Nuvem conectada. Nenhum registro pendente."
                } else {
                    "Sincronização concluída: $downloaded recebidos da nuvem, $uploaded sincronizados."
                }
            )
        } catch (e: Exception) {
            return SyncReport(
                success = false,
                message = "Não foi possível sincronizar com a nuvem no momento (${e.message ?: "Verifique a conexão"})."
            )
        }
    }
}
