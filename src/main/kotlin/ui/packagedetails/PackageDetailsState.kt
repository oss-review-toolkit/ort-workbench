package org.ossreviewtoolkit.workbench.ui.packagedetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.workbench.ui.AppState

@Composable
fun rememberPackageDetailsState(appState: AppState, id: Identifier): PackageDetailsState {
    val api = appState.ortModel.api.collectAsState()
    val packageInfo = remember(id) {
        val pkg = api.value.result.getPackageOrProject(id)
        val project = api.value.result.getProject(id)

        PackageInfo(
            id = id,
            pkg = pkg,
            project = project,
            license = api.value.licenseInfoResolver.resolveLicenseInfo(id)
        )
    }

    return remember(packageInfo) {
        PackageDetailsState(
            packageInfo = packageInfo
        )
    }
}

data class PackageInfo(
    val id: Identifier,
    val pkg: CuratedPackage?,
    val project: Project?,
    val license: ResolvedLicenseInfo
)

data class PackageDetailsState(
    val packageInfo: PackageInfo
)
