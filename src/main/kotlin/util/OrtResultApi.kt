package org.ossreviewtoolkit.workbench.util

import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.config.CopyrightGarbage
import org.ossreviewtoolkit.model.config.OrtConfiguration
import org.ossreviewtoolkit.model.config.createFileArchiver
import org.ossreviewtoolkit.model.licenses.DefaultLicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoProvider
import org.ossreviewtoolkit.model.licenses.LicenseInfoResolver
import org.ossreviewtoolkit.model.readValue
import org.ossreviewtoolkit.model.utils.DefaultResolutionProvider
import org.ossreviewtoolkit.model.utils.FileArchiver
import org.ossreviewtoolkit.model.utils.PackageConfigurationProvider
import org.ossreviewtoolkit.model.utils.ResolutionProvider
import org.ossreviewtoolkit.model.utils.SimplePackageConfigurationProvider
import org.ossreviewtoolkit.model.utils.createLicenseInfoResolver
import org.ossreviewtoolkit.utils.core.ORT_CONFIG_FILENAME
import org.ossreviewtoolkit.utils.core.ORT_COPYRIGHT_GARBAGE_FILENAME
import org.ossreviewtoolkit.utils.core.ORT_RESOLUTIONS_FILENAME
import org.ossreviewtoolkit.utils.core.ortConfigDirectory

class OrtResultApi(
    val result: OrtResult,
) {
    val config: OrtConfiguration
    val copyrightGarbage: CopyrightGarbage
    val fileArchiver: FileArchiver
    val licenseInfoProvider: LicenseInfoProvider
    val licenseInfoResolver = result.createLicenseInfoResolver()
    val packageConfigurationProvider: PackageConfigurationProvider
    val resolutionProvider: ResolutionProvider

    init {
        config = OrtConfiguration.load(file = ortConfigDirectory.resolve(ORT_CONFIG_FILENAME))
        copyrightGarbage = ortConfigDirectory.resolve(ORT_COPYRIGHT_GARBAGE_FILENAME).takeIf { it.isFile }?.readValue()
            ?: CopyrightGarbage()
        fileArchiver = config.scanner.archive.createFileArchiver()
        // TODO: Let ORT provide a default package configuration location.
        packageConfigurationProvider = SimplePackageConfigurationProvider.EMPTY
        licenseInfoProvider = DefaultLicenseInfoProvider(result, packageConfigurationProvider)

        val resolutionsFile = ortConfigDirectory.resolve(ORT_RESOLUTIONS_FILENAME)
        resolutionProvider = DefaultResolutionProvider.create(result, resolutionsFile)

        LicenseInfoResolver(
            provider = licenseInfoProvider,
            copyrightGarbage = copyrightGarbage,
            addAuthorsToCopyrights = config.addAuthorsToCopyrights,
            archiver = fileArchiver,
            licenseFilenamePatterns = config.licenseFilePatterns
        )
    }
}
