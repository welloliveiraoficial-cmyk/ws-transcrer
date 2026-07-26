package com.welloliveira.wstranscrer.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.welloliveira.wstranscrer.ui.theme.*

enum class AbaPrincipal(val rotulo: String) {
    ENVIAR("Enviar"),
    TRANSCRICOES("Transcrições"),
    CONFIGURACOES("Configurações")
}

@Composable
fun BottomDock(
    abaAtual: AbaPrincipal,
    aoSelecionar: (AbaPrincipal) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Panel2.copy(alpha = 0.92f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AbaPrincipal.entries.forEach { aba ->
            val ativo = aba == abaAtual
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        if (ativo) Modifier.background(Brush.linearGradient(GradienteBotao))
                        else Modifier
                    )
                    .clickable { aoSelecionar(aba) }
                    .padding(horizontal = if (ativo) 16.dp else 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (aba) {
                        AbaPrincipal.ENVIAR -> Icons.Filled.FileUpload
                        AbaPrincipal.TRANSCRICOES -> Icons.Filled.Description
                        AbaPrincipal.CONFIGURACOES -> Icons.Filled.Settings
                    },
                    contentDescription = aba.rotulo,
                    tint = if (ativo) Bg else InkDim,
                    modifier = Modifier.size(20.dp)
                )
                if (ativo) {
                    Text(aba.rotulo, color = Bg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
