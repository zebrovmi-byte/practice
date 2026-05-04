package ci.nsu.mobile.main.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ci.nsu.mobile.main.ui.edit.EditQuoteScreen
import ci.nsu.mobile.main.ui.home.HomeScreen
import ci.nsu.mobile.main.ui.quotes.QuotesScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Quotes : Screen("quotes")
    object EditQuote : Screen("edit_quote?id={id}") {
        fun createRoute(id: Long? = null) =
            if (id != null) "edit_quote?id=$id" else "edit_quote"
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onEditClick = { navController.navigate(Screen.Quotes.route) }
            )
        }
        composable(Screen.Quotes.route) {
            QuotesScreen(
                onBack = { navController.popBackStack() },
                onQuoteClick = { id ->
                    navController.navigate(Screen.EditQuote.createRoute(id))
                },
                onAddClick = {
                    navController.navigate(Screen.EditQuote.createRoute())
                }
            )
        }
        composable(
            route = Screen.EditQuote.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val idStr = backStackEntry.arguments?.getString("id")
            val id = idStr?.toLongOrNull()
            EditQuoteScreen(
                quoteId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
