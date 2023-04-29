package org.ossreviewtoolkit.workbench.ui

import org.ossreviewtoolkit.workbench.utils.MaterialIcon

enum class MenuItem(val icon: MaterialIcon) {
    SUMMARY(MaterialIcon.ASSESSMENT),
    PACKAGES(MaterialIcon.INVENTORY),
    DEPENDENCIES(MaterialIcon.ACCOUNT_TREE),
    ISSUES(MaterialIcon.BUG_REPORT),
    RULE_VIOLATIONS(MaterialIcon.GAVEL),
    VULNERABILITIES(MaterialIcon.LOCK_OPEN),
    SETTINGS(MaterialIcon.SETTINGS)
}
