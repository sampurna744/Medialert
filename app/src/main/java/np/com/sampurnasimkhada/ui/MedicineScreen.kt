package np.com.sampurnasimkhada.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Medicine(val id: Int, val name: String, val time: String, val repeat: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen() {
    var medicineName by remember { mutableStateOf("") }
    var medicineTime by remember { mutableStateOf("") }
    var repeatOption by remember { mutableStateOf("Daily") }
    
    // Mock data list
    val medicines = remember {
        mutableStateListOf(
            Medicine(1, "Paracetamol", "08:00 AM", "Daily"),
            Medicine(2, "Vitamin C", "09:00 PM", "Weekly")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Medicines & Alarms") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Handle add */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Add/Edit Medicine", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = medicineName,
                onValueChange = { medicineName = it },
                label = { Text("Medicine Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = medicineTime,
                onValueChange = { medicineTime = it },
                label = { Text("Time (e.g. 08:00 AM)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Repeat:")
                Spacer(modifier = Modifier.width(16.dp))
                // Simple Radio buttons or a Dropdown would go here
                Text(repeatOption, style = MaterialTheme.typography.bodyLarge)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* Save logic */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Your Medicines", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(medicines) { med ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = med.name, style = MaterialTheme.typography.bodyLarge)
                                Text(text = "${med.time} - ${med.repeat}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                IconButton(onClick = { /* Edit */ }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { /* Delete */ }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
