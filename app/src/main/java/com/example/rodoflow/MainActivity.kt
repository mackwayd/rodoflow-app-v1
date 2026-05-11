package com.example.rodoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rodoflow.AppLog
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.ui.abastecimentos.NovaAbastecimentoScreen
import com.example.rodoflow.ui.components.LoadDataErrorPanel
import com.example.rodoflow.ui.components.LocalSnackbar
import com.example.rodoflow.ui.components.StatusBadge
import com.example.rodoflow.ui.despesas.NovaDespesaScreen
import com.example.rodoflow.ui.financeiro.FinanceiroScreen
import com.example.rodoflow.ui.home.HomeScreen
import com.example.rodoflow.ui.theme.RodoFlowTheme
import com.example.rodoflow.ui.util.formatBrl
import com.example.rodoflow.ui.util.formatIsoDateTimeBr
import com.example.rodoflow.ui.util.formatToneladas
import com.example.rodoflow.ui.viagens.NovaViagemScreen
import com.example.rodoflow.ui.viagens.ViagemDetalheScreen
import com.example.rodoflow.ui.viagens.ViagemDetalheViewModel
import com.example.rodoflow.ui.viagens.ViagensViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RodoFlowTheme {
                RodoFlowApp()
            }
        }
    }
}

sealed interface ViagensIntent {
    data object NovaViagem : ViagensIntent
    data class AbrirViagem(val viagemId: String) : ViagensIntent
    data class NovoAbastecimento(val viagemId: String?) : ViagensIntent
    data class NovaDespesa(val viagemId: String?) : ViagensIntent
}

@Composable
fun RodoFlowApp() {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Home) }
    var homeReloadNonce by remember { mutableIntStateOf(0) }
    var viagensReloadNonce by remember { mutableIntStateOf(0) }
    var financeiroReloadNonce by remember { mutableIntStateOf(0) }
    var pendingViagensIntent by remember { mutableStateOf<ViagensIntent?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val showSnackbar: (String) -> Unit = remember(snackbarHostState, snackbarScope) {
        { message ->
            snackbarScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
            Unit
        }
    }

    CompositionLocalProvider(LocalSnackbar provides showSnackbar) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTab = tab
                                when (tab) {
                                    BottomTab.Home -> homeReloadNonce++
                                    BottomTab.Viagens -> viagensReloadNonce++
                                    BottomTab.Financeiro -> financeiroReloadNonce++
                                }
                            },
                            label = { Text(text = tab.label) },
                            icon = {}
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(snackbarData = data)
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                when (selectedTab) {
                    BottomTab.Home -> HomeScreen(
                        reloadNonce = homeReloadNonce,
                        onNovaViagem = {
                            pendingViagensIntent = ViagensIntent.NovaViagem
                            selectedTab = BottomTab.Viagens
                        },
                        onNovoAbastecimento = { viagemId ->
                            pendingViagensIntent = ViagensIntent.NovoAbastecimento(viagemId)
                            selectedTab = BottomTab.Viagens
                        },
                        onNovaDespesa = { viagemId ->
                            pendingViagensIntent = ViagensIntent.NovaDespesa(viagemId)
                            selectedTab = BottomTab.Viagens
                        },
                        onAbrirViagemAtual = { viagemId ->
                            pendingViagensIntent = ViagensIntent.AbrirViagem(viagemId)
                            selectedTab = BottomTab.Viagens
                        },
                    )
                    BottomTab.Viagens -> ViagensScreen(
                        reloadNonce = viagensReloadNonce,
                        pendingIntent = pendingViagensIntent,
                        onIntentHandled = { pendingViagensIntent = null },
                        onFinanceiroChanged = {
                            homeReloadNonce++
                            financeiroReloadNonce++
                            AppLog.d(
                                "HOME_RELOAD",
                                "onFinanceiroChanged disparado -> homeReloadNonce=$homeReloadNonce " +
                                    "financeiroReloadNonce=$financeiroReloadNonce",
                            )
                        },
                    )
                    BottomTab.Financeiro -> FinanceiroScreen(reloadNonce = financeiroReloadNonce)
                }
            }
        }
    }
}

enum class BottomTab(val label: String) {
    Home("Home"),
    Viagens("Viagens"),
    Financeiro("Financeiro")
}

