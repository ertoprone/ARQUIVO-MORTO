package com.example.data.repository

import com.example.data.local.EgressoDao
import com.example.data.model.EgressoEntity
import com.example.util.SampleDataGenerator
import kotlinx.coroutines.flow.Flow

class EgressoRepository(private val egressoDao: EgressoDao) {

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

    suspend fun checkAndPreloadSampleData() {
        if (egressoDao.getCount() == 0) {
            egressoDao.insertAll(SampleDataGenerator.getSampleEgressos())
        }
    }

    suspend fun loadSampleData() {
        egressoDao.insertAll(SampleDataGenerator.getSampleEgressos())
    }

    suspend fun insertEgressos(egressos: List<EgressoEntity>) {
        egressoDao.insertAll(egressos)
    }

    suspend fun insertEgresso(egresso: EgressoEntity): Long {
        return egressoDao.insert(egresso)
    }

    suspend fun updateEgresso(egresso: EgressoEntity) {
        egressoDao.update(egresso)
    }

    suspend fun deleteEgresso(egresso: EgressoEntity) {
        egressoDao.delete(egresso)
    }

    suspend fun clearAll() {
        egressoDao.deleteAll()
    }
}
