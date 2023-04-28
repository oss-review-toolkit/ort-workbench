package org.ossreviewtoolkit.workbench.ui.packagedetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo
import org.ossreviewtoolkit.workbench.ui.WorkbenchController

@Composable
fun rememberPackageDetailsState(controller: WorkbenchController, id: Identifier): PackageDetailsState {
    val api by controller.ortModel.api.collectAsState()
    val packageInfo = remember(id) {
        val pkg = api.getCuratedPackageOrProject(id)
        val project = api.getProject(id)

        PackageInfo(
            id = id,
            pkg = pkg,
            project = project,
            license = api.getResolvedLicense(id)
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
