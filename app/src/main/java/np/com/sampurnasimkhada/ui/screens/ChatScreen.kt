package np.com.sampurnasimkhada.ui
import android.app.Activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import np.com.sampurnasimkhada.ui.theme.*
import np.com.sampurnasimkhada.viewmodel.ChatMessage
import np.com.sampurnasimkhada.viewmodel.ChatViewModel
import np.com.sampurnasimkhada.viewmodel.ViewModelFactory

@Composable
fun ChatScreen(
    contextMedicineId: Long?,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel(factory = ViewModelFactory((LocalContext.current as Activity).application)),
) {
    val state by vm.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { vm.init(contextMedicineId) }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    val suggestions = if (state.contextMedicineName != null) {
        val n = state.contextMedicineName!!.split(" ").first()
        listOf("Side effects of $n?", "Can I take $n with food?", "What if I missed a dose?")
    } else listOf("What is ibuprofen?", "Can I take paracetamol daily?", "What are antihistamines?")

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            Surface(color = BgInset) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextMuted) }
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF00A884)), contentAlignment = Alignment.Center) { Text("🤖", fontSize = 18.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Medicine Assistant", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(if (state.isLoading) "Thinking…" else "Online", style = MaterialTheme.typography.labelSmall, color = if (state.isLoading) Warning else Secondary)
                    }
                    state.contextMedicineName?.let { n ->
                        Surface(shape = RoundedCornerShape(20.dp), color = Primary.copy(.2f)) {
                            Text(n.split(" ").first(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = Primary)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = BgInset) {
                Column {
                    // Suggestions (only first 2 messages)
                    if (state.messages.size <= 2) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            suggestions.take(3).forEach { s ->
                                SuggestionChip(onClick = { vm.send(s) }, label = { Text(s, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f),
                                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = BorderFaint, borderWidth = 1.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = BgSurface, labelColor = TextMuted))
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp).navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = state.input, onValueChange = vm::onInput,
                            modifier = Modifier.weight(1f), placeholder = { Text("Ask about your medicines…", color = TextMuted) },
                            singleLine = true, shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = BgSurface, unfocusedContainerColor = BgSurface,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = Primary, unfocusedBorderColor = BorderFaint, cursorColor = Primary))
                        val canSend = state.input.isNotBlank() && !state.isLoading
                        IconButton(onClick = { vm.send() }, enabled = canSend,
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(if (canSend) Primary else BgSurface)) {
                            Icon(Icons.Default.Send, null, tint = if (canSend) Color.White else TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
    ) { pad ->
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.messages) { msg -> Bubble(msg) }
            if (state.isLoading) {
                item {
                    Row(modifier = Modifier.padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF00A884)), contentAlignment = Alignment.Center) { Text("🤖", fontSize = 14.sp) }
                        Surface(shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp), color = BgSurface, border = BorderStroke(1.dp, BorderFaint)) {
                            Text("…", modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Bubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start, verticalAlignment = Alignment.Bottom) {
        if (!isUser) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF00A884)).padding(end = 8.dp), contentAlignment = Alignment.Center) { Text("🤖", fontSize = 14.sp) }
            Spacer(Modifier.width(8.dp))
        }
        Surface(modifier = Modifier.widthIn(max = 280.dp),
            shape = if (isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            color = if (isUser) Primary else BgSurface,
            border = if (isUser) null else BorderStroke(1.dp, BorderFaint)) {
            Text(msg.text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall, color = if (isUser) Color.White else TextPrimary, lineHeight = 20.sp)
        }
    }
}