package com.example.sicedroidmultiplatform.data

import kotlinx.serialization.Serializable

@Serializable
data class AccesoLoginResult(
    val acceso: Boolean = false,
    val matricula: String = "",
    val contrasenia: String = "",
    val tipoUsuario: Int = 0,
    val estatus: String = ""
)

@Serializable
data class ProfileStudent(
    val matricula: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val especialidad: String = "",
    val semActual: Int = 0,
    val cdtosAcumulados: Int = 0,
    val cdtosActuales: Int = 0,
    val lineamiento: Int = 0,
    val fechaReins: String = "",
    val estatus: String = "",
    val modEducativo: Int = 0,
    val inscrito: Boolean = false,
    val adeudo: Boolean = false,
    val adeudoDescripcion: String = "",
    val urlFoto: String = ""
) {
    val semestre: Int get() = semActual
    val creditosAcumulados: Int get() = cdtosAcumulados
    val creditosActuales: Int get() = cdtosActuales
}

@Serializable
data class CargaAcademica(
    val clvOficial: String = "",
    val materia: String = "",
    val grupo: String = "",
    val creditos: Int = 0,
    val docente: String = "",
    val observaciones: String = "",
    val estadoMateria: Int = 0,
    val semestre: Int = 0
)

@Serializable
data class KardexItem(
    val clvOficial: String = "",
    val materia: String = "",
    val semestre: Int = 0,
    val creditos: Int = 0,
    val calificacion: String = "",
    val acreditacion: String = "",
    val periodo: String = "",
    val observaciones: String = ""
)

@Serializable
data class CalificacionUnidad(
    val clvOficial: String = "",
    val materia: String = "",
    val unidad: Int = 0,
    val calificacion: Double = 0.0,
    val fecha: String = "",
    val observaciones: String = ""
)

@Serializable
data class CalificacionFinal(
    val clvOficial: String = "",
    val materia: String = "",
    val grupo: String = "",
    val calificacion: String = "",
    val acreditacion: String = "",
    val periodo: String = "",
    val creditos: Int = 0,
    val observaciones: String = ""
)
