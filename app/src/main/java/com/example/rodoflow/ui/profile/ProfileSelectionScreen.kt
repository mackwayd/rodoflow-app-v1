package com.example.rodoflow.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rodoflow.ui.session.UserProfile
import com.example.rodoflow.ui.theme.AppButtonShape
import com.example.rodoflow.ui.theme.AppCardShape

@Composable
fun ProfileSelectionScreen(
    onSelect: (UserProfile) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "RodoFlow",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Escolha como vai usar o app neste dispositivo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppCardShape,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { onSelect(UserProfile.Motorista) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = AppButtonShape,
                ) {
                    Icon(Icons.Outlined.DirectionsCar, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Motorista", style = MaterialTheme.typography.titleSmall)
                }
                OutlinedButton(
                    onClick = { onSelect(UserProfile.Admin) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = AppButtonShape,
                ) {
                    Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Admin", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
