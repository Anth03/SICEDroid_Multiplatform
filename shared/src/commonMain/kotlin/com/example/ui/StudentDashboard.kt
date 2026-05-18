package com.example.sicedroidmultiplatform.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sicedroidmultiplatform.SicenetRepository
import kotlinx.coroutines.launch

@Composable
fun StudentDashboard(
    repository: SicenetRepository,
    onLogout: () -> Unit
) {
    val loginState by repository.loginState.collectAsState()
    val studentInfo = loginState?.studentInfo
    val academicProfile by repository.academicProfile.collectAsState()
    val schedule by repository.schedule.collectAsState()

    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            repository.loadAcademicProfile()
            repository.loadSchedule()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SICENET - ${studentInfo?.nombre ?: "Alumno"}") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Perfil") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Horario") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Kardex") }
                )
            }

            when (selectedTab) {
                0 -> PerfilTab(studentInfo, academicProfile)
                1 -> ScheduleTab(schedule)
                2 -> KardexTab(repository)
            }
        }
    }
}

@Composable
fun PerfilTab(studentInfo: com.example.sicedroidmultiplatform.data.StudentInfo?, academicProfile: String?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (studentInfo != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Información Personal", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow("Matrícula", studentInfo.matricula)
                        InfoRow("Nombre", studentInfo.nombre)
                        InfoRow("Carrera", studentInfo.carrera)
                        InfoRow("Semestre", studentInfo.semestre.toString())
                    }
                }
            }
        }

        if (academicProfile != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Perfil Académico", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = academicProfile,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleTab(schedule: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        if (schedule != null) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Carga Académica", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = schedule,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        } else {
            Text("Cargando horario...")
        }
    }
}

@Composable
fun KardexTab(repository: SicenetRepository) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Kardex - Próximamente")
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(modifier = Modifier.height(4.dp))
}