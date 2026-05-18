package com.example.sicedroidmultiplatform

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sicedroidmultiplatform.database.DataCacheFactory
import com.example.sicedroidmultiplatform.ui.CargaAcademicaScreen
import com.example.sicedroidmultiplatform.ui.CalifFinalScreen
import com.example.sicedroidmultiplatform.ui.CalifUnidadesScreen
import com.example.sicedroidmultiplatform.ui.KardexScreen
import com.example.sicedroidmultiplatform.ui.LoginScreen
import com.example.sicedroidmultiplatform.ui.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(cacheFactory: DataCacheFactory) {
    val snViewModel: SicenetViewModel = viewModel { SicenetViewModel(cacheFactory) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("SICENET") })
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (val state = snViewModel.uiState) {
                    is SicenetUiState.NotLoggedIn,
                    is SicenetUiState.Loading,
                    is SicenetUiState.Error -> {
                        LoginScreen(
                            uiState = snViewModel.uiState,
                            onLoginClick = { matricula, password ->
                                snViewModel.login(matricula, password)
                            }
                        )
                    }

                    is SicenetUiState.ProfileSuccess -> {
                        ProfileScreen(
                            profile = state.profile,
                            onLogoutClick = { snViewModel.logout() },
                            onCargaAcademicaClick = { snViewModel.getCargaAcademica() },
                            onKardexClick = { snViewModel.getKardex() },
                            onCalifUnidadesClick = { snViewModel.getCalifUnidades() },
                            onCalifFinalClick = { snViewModel.getCalifFinal() }
                        )
                    }

                    is SicenetUiState.CargaAcademicaSuccess -> {
                        CargaAcademicaScreen(
                            carga = state.carga,
                            onBackClick = { snViewModel.backToProfile() }
                        )
                    }

                    is SicenetUiState.KardexSuccess -> {
                        KardexScreen(
                            kardex = state.kardex,
                            onBackClick = { snViewModel.backToProfile() }
                        )
                    }

                    is SicenetUiState.CalifUnidadesSuccess -> {
                        CalifUnidadesScreen(
                            calificaciones = state.calificaciones,
                            onBackClick = { snViewModel.backToProfile() }
                        )
                    }

                    is SicenetUiState.CalifFinalSuccess -> {
                        CalifFinalScreen(
                            calificaciones = state.calificaciones,
                            onBackClick = { snViewModel.backToProfile() }
                        )
                    }
                }
            }
        }
    }
}