@Composable
fun ViagensScreen(
    reloadNonce: Int = 0,
    pendingIntent: ViagensIntent? = null,
    onIntentHandled: () -> Unit = {},
    onFinanceiroChanged: () -> Unit = {},
    viewModel: ViagensViewModel = viewModel(),
) {
    val navController = rememberNavController()
    LaunchedEffect(pendingIntent) {
        when (val intent = pendingIntent) {
            null -> Unit
            ViagensIntent.NovaViagem -> {
                navController.navigate("nova_viagem")
                onIntentHandled()
            }
            is ViagensIntent.AbrirViagem -> {
                navController.navigate("viagem_detalhe/${intent.viagemId}")
                onIntentHandled()
            }
            is ViagensIntent.NovoAbastecimento -> {
                if (intent.viagemId != null) {
                    navController.navigate("viagem_detalhe/${intent.viagemId}")
                    navController.navigate("nova_abastecimento/${intent.viagemId}")
                } else {
                    navController.navigate("nova_abastecimento")
                }
                onIntentHandled()
            }
            is ViagensIntent.NovaDespesa -> {
                if (intent.viagemId != null) {
                    navController.navigate("viagem_detalhe/${intent.viagemId}")
                    navController.navigate("nova_despesa/${intent.viagemId}")
                } else {
                    navController.navigate("nova_despesa")
                }
                onIntentHandled()
            }
        }
    }
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "viagens_list",
    ) {
        composable("viagens_list") {
            val loading by viewModel.loading.collectAsStateWithLifecycle()
            val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()
            val viagens by viewModel.viagens.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.loadViagens()
            }
            LaunchedEffect(reloadNonce) {
                if (reloadNonce > 0) {
                    viewModel.loadViagens()
                }
            }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            LaunchedEffect(navBackStackEntry?.id) {
                if (navBackStackEntry?.destination?.route == "viagens_list") {
                    viewModel.loadViagens()
                }
            }

            when {
                loading && viagens.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(
                                text = "Carregando viagens...",
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                }
                loadFailed && viagens.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadDataErrorPanel(onRetry = { viewModel.loadViagens() })
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    ) {
                        Button(
                            onClick = { navController.navigate("nova_viagem") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            Text("+ Nova Viagem")
                        }

                        if (viagens.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Nenhuma viagem encontrada",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "As viagens criadas aparecerão aqui",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                items(
                                    items = viagens,
                                    key = { it.id },
                                ) { item ->
                                    ViagemListCard(
                                        item = item,
                                        onAbrirDetalhes = {
                                            navController.navigate("viagem_detalhe/${item.id}")
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        composable("nova_viagem") {
            NovaViagemScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("viagem_detalhe/{viagemId}") { backStackEntry ->
            val viagemId = backStackEntry.arguments?.getString("viagemId").orEmpty()
            ViagemDetalheScreen(
                viagemId = viagemId,
                onNavigateNovaDespesa = { id -> navController.navigate("nova_despesa/$id") },
                onNavigateNovoAbastecimento = { id -> navController.navigate("nova_abastecimento/$id") },
                onFinanceiroChanged = onFinanceiroChanged,
            )
        }
        composable("nova_despesa") {
            NovaDespesaScreen(
                preselectedViagemId = null,
                onNavigateBack = {
                    onFinanceiroChanged()
                    navController.popBackStack()
                },
            )
        }
        composable("nova_despesa/{viagemId}") { backStackEntry ->
            val viagemId = backStackEntry.arguments?.getString("viagemId").orEmpty()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("viagem_detalhe/$viagemId")
            }
            val parentViewModel: ViagemDetalheViewModel = viewModel(viewModelStoreOwner = parentEntry)
            NovaDespesaScreen(
                preselectedViagemId = viagemId,
                onNavigateBack = {
                    parentViewModel.loadViagem(viagemId)
                    onFinanceiroChanged()
                    navController.popBackStack()
                },
            )
        }
        composable("nova_abastecimento") {
            NovaAbastecimentoScreen(
                preselectedViagemId = null,
                onNavigateBack = {
                    onFinanceiroChanged()
                    navController.popBackStack()
                },
            )
        }
        composable("nova_abastecimento/{viagemId}") { backStackEntry ->
            val viagemId = backStackEntry.arguments?.getString("viagemId").orEmpty()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("viagem_detalhe/$viagemId")
            }
            val parentViewModel: ViagemDetalheViewModel = viewModel(viewModelStoreOwner = parentEntry)
            NovaAbastecimentoScreen(
                preselectedViagemId = viagemId,
                onNavigateBack = {
                    parentViewModel.loadViagem(viagemId)
                    onFinanceiroChanged()
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
private fun ViagemListCard(
    item: Viagem,
    onAbrirDetalhes: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "${item.origem.ifBlank { "-" }} → ${item.destino.ifBlank { "-" }}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Cliente: ${item.cliente.ifBlank { "-" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val cargaFmt = humanizeTipoCarga(item.tipoCarga)
                val tonFmt = formatToneladas(item.numeroToneladas).let { t ->
                    if (t == "-") "—" else t
                }
                Text(
                    text = "Carga: $cargaFmt • $tonFmt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatBrl(item.valorBruto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    StatusBadge(status = item.status)
                    Text(
                        text = formatIsoDateTimeBr(item.dataInicio),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedButton(
                onClick = onAbrirDetalhes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text("Abrir detalhes")
            }
        }
    }
}

private fun humanizeTipoCarga(raw: String): String {
    if (raw.isBlank()) return "-"
    return raw.split("_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase(Locale.getDefault()) }
        }
}

@Preview(showBackground = true)
@Composable
fun RodoFlowAppPreview() {
    RodoFlowTheme {
        RodoFlowApp()
    }
}
