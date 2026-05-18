package com.example.sicedroidmultiplatform.data

import kotlinx.serialization.Serializable

@Serializable
data class StudentInfo(
    val matricula: String,
    val nombre: String,
    val carrera: String,
    val semestre: Int,
    val lineamiento: Int
)

@Serializable
data class Subject(
    val clave: String,
    val nombre: String,
    val calificacion: Double?,
    val estatus: String
)

@Serializable
data class AcademicPeriod(
    val periodo: String,
    val materias: List<Subject>
)

@Serializable
data class LoginResult(
    val success: Boolean,
    val message: String,
    val studentInfo: StudentInfo?
)