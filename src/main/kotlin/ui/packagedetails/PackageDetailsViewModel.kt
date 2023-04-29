package org.ossreviewtoolkit.workbench.ui.packagedetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import kotlinx.coroutines.flow.Flow

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.workbench.lifecycle.MoleculeViewModel
import org.ossreviewtoolkit.workbench.model.OrtModel

class PackageDetailsViewModel(private val ortModel: OrtModel, private val pkgId: Identifier) :
    MoleculeViewModel<Unit, PackageDetailsState>() {

    @Composable
    override fun composeModel(events: Flow<Unit>): PackageDetailsState {
        return PackageDetailsPresenter(ortModel, pkgId)
    }
}

@Composable
fun PackageDetailsPresenter(
    ortModel: OrtModel,
    pkgId: Identifier
): PackageDetailsState {
    val api by ortModel.api.collectAsState()
    val packageInfo = remember(pkgId) {
        val pkg = api.getCuratedPackageOrProject(pkgId)
        val project = api.getProject(pkgId)

        PackageInfo(
            id = pkgId,
            pkg = pkg,
            project = project,
            license = api.getResolvedLicense(pkgId)
        )
    }

    return PackageDetailsState(packageInfo)
}
