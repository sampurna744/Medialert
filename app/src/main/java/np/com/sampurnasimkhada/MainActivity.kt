package np.com.sampurnasimkhada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import np.com.sampurnasimkhada.ui.*
import np.com.sampurnasimkhada.ui.theme.MedialertTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedialertTheme {
                val navController = rememberNavController()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        
                        // Only show bottom bar on the main app screens
                        if (currentRoute in listOf("medicine", "profile", "about")) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "medicine",
                                    onClick = { 
                                        navController.navigate("medicine") {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    },
                                    label = { Text("Meds") },
                                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Medicines") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = { 
                                        navController.navigate("profile") {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    },
                                    label = { Text("Profile") },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "about",
                                    onClick = { 
                                        navController.navigate("about") {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    },
                                    label = { Text("About") },
                                    icon = { Icon(Icons.Default.Info, contentDescription = "About") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "signin",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("signin") {
                            SignInScreen(
                                onSignInClick = { 
                                    navController.navigate("medicine") {
                                        popUpTo("signin") { inclusive = true }
                                    }
                                },
                                onSignUpNavigate = { navController.navigate("signup") }
                            )
                        }
                        composable("signup") {
                            SignUpScreen(
                                onRegisterClick = { navController.navigate("signin") },
                                onBackToSignIn = { navController.navigate("signin") }
                            )
                        }
                        composable("medicine") {
                            MedicineScreen()
                        }
                        composable("profile") {
                            ProfileScreen(onEditClick = { /* Handle edit logic */ })
                        }
                        composable("about") {
                            AboutUsScreen()
                        }
                    }
                }
            }
        }
    }
}
