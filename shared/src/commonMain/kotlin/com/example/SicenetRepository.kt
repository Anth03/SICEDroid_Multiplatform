package com.example.sicedroidmultiplatform

import com.example.sicedroidmultiplatform.data.*
import com.example.sicedroidmultiplatform.database.DataCache
import com.example.sicedroidmultiplatform.network.SicenetApiService
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import kotlinx.serialization.json.*

class SicenetRepository(private val cache: DataCache) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client = HttpClient {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
        expectSuccess = false
    }

    private val apiService = SicenetApiService(client)

    // ── Login ────────────────────────────────────────────────────────────────

    suspend fun login(matricula: String, password: String): AccesoLoginResult {
        val xml = apiService.login(matricula, password)
        val jsonStr = extractFromSoap(xml, "accesoLoginResult")
        return if (jsonStr.isNotEmpty() && jsonStr != "null") {
            runCatching { json.decodeFromString<AccesoLoginResult>(jsonStr) }.getOrDefault(AccesoLoginResult())
        } else AccesoLoginResult()
    }

    // ── Perfil ───────────────────────────────────────────────────────────────

    suspend fun getPerfilAcademico(): ProfileStudent {
        val xml = apiService.getPerfilAcademico()
        val jsonStr = extractFromSoap(xml, "getAlumnoAcademicoWithLineamientoResult")
        val profile = if (jsonStr.isNotEmpty() && jsonStr != "null") {
            runCatching { json.decodeFromString<ProfileStudent>(jsonStr) }.getOrDefault(ProfileStudent())
        } else ProfileStudent()
        if (profile.matricula.isNotEmpty()) cache.saveProfile(profile)
        return profile
    }

    fun getCachedProfile(): ProfileStudent? = cache.getProfile()

    // ── Carga Académica ──────────────────────────────────────────────────────

    fun getCachedCargaAcademica(): List<CargaAcademica> = cache.getCargaAcademica()

    suspend fun getCargaAcademica(): List<CargaAcademica> {
        val xml = apiService.getCargaAcademica()
        val jsonStr = extractFromSoap(xml, "getCargaAcademicaByAlumnoResult")
        if (jsonStr.isEmpty() || jsonStr == "null") return emptyList()
        return runCatching {
            val array = toJsonArray(jsonStr, "lstCarga", "Carga", "carga")
            array.mapNotNull { element ->
                val obj = element.jsonObject
                CargaAcademica(
                    clvOficial = obj.str("clvOficial", "ClvOficial", "ClvMat", "ClvOfiMat", "clave", "Clave"),
                    materia    = obj.str("Materia", "materia", "NomMat", "nombreMateria"),
                    grupo      = obj.str("Grupo", "grupo"),
                    creditos   = obj.int("C", "Creditos", "creditos", "Cdts", "cred"),
                    docente    = obj.str("Docente", "docente", "nomDocente", "NomDocente", "profesor", "Profesor"),
                    observaciones = obj.str("Observaciones", "observaciones", "obs"),
                    estadoMateria = obj.int("EstadoMateria", "estadoMateria", "estado"),
                    semestre   = obj.int("Semestre", "semestre", "SemActual", "semActual", "sem")
                )
            }
        }.getOrDefault(emptyList()).also { list ->
            if (list.isNotEmpty()) cache.saveCargaAcademica(list)
        }
    }

    // ── Kardex ───────────────────────────────────────────────────────────────

    fun getCachedKardex(): List<KardexItem> = cache.getKardex()

    suspend fun getKardex(lineamiento: Int): List<KardexItem> {
        val xml = apiService.getKardexConPromedio(lineamiento)
        val jsonStr = extractFromSoap(xml, "getAllKardexConPromedioByAlumnoResult")
        if (jsonStr.isEmpty() || jsonStr == "null") return emptyList()
        return runCatching {
            val array = toJsonArray(jsonStr, "lstKardex")
            array.mapNotNull { element ->
                val obj = element.jsonObject
                val p1 = obj.str("P1", "P2", "P3", "Periodo")
                val a1 = obj.str("A1", "A2", "A3", "Anio")
                val periodo = listOf(p1, a1).filter { it.isNotBlank() }.joinToString(" ")
                val califStr = obj.str("Calif", "calif").ifBlank {
                    obj.int("Calif", "calif").takeIf { it != 0 }?.toString() ?: ""
                }
                KardexItem(
                    clvOficial   = obj.str("ClvOfiMat", "ClvMat", "clvOficial"),
                    materia      = obj.str("Materia", "materia"),
                    semestre     = obj.int("S1", "S2", "S3", "Semestre", "semestre"),
                    creditos     = obj.int("Cdts", "Creditos", "creditos"),
                    calificacion = califStr,
                    acreditacion = obj.str("Acred", "acreditacion", "acreditado"),
                    periodo      = periodo.trim(),
                    observaciones = obj.str("Observaciones", "observaciones")
                )
            }
        }.getOrDefault(emptyList()).also { list ->
            if (list.isNotEmpty()) cache.saveKardex(list)
        }
    }

    // ── Calificaciones por Unidad ─────────────────────────────────────────────
    // Las unidades vienen como campos U1, U2, U3... en cada objeto de materia.

    fun getCachedCalifUnidades(): List<CalificacionUnidad> = cache.getCalifUnidades()

    suspend fun getCalifUnidades(): List<CalificacionUnidad> {
        val xml = apiService.getCalifUnidades()
        val jsonStr = extractFromSoap(xml, "getCalifUnidadesByAlumnoResult")
        if (jsonStr.isEmpty() || jsonStr == "null") return emptyList()
        return runCatching {
            val array = toJsonArray(jsonStr, "lstCalif", "Calificaciones")
            array.flatMap { element ->
                val obj = element.jsonObject
                val clv     = obj.str("ClvMat", "ClvOfiMat", "clvOficial")
                val materia = obj.str("Materia", "materia", "NomMat").ifBlank { clv }
                val grupo   = obj.str("Grupo", "grupo")
                (1..10).mapNotNull { u ->
                    val calStr = obj.str("U$u", "C$u")
                    if (calStr.isNotBlank() && calStr != "null" && calStr != "--") {
                        val cal = calStr.toDoubleOrNull() ?: 0.0
                        if (cal > 0.0) CalificacionUnidad(
                            clvOficial    = clv,
                            materia       = materia,
                            unidad        = u,
                            calificacion  = cal,
                            fecha         = obj.str("Fecha$u", "fecha$u", "Fecha"),
                            observaciones = if (grupo.isNotEmpty()) "Grupo: $grupo"
                                            else obj.str("Observaciones", "observaciones")
                        ) else null
                    } else null
                }
            }
        }.getOrDefault(emptyList()).also { list ->
            if (list.isNotEmpty()) cache.saveCalifUnidades(list)
        }
    }

    // ── Calificaciones Finales ────────────────────────────────────────────────

    fun getCachedCalifFinal(): List<CalificacionFinal> = cache.getCalifFinal()

    suspend fun getCalifFinal(modEducativo: Int): List<CalificacionFinal> {
        val xml = apiService.getCalifFinal(modEducativo)
        val jsonStr = extractFromSoap(xml, "getAllCalifFinalByAlumnosResult")
        if (jsonStr.isEmpty() || jsonStr == "null") return emptyList()
        return runCatching {
            val array = toJsonArray(jsonStr, "lstFinal", "lstCargaCalif", "lstCalif", "Calificaciones")
            array.mapNotNull { element ->
                val obj = element.jsonObject
                val calStr = obj.str("calif", "Calif", "calFinal", "califFinal", "calificacion", "Calificacion").ifBlank {
                    obj.double("calif", "Calif", "calFinal", "califFinal").takeIf { it != 0.0 }?.toString() ?: ""
                }
                val p1 = obj.str("P1", "P2", "periodo", "Periodo", "tipo")
                val a1 = obj.str("A1", "A2", "anio", "Anio")
                val periodo = listOf(p1, a1).filter { it.isNotBlank() }.joinToString(" ")
                CalificacionFinal(
                    clvOficial    = obj.str("clvMat", "ClvMat", "ClvOfiMat", "clvOficial", "clave", "Clave"),
                    materia       = obj.str("materia", "Materia", "NomMat", "nombreMateria"),
                    grupo         = obj.str("grupo", "Grupo"),
                    calificacion  = calStr,
                    acreditacion  = obj.str("acreditado", "Acred", "acreditacion", "tipoAcred", "tipo_acred"),
                    periodo       = periodo.trim(),
                    creditos      = obj.int("C", "Cdts", "Creditos", "creditos", "cred"),
                    observaciones = obj.str("Observaciones", "observaciones", "obs")
                )
            }
        }.getOrDefault(emptyList()).also { list ->
            if (list.isNotEmpty()) cache.saveCalifFinal(list)
        }
    }

    fun clearSession() {
        cache.clearAll()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun extractFromSoap(xml: String, tagName: String): String {
        val startTag = "<$tagName>"
        val endTag   = "</$tagName>"
        val s = xml.indexOf(startTag)
        val e = xml.indexOf(endTag)
        return if (s != -1 && e != -1) xml.substring(s + startTag.length, e) else ""
    }

    /** Convierte el jsonStr en JsonArray; si es un objeto, busca el array bajo las claves dadas. */
    private fun toJsonArray(jsonStr: String, vararg arrayKeys: String): JsonArray {
        if (jsonStr.isBlank() || jsonStr == "null" || jsonStr == "[]") return JsonArray(emptyList())
        return try {
            when (val el = json.parseToJsonElement(jsonStr)) {
                is JsonArray  -> el
                is JsonObject -> arrayKeys.firstNotNullOfOrNull { key ->
                    (el[key] as? JsonArray)?.takeIf { it.isNotEmpty() }
                } ?: JsonArray(emptyList())
                else -> JsonArray(emptyList())
            }
        } catch (e: Exception) { JsonArray(emptyList()) }
    }

    /** Lee un campo String probando varios nombres de clave. */
    private fun JsonObject.str(vararg keys: String): String {
        for (key in keys) {
            val v = this[key] ?: continue
            val s = (v as? JsonPrimitive)?.content ?: continue
            if (s.isNotBlank() && s != "null") return s
        }
        return ""
    }

    /** Lee un campo Int probando varios nombres de clave. */
    private fun JsonObject.int(vararg keys: String): Int {
        for (key in keys) {
            val v = (this[key] as? JsonPrimitive) ?: continue
            v.intOrNull?.let { return it }
            v.content.toIntOrNull()?.let { return it }
        }
        return 0
    }

    /** Lee un campo Double probando varios nombres de clave. */
    private fun JsonObject.double(vararg keys: String): Double {
        for (key in keys) {
            val v = (this[key] as? JsonPrimitive) ?: continue
            v.doubleOrNull?.let { return it }
            v.content.toDoubleOrNull()?.let { return it }
        }
        return 0.0
    }
}
