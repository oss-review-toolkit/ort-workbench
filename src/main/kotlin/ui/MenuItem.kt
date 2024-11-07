package org.ossreviewtoolkit.workbench.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class MenuItem(val icon: ImageVector) {
    SUMMARY(Icons.Default.Assessment),
    PACKAGES(Icons.Default.Inventory),
    DEPENDENCIES(Icons.Default.AccountTree),
    ISSUES(Icons.Default.BugReport),
    RULE_VIOLATIONS(Icons.Default.Gavel),
    VULNERABILITIES(Icons.Default.LockOpen),
    SETTINGS(Icons.Default.Settings)
}
