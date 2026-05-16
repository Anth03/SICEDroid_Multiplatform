package com.example.sicedroidmultiplatform.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

private const val SOAP_ENDPOINT = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"
private const val SOAP_NAMESPACE = "http://tempuri.org/"

private fun soapLogin(matricula: String, password: String) = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <accesoLogin xmlns="http://tempuri.org/">
          <strMatricula>$matricula</strMatricula>
          <strContrasenia>$password</strContrasenia>
          <tipoUsuario>ALUMNO</tipoUsuario>
        </accesoLogin>
      </soap:Body>
    </soap:Envelope>""".trimIndent()

private val soapPerfilAcademico = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
      </soap:Body>
    </soap:Envelope>""".trimIndent()

private val soapCargaAcademica = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />
      </soap:Body>
    </soap:Envelope>""".trimIndent()

private val soapCalifUnidades = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />
      </soap:Body>
    </soap:Envelope>""".trimIndent()

private fun soapKardex(lineamiento: Int) = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
          <aluLineamiento>$lineamiento</aluLineamiento>
        </getAllKardexConPromedioByAlumno>
      </soap:Body>
    </soap:Envelope>""".trimIndent()

private fun soapCalifFinal(modEducativo: Int) = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
          <bytModEducativo>$modEducativo</bytModEducativo>
        </getAllCalifFinalByAlumnos>
      </soap:Body>
    </soap:Envelope>""".trimIndent()

class SicenetApiService(private val client: HttpClient) {

    private suspend fun post(soapAction: String, body: String): String =
        client.post(SOAP_ENDPOINT) {
            header("Content-Type", "text/xml; charset=utf-8")
            header("SOAPAction", "$SOAP_NAMESPACE$soapAction")
            setBody(body)
        }.bodyAsText()

    suspend fun login(matricula: String, password: String): String =
        post("accesoLogin", soapLogin(matricula, password))

    suspend fun getPerfilAcademico(): String =
        post("getAlumnoAcademicoWithLineamiento", soapPerfilAcademico)

    suspend fun getCargaAcademica(): String =
        post("getCargaAcademicaByAlumno", soapCargaAcademica)

    suspend fun getKardexConPromedio(lineamiento: Int): String =
        post("getAllKardexConPromedioByAlumno", soapKardex(lineamiento))

    suspend fun getCalifUnidades(): String =
        post("getCalifUnidadesByAlumno", soapCalifUnidades)

    suspend fun getCalifFinal(modEducativo: Int): String =
        post("getAllCalifFinalByAlumnos", soapCalifFinal(modEducativo))
}