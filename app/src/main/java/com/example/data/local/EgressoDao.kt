package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EgressoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EgressoDao {

    @Query("SELECT * FROM egressos ORDER BY nome ASC")
    fun getAllEgressos(): Flow<List<EgressoEntity>>

    @Query("""
        SELECT * FROM egressos 
        WHERE (:query = '' OR nome LIKE '%' || :query || '%' OR codigo LIKE '%' || :query || '%' OR cpf LIKE '%' || :query || '%' OR caixaArquivo LIKE '%' || :query || '%')
        AND (:curso = '' OR curso = :curso)
        AND (:status = '' OR statusDocumento = :status)
        AND (:caixa = '' OR caixaArquivo = :caixa)
        AND (:anoMin = 0 OR anoConclusao >= :anoMin)
        AND (:anoMax = 0 OR anoConclusao <= :anoMax)
        ORDER BY 
            CASE WHEN :sortBy = 'nome_asc' THEN nome END ASC,
            CASE WHEN :sortBy = 'nome_desc' THEN nome END DESC,
            CASE WHEN :sortBy = 'ano_asc' THEN anoConclusao END ASC,
            CASE WHEN :sortBy = 'ano_desc' THEN anoConclusao END DESC,
            CASE WHEN :sortBy = 'codigo' THEN codigo END ASC,
            nome ASC
    """)
    fun searchEgressos(
        query: String,
        curso: String,
        status: String,
        caixa: String,
        anoMin: Int,
        anoMax: Int,
        sortBy: String
    ): Flow<List<EgressoEntity>>

    @Query("SELECT DISTINCT curso FROM egressos WHERE curso IS NOT NULL AND curso != '' ORDER BY curso ASC")
    fun getDistinctCursos(): Flow<List<String>>

    @Query("SELECT DISTINCT caixaArquivo FROM egressos WHERE caixaArquivo IS NOT NULL AND caixaArquivo != '' ORDER BY caixaArquivo ASC")
    fun getDistinctCaixas(): Flow<List<String>>

    @Query("SELECT DISTINCT statusDocumento FROM egressos WHERE statusDocumento IS NOT NULL AND statusDocumento != '' ORDER BY statusDocumento ASC")
    fun getDistinctStatus(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM egressos")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(egressos: List<EgressoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(egresso: EgressoEntity): Long

    @Update
    suspend fun update(egresso: EgressoEntity)

    @Delete
    suspend fun delete(egresso: EgressoEntity)

    @Query("DELETE FROM egressos")
    suspend fun deleteAll()
}
