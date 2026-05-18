package com.example.sicedroidmultiplatform

import com.example.sicedroidmultiplatform.network.SicenetApiService
import com.example.sicedroidmultiplatform.data.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.xml.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SicenetRepository(
    private val clientEngine: HttpClientEngine? = null
) {
    private val client = HttpClient(clientEngine) {
        install(ContentNegotiation) {
            xml()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
        }
        expectSuccess = false
    }

    private val apiService = SicenetApiService(client)

    private val _loginState = MutableStateFlow<LoginResult?>(null)
    val loginState: StateFlow<LoginResult?> = _loginState.asStateFlow()

    private val _academicProfile = MutableStateFlow<String?>(null)
    val academicProfile: StateFlow<String?> = _academicProfile.asStateFlow()

    private val _schedule = MutableStateFlow<String?>(null)
    val schedule: StateFlow<String?> = _schedule.asStateFlow()

    suspend fun login(matricula: String, password: String) {
        try {
            val response = apiService.login(matricula, password)
            // Parsear la respuesta SOAP para extraer información del estudiante
            val success = response.contains("exito") || response.contains("success")
            _loginState.value = LoginResult(
                success = success,
                message = if (success) "Login exitoso" else "Credenciales incorrectas",
                studentInfo = if (success) parseStudentInfo(response) else null
            )
        } catch (e: Exception) {
            _loginState.value = LoginResult(
                success = false,
                message = "Error de conexión: ${e.message}",
                studentInfo = null
            )
        }
    }

    suspend fun loadAcademicProfile() {
        try {
            val response = apiService.getPerfilAcademico()
            _academicProfile.value = response
        } catch (e: Exception) {
            _academicProfile.value = "Error: ${e.message}"
        }
    }

    suspend fun loadSchedule() {
        try {
            val response = apiService.getCargaAcademica()
            _schedule.value = response
        } catch (e: Exception) {
            _schedule.value = "Error: ${e.message}"
        }
    }

    private fun parseStudentInfo(response: String): StudentInfo? {
        // Parseo básico, deberías mejorarlo con un parser XML real
        val matricula = extractXmlValue(response, "Matricula") ?: "N/A"
        val nombre = extractXmlValue(response, "Nombre") ?: "N/A"
        val carrera = extractXmlValue(response, "Carrera") ?: "N/A"
        val semestre = extractXmlValue(response, "Semestre")?.toIntOrNull() ?: 1
        val lineamiento = extractXmlValue(response, "Lineamiento")?.toIntOrNull() ?: 0

        return StudentInfo(matricula, nombre, carrera, semestre, lineamiento)
    }

    private fun extractXmlValue(xml: String, tag: String): String? {
        val pattern = "<$tag>(.*?)</$tag>".toRegex()
        return pattern.find(xml)?.groupValues?.get(1)
    }

    fun clearData() {
        _loginState.value = null
        _academicProfile.value = null
        _schedule.value = null
    }
}