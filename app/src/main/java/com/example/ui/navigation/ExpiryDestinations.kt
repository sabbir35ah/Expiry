package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        activeIcon = Icons.Filled.Dashboard,
        inactiveIcon = Icons.Outlined.Dashboard
    )

    data object Timeline : Screen(
        route = "timeline",
        title = "Timeline",
        activeIcon = Icons.Filled.Timeline,
        inactiveIcon = Icons.Outlined.Timeline
    )

    data object RenewalHub : Screen(
        route = "renewal_hub",
        title = "Renewal Hub",
        activeIcon = Icons.Filled.Public,
        inactiveIcon = Icons.Outlined.Public
    )

    data object VaultPrivacy : Screen(
        route = "vault_privacy",
        title = "Privacy Vault",
        activeIcon = Icons.Filled.Lock,
        inactiveIcon = Icons.Outlined.Lock
    )

    companion object {
        val bottomNavItems = listOf(Dashboard, Timeline, RenewalHub, VaultPrivacy)
    }
}

object ExpiryRoutes {
    const val DASHBOARD = "dashboard"
    const val TIMELINE = "timeline"
    const val RENEWAL_HUB = "renewal_hub"
    const val VAULT_PRIVACY = "vault_privacy"
    const val DOCUMENT_DETAIL = "doc_detail/{docId}"
    const val DOCUMENT_EDITOR = "doc_editor?docId={docId}"
    const val DOCUMENT_SCANNER = "doc_scanner"

    fun documentDetail(docId: Long): String = "doc_detail/$docId"
    fun documentEditor(docId: Long = 0L): String = "doc_editor?docId=$docId"
}
