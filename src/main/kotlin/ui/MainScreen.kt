package org.ossreviewtoolkit.workbench.ui

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.navigation.OldScreen
import org.ossreviewtoolkit.workbench.utils.MaterialIcon

sealed class MainScreen(val name: String, val icon: MaterialIcon) : OldScreen {
    object Dependencies : MainScreen("Dependencies", MaterialIcon.ACCOUNT_TREE)
    object Issues : MainScreen("Issues", MaterialIcon.BUG_REPORT)
    class PackageDetails(val pkg: Identifier) : MainScreen("Package Details", MaterialIcon.INVENTORY)
    object Packages : MainScreen("Packages", MaterialIcon.INVENTORY)
    object Settings : MainScreen("Settings", MaterialIcon.SETTINGS)
    object Summary : MainScreen("Summary", MaterialIcon.ASSESSMENT)
    object RuleViolations : MainScreen("Rule Violations", MaterialIcon.GAVEL)
    object Vulnerabilities : MainScreen("Vulnerabilities", MaterialIcon.LOCK_OPEN)
}
