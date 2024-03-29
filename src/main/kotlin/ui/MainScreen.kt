package org.ossreviewtoolkit.workbench.ui

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.lifecycle.ViewModel
import org.ossreviewtoolkit.workbench.model.OrtModel
import org.ossreviewtoolkit.workbench.navigation.Screen
import org.ossreviewtoolkit.workbench.ui.dependencies.DependenciesViewModel
import org.ossreviewtoolkit.workbench.ui.issues.IssuesViewModel
import org.ossreviewtoolkit.workbench.ui.packagedetails.PackageDetailsViewModel
import org.ossreviewtoolkit.workbench.ui.packages.PackagesViewModel
import org.ossreviewtoolkit.workbench.ui.settings.SettingsViewModel
import org.ossreviewtoolkit.workbench.ui.summary.SummaryViewModel
import org.ossreviewtoolkit.workbench.ui.violations.ViolationsViewModel
import org.ossreviewtoolkit.workbench.ui.vulnerabilities.VulnerabilitiesViewModel

sealed class MainScreen<VM : ViewModel>(val name: String, val menuItem: MenuItem? = null) : Screen<VM> {
    data class Dependencies(private val ortModel: OrtModel) :
        MainScreen<DependenciesViewModel>("Dependencies", MenuItem.DEPENDENCIES) {
        override fun createViewModel() = DependenciesViewModel(ortModel)
    }

    data class Issues(private val ortModel: OrtModel) : MainScreen<IssuesViewModel>("Issues", MenuItem.ISSUES) {
        override fun createViewModel() = IssuesViewModel(ortModel)
    }

    data class PackageDetails(private val ortModel: OrtModel, private val pkgId: Identifier) :
        MainScreen<PackageDetailsViewModel>("Package Details") {
        override fun createViewModel() = PackageDetailsViewModel(ortModel, pkgId)
    }

    data class Packages(private val ortModel: OrtModel) : MainScreen<PackagesViewModel>("Packages", MenuItem.PACKAGES) {
        override fun createViewModel() = PackagesViewModel(ortModel)
    }

    data class Settings(private val controller: WorkbenchController) :
        MainScreen<SettingsViewModel>("Settings", MenuItem.SETTINGS) {
        override fun createViewModel() = SettingsViewModel(controller)
    }

    data class Summary(private val ortModel: OrtModel) : MainScreen<SummaryViewModel>("Summary", MenuItem.SUMMARY) {
        override fun createViewModel() = SummaryViewModel(ortModel)
    }

    data class RuleViolations(private val ortModel: OrtModel) :
        MainScreen<ViolationsViewModel>("Rule Violations", MenuItem.RULE_VIOLATIONS) {
        override fun createViewModel() = ViolationsViewModel(ortModel)
    }

    data class Vulnerabilities(private val ortModel: OrtModel) :
        MainScreen<VulnerabilitiesViewModel>("Vulnerabilities", MenuItem.VULNERABILITIES) {
        override fun createViewModel() = VulnerabilitiesViewModel(ortModel)
    }
}
