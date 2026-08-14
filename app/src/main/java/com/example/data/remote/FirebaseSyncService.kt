package com.example.data.remote

import android.util.Log
import com.example.data.model.EgressoEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseSyncService {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseSyncService", "Firebase Firestore unavailable: ${e.message}")
            null
        }
    }

    private fun getCollection() = firestore?.collection("escola_cleuza_vargas_egressos")

    private fun sanitizeDocId(codigo: String, fallbackId: Long): String {
        val clean = codigo.trim()
            .replace("/", "_")
            .replace("\\", "_")
            .replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
        return if (clean.isNotEmpty()) clean else "EG_$fallbackId"
    }

    suspend fun pushEgresso(egresso: EgressoEntity, userEmail: String?): Boolean = withContext(Dispatchers.IO) {
        val collection = getCollection() ?: return@withContext false
        try {
            val docId = sanitizeDocId(egresso.codigo, egresso.id)
            val data = hashMapOf<String, Any>(
                "codigo" to egresso.codigo,
                "nome" to egresso.nome,
                "cpf" to egresso.cpf,
                "rg" to egresso.rg,
                "genero" to egresso.genero,
                "curso" to egresso.curso,
                "anoConclusao" to egresso.anoConclusao,
                "turma" to egresso.turma,
                "statusDocumento" to egresso.statusDocumento,
                "formatoEnvioDigital" to egresso.formatoEnvioDigital,
                "dataEnvioDigital" to egresso.dataEnvioDigital,
                "caixaArquivo" to egresso.caixaArquivo,
                "prateleiraCorredor" to egresso.prateleiraCorredor,
                "pastaProtocolo" to egresso.pastaProtocolo,
                "observacoes" to egresso.observacoes,
                "cadastradoPor" to egresso.cadastradoPor,
                "dataCadastro" to egresso.dataCadastro,
                "lastModified" to System.currentTimeMillis(),
                "lastModifiedBy" to (userEmail ?: egresso.cadastradoPor.ifEmpty { "Operador" })
            )
            collection.document(docId).set(data, SetOptions.merge()).awaitTask()
            true
        } catch (e: Exception) {
            Log.e("FirebaseSyncService", "Error pushing egresso to cloud: ${e.message}")
            false
        }
    }

    suspend fun pushEgressosBatch(egressos: List<EgressoEntity>, userEmail: String?): Int = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext 0
        val collection = getCollection() ?: return@withContext 0
        if (egressos.isEmpty()) return@withContext 0

        var pushedCount = 0
        // Firestore batch has a 500 operations limit
        val chunks = egressos.chunked(400)
        for (chunk in chunks) {
            try {
                val batch = fs.batch()
                for (egresso in chunk) {
                    val docId = sanitizeDocId(egresso.codigo, egresso.id)
                    val docRef = collection.document(docId)
                    val data = hashMapOf<String, Any>(
                        "codigo" to egresso.codigo,
                        "nome" to egresso.nome,
                        "cpf" to egresso.cpf,
                        "rg" to egresso.rg,
                        "genero" to egresso.genero,
                        "curso" to egresso.curso,
                        "anoConclusao" to egresso.anoConclusao,
                        "turma" to egresso.turma,
                        "statusDocumento" to egresso.statusDocumento,
                        "formatoEnvioDigital" to egresso.formatoEnvioDigital,
                        "dataEnvioDigital" to egresso.dataEnvioDigital,
                        "caixaArquivo" to egresso.caixaArquivo,
                        "prateleiraCorredor" to egresso.prateleiraCorredor,
                        "pastaProtocolo" to egresso.pastaProtocolo,
                        "observacoes" to egresso.observacoes,
                        "cadastradoPor" to egresso.cadastradoPor,
                        "dataCadastro" to egresso.dataCadastro,
                        "lastModified" to System.currentTimeMillis(),
                        "lastModifiedBy" to (userEmail ?: egresso.cadastradoPor.ifEmpty { "Operador" })
                    )
                    batch.set(docRef, data, SetOptions.merge())
                }
                batch.commit().awaitTask()
                pushedCount += chunk.size
            } catch (e: Exception) {
                Log.e("FirebaseSyncService", "Error pushing batch to cloud: ${e.message}")
            }
        }
        pushedCount
    }

    suspend fun deleteEgresso(codigo: String, fallbackId: Long = 0): Boolean = withContext(Dispatchers.IO) {
        val collection = getCollection() ?: return@withContext false
        try {
            val docId = sanitizeDocId(codigo, fallbackId)
            collection.document(docId).delete().awaitTask()
            true
        } catch (e: Exception) {
            Log.e("FirebaseSyncService", "Error deleting from cloud: ${e.message}")
            false
        }
    }

    suspend fun pullAllEgressos(): List<EgressoEntity> = withContext(Dispatchers.IO) {
        val collection = getCollection() ?: return@withContext emptyList()
        try {
            val snapshot = collection.get().awaitTask()
            val list = mutableListOf<EgressoEntity>()
            for (doc in snapshot.documents) {
                val item = docToEntity(doc)
                if (item != null) {
                    list.add(item)
                }
            }
            list
        } catch (e: Exception) {
            Log.e("FirebaseSyncService", "Error pulling egressos from cloud: ${e.message}")
            emptyList()
        }
    }

    fun startRealtimeListener(onUpdate: (List<EgressoEntity>) -> Unit): ListenerRegistration? {
        val collection = getCollection() ?: return null
        return try {
            collection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseSyncService", "Realtime listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { docToEntity(it) }
                    onUpdate(list)
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseSyncService", "Failed to start listener: ${e.message}")
            null
        }
    }

    private fun docToEntity(doc: DocumentSnapshot): EgressoEntity? {
        val nome = doc.getString("nome")?.trim() ?: return null
        if (nome.isBlank()) return null

        val codigo = (doc.getString("codigo") ?: doc.getString("sgde") ?: doc.id).trim()

        val ano = when (val v = doc.get("anoConclusao")) {
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: 0
            else -> 0
        }

        val dataCad = when (val v = doc.get("dataCadastro")) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: System.currentTimeMillis()
            else -> System.currentTimeMillis()
        }

        val caixa = (doc.getString("caixaArquivo") ?: doc.getString("caixa") ?: "Caixa 01").trim()
        val prateleira = (doc.getString("prateleiraCorredor") ?: doc.getString("prateleira") ?: "").trim()
        val pasta = (doc.getString("pastaProtocolo") ?: doc.getString("armario") ?: doc.getString("pasta") ?: "").trim()
        val status = (doc.getString("statusDocumento") ?: doc.getString("status") ?: "Arquivado Completo").trim()
        val gen = (doc.getString("genero") ?: doc.getString("sexo") ?: "").trim()

        return EgressoEntity(
            id = 0,
            codigo = if (codigo.isNotEmpty()) codigo else "AUTO_${System.currentTimeMillis()}",
            nome = nome,
            cpf = doc.getString("cpf")?.trim() ?: "",
            rg = doc.getString("rg")?.trim() ?: "",
            genero = gen,
            curso = doc.getString("curso")?.trim() ?: "",
            anoConclusao = ano,
            turma = doc.getString("turma")?.trim() ?: "",
            statusDocumento = if (status.isNotEmpty()) status else "Arquivado Completo",
            formatoEnvioDigital = doc.getString("formatoEnvioDigital")?.trim() ?: "",
            dataEnvioDigital = doc.getString("dataEnvioDigital")?.trim() ?: "",
            caixaArquivo = if (caixa.isNotEmpty()) caixa else "Caixa 01",
            prateleiraCorredor = prateleira,
            pastaProtocolo = pasta,
            observacoes = doc.getString("observacoes")?.trim() ?: "",
            cadastradoPor = (doc.getString("cadastradoPor") ?: doc.getString("lastModifiedBy") ?: "").trim(),
            dataCadastro = dataCad
        )
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWithException(exception)
        }
    }
}
