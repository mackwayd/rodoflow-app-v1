package com.example.rodoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rodoflow.data.model.SaldoMotorista
import com.example.rodoflow.data.repository.AuthRepository
import com.example.rodoflow.ui.abastecimentos.NovaAbastecimentoScreen
import com.example.rodoflow.ui.despesas.NovaDespesaScreen
import com.example.rodoflow.ui.financeiro.FinanceiroViewModel
import com.example.rodoflow.ui.home.HomeViewModel
import com.example.rodoflow.ui.login.LoginScreen
import com.example.rodoflow.ui.session.rememberAppSessionViewModel
import com.example.rodoflow.ui.theme.RodoFlowTheme
import com.example.rodoflow.ui.viagens.NovaViagemScreen
import com.example.rodoflow.ui.viagens.ViagemDetalheScreen
import com.example.rodoflow.ui.viagens.ViagensViewModel
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RodoFlowTheme {
                RodoFlowApp()
            }
        }
    }
}

@Composable
fun RodoFlowApp() {
    val sessionViewModel = rememberAppSessionViewModel()
    val profile by sessionViewModel.profile.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Home) }
    var homeReloadNonce by remember { mutableIntStateOf(0) }
    var viagensReloadNonce by remember { mutableIntStateOf(0) }
    var financeiroReloadNonce by remember { mutableIntStateOf(0) }

    var bootstrapDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        AuthRepository().tryRestoreSession().onSuccess { (me, userId) ->
            sessionViewModel.onAuthenticated(me, userId)
        }
        bootstrapDone = true
    }

    if (!bootstrapDone) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (profile == null) {
        LoginScreen(
            sessionViewModel = sessionViewModel,
            onLoginSuccess = {
                selectedTab = BottomTab.Home
                homeReloadNonce++
            },
        )
        return
    }

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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (selectedTab) {
                BottomTab.Home -> HomeScreen(reloadNonce = homeReloadNonce)
                BottomTab.Viagens -> ViagensScreen(reloadNonce = viagensReloadNonce)
                BottomTab.Financeiro -> FinanceiroScreen(reloadNonce = financeiroReloadNonce)
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
fun HomeScreen(
    reloadNonce: Int = 0,
    viewModel: HomeViewModel = viewModel(),
) {
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val saldo by viewModel.saldo.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSaldo()
    }
    LaunchedEffect(reloadNonce) {
        if (reloadNonce > 0) {
            viewModel.loadSaldo()
        }
    }

    val moneyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    when {
        loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Carregando...")
            }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error.orEmpty())
            }
        }
        else -> {
            when {
                saldo.size == 1 -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SaldoMotoristaLinhas(item = saldo.first(), moneyFormat = moneyFormat)
                    }
                }
                saldo.size > 1 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = saldo,
                            key = { it.motoristaId },
                        ) { item ->
                            SaldoMotoristaLinhas(item = item, moneyFormat = moneyFormat)
                        }
                    }
                }
                else -> Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun SaldoMotoristaLinhas(item: SaldoMotorista, moneyFormat: NumberFormat) {
    Column {
        Text(text = "Motorista: ${item.motoristaNome}")
        Text(text = "Total de viagens: ${item.totalViagens}")
        Text(text = "Saldo pendente: ${moneyFormat.format(item.saldoPendente)}")
    }
}

@Composable
fun ViagensScreen(
    reloadNonce: Int = 0,
    viewModel: ViagensViewModel = viewModel(),
) {
    val navController = rememberNavController()
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "viagens_list",
    ) {
        composable("viagens_list") {
            val loading by viewModel.loading.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle()
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

            val moneyFormat = remember {
                NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
            }

            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Carregando...")
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error.orEmpty())
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(onClick = { navController.navigate("nova_viagem") }) {
                            Text("+ Nova Viagem")
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = viagens,
                                key = { it.id },
                            ) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate("viagem_detalhe/${item.id}") },
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(text = "${item.origem} → ${item.destino}")
                                    Text(text = "Valor: ${moneyFormat.format(item.valorBruto)}")
                                    Text(text = "Status: ${item.status}")
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
            )
        }
        composable("nova_despesa/{viagemId}") { backStackEntry ->
            val viagemId = backStackEntry.arguments?.getString("viagemId").orEmpty()
            NovaDespesaScreen(
                viagemId = viagemId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable("nova_abastecimento/{viagemId}") { backStackEntry ->
            val viagemId = backStackEntry.arguments?.getString("viagemId").orEmpty()
            NovaAbastecimentoScreen(
                viagemId = viagemId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun FinanceiroScreen(
    reloadNonce: Int = 0,
    viewModel: FinanceiroViewModel = viewModel(),
) {
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val resumos by viewModel.resumos.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadResumo()
    }
    LaunchedEffect(reloadNonce) {
        if (reloadNonce > 0) {
            viewModel.loadResumo()
        }
    }

    val moneyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    when {
        loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Carregando...")
            }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error.orEmpty())
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = resumos,
                    key = { it.viagemId },
                ) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(text = "Motorista: ${item.motoristaNome}")
                            Text(text = "Caminhão: ${item.caminhaoPlaca}")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Bruto: ${moneyFormat.format(item.valorBruto)}")
                            Text(text = "Despesas: ${moneyFormat.format(item.totalDespesas)}")
                            Text(text = "Abastecimento: ${moneyFormat.format(item.totalAbastecimentos)}")
                            Text(
                                text = "Líquido: ${moneyFormat.format(item.valorLiquido)}",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RodoFlowAppPreview() {
    RodoFlowTheme {
        RodoFlowApp()
    }
}