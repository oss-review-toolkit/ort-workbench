package org.ossreviewtoolkit.workbench.model

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.RuleViolation
import org.ossreviewtoolkit.model.Severity
import org.ossreviewtoolkit.model.config.RuleViolationResolution
import org.ossreviewtoolkit.utils.spdx.SpdxSingleLicenseExpression

data class ResolvedRuleViolation(
    val pkg: Identifier?,
    val rule: String,
    val license: SpdxSingleLicenseExpression?,
    val licenseSources: Set<LicenseSource>,
    val severity: Severity,
    val message: String,
    val howToFix: String,
    val resolutions: List<RuleViolationResolution>
) {
    constructor(
        resolutions: List<RuleViolationResolution>,
        violation: RuleViolation
    ) : this(
        violation.pkg,
        violation.rule,
        violation.license,
        violation.licenseSources,
        violation.severity,
        violation.message,
        violation.howToFix,
        resolutions
    )
}
