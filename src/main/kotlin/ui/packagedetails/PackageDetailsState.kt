package org.ossreviewtoolkit.workbench.ui.packagedetails

import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.licenses.ResolvedLicenseInfo

data class PackageInfo(
    val id: Identifier,
    val pkg: CuratedPackage?,
    val project: Project?,
    val license: ResolvedLicenseInfo
)

data class PackageDetailsState(
    val packageInfo: PackageInfo?
)
