package np.com.sampurnasimkhada.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import np.com.sampurnasimkhada.ui.*

// ── Route constants ───────────────────────────────────────

sealed class Route(val path: String) {
    object Home         : Route("home")
    object MedicineList : Route("medicine_list")
    object AddMedicine  : Route("add_medicine")
    object EditMedicine : Route("edit_medicine/{medicineId}") {
        fun create(id: Long) = "edit_medicine/$id"
    }
    object MedicineDetail : Route("medicine_detail/{medicineId}") {
        fun create(id: Long) = "medicine_detail/$id"
    }
    object Chat         : Route("chat?medicineId={medicineId}") {
        fun create(medicineId: Long? = null) =
            if (medicineId != null) "chat?medicineId=$medicineId" else "chat"
    }
    object History      : Route("history")
    object Settings     : Route("settings")
}

// Bottom-nav tabs (routes shown in the nav bar)
val bottomNavRoutes = listOf(Route.Home.path, Route.MedicineList.path, Route.Chat.path)

// ── Nav graph ─────────────────────────────────────────────

@Composable
fun MediAlertNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Route.Home.path,
    ) {

        // ── Main tabs ─────────────────────────────────────
        composable(Route.Home.path) {
            HomeScreen(
                onAddMedicine   = { navController.navigate(Route.AddMedicine.path) },
                onOpenChat      = { navController.navigate(Route.Chat.create()) },
                onMedicineClick = { id -> navController.navigate(Route.MedicineDetail.create(id)) },
                onOpenHistory   = { navController.navigate(Route.History.path) },
                onOpenSettings  = { navController.navigate(Route.Settings.path) },
            )
        }

        composable(Route.MedicineList.path) {
            MedicineListScreen(
                onAddMedicine   = { navController.navigate(Route.AddMedicine.path) },
                onMedicineClick = { id -> navController.navigate(Route.MedicineDetail.create(id)) },
                onEditMedicine  = { id -> navController.navigate(Route.EditMedicine.create(id)) },
            )
        }

        composable(
            route     = Route.Chat.path,
            arguments = listOf(navArgument("medicineId") {
                type         = NavType.LongType
                defaultValue = -1L
            }),
        ) { back ->
            val medId = back.arguments!!.getLong("medicineId").takeIf { it != -1L }
            ChatScreen(
                contextMedicineId = medId,
                onBack            = { navController.popBackStack() },
            )
        }

        // ── Secondary screens ─────────────────────────────
        composable(Route.AddMedicine.path) {
            AddEditMedicineScreen(
                medicineId = null,
                onBack     = { navController.popBackStack() },
                onSaved    = { navController.popBackStack() },
            )
        }

        composable(
            route     = Route.EditMedicine.path,
            arguments = listOf(navArgument("medicineId") { type = NavType.LongType }),
        ) { back ->
            val id = back.arguments!!.getLong("medicineId")
            AddEditMedicineScreen(
                medicineId = id,
                onBack     = { navController.popBackStack() },
                onSaved    = { navController.popBackStack() },
            )
        }

        composable(
            route     = Route.MedicineDetail.path,
            arguments = listOf(navArgument("medicineId") { type = NavType.LongType }),
        ) { back ->
            val id = back.arguments!!.getLong("medicineId")
            MedicineDetailScreen(
                medicineId = id,
                onBack     = { navController.popBackStack() },
                onEdit     = { navController.navigate(Route.EditMedicine.create(id)) },
                onAskChat  = { navController.navigate(Route.Chat.create(id)) },
            )
        }

        composable(Route.History.path) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Settings.path) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
