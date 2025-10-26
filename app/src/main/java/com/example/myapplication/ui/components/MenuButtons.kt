package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Paleta/padrão visual igual aos tiles do menu. */
object MenuButtonStyle {
    val Container  = Color(0xFF0F3A34)   // verde escuro (tile)
    val Content    = Color(0xFFE6F2EF)   // texto claro
    val Shape: Shape = RoundedCornerShape(16.dp)
}

/** Botão primário (cheio). */
@Composable
fun MenuPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MenuButtonStyle.Shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MenuButtonStyle.Container,
            contentColor   = MenuButtonStyle.Content,
            disabledContainerColor = MenuButtonStyle.Container.copy(alpha = 0.5f),
            disabledContentColor   = MenuButtonStyle.Content.copy(alpha = 0.7f),
        )
    ) { content() }
}

/** Botão secundário (contornado). */
@Composable
fun MenuSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MenuButtonStyle.Shape,
        border = BorderStroke(1.5.dp, MenuButtonStyle.Container),
        colors = ButtonDefaults.outlinedButtonColors( // 👈 corrigido aqui
            containerColor = Color.Transparent,
            contentColor   = MenuButtonStyle.Container,
            disabledContentColor = MenuButtonStyle.Container.copy(alpha = 0.5f),
        )
    ) { content() }
}
