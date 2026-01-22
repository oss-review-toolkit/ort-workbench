package org.ossreviewtoolkit.workbench.utils

import org.ossreviewtoolkit.workbench.model.ResolutionStatus
import org.ossreviewtoolkit.workbench.model.ResolvedIssue
import org.ossreviewtoolkit.workbench.model.ResolvedRuleViolation
import org.ossreviewtoolkit.workbench.model.ResolvedVulnerability
import org.ossreviewtoolkit.workbench.ui.packages.ExclusionStatus
import org.ossreviewtoolkit.workbench.ui.packages.IssueStatus
import org.ossreviewtoolkit.workbench.ui.packages.ViolationStatus
import org.ossreviewtoolkit.workbench.ui.packages.VulnerabilityStatus

fun matchExclusionStatus(filter: ExclusionStatus?, value: Boolean) = filter == null
    || filter == ExclusionStatus.EXCLUDED && value
    || filter == ExclusionStatus.INCLUDED && !value

fun matchIssueStatus(filter: IssueStatus?, value: List<ResolvedIssue>) = filter == null
    || filter == IssueStatus.HAS_ISSUES && value.isNotEmpty()
    || filter == IssueStatus.NO_ISSUES && value.isEmpty()

fun <T> matchResolutionStatus(filter: ResolutionStatus?, value: List<T>) = filter == null
    || filter == ResolutionStatus.RESOLVED && value.isNotEmpty()
    || filter == ResolutionStatus.UNRESOLVED && value.isEmpty()

fun matchViolationStatus(filter: ViolationStatus?, value: List<ResolvedRuleViolation>) = filter == null
    || filter == ViolationStatus.HAS_VIOLATIONS && value.isNotEmpty()
    || filter == ViolationStatus.NO_VIOLATIONS && value.isEmpty()

fun matchVulnerabilityStatus(filter: VulnerabilityStatus?, value: List<ResolvedVulnerability>) = filter == null
    || filter == VulnerabilityStatus.HAS_VULNERABILITY && value.isNotEmpty()
    || filter == VulnerabilityStatus.NO_VULNERABILITY && value.isEmpty()

fun matchString(filter: String?, vararg values: String) = filter.isNullOrEmpty() || filter in values

fun matchString(filter: String?, values: Collection<String>) = filter.isNullOrEmpty() || filter in values

fun matchStringContains(filter: String?, vararg values: String) =
    filter.isNullOrEmpty() || values.any { it.contains(filter) }

fun matchStringContains(filter: String?, values: List<String>) =
    filter.isNullOrEmpty() || values.any { it.contains(filter) }

fun <T> matchValue(filter: T?, value: T) = filter == null || filter == value

fun <T> matchAnyValue(filter: T?, value: Collection<T>) = filter == null || value.any { it == filter }
