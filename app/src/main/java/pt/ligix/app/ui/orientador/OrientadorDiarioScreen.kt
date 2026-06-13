package pt.ligix.app.ui.orientador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ligix.app.R
import pt.ligix.app.ui.auth.DarkBlue

@Composable
fun OrientadorDiarioScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F7)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Book, contentDescription = null,
            tint = Color.LightGray, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.diary), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
        Text(stringResource(R.string.in_development), fontSize = 14.sp, color = Color.Gray)
    }
}
