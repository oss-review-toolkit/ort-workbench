package org.ossreviewtoolkit.workbench.utils

import org.ossreviewtoolkit.utils.spdx.SpdxExpression

class SpdxExpressionStringComparator : Comparator<SpdxExpression> {
    override fun compare(left: SpdxExpression?, right: SpdxExpression?): Int =
        compareValues(left?.toString(), right?.toString())
}
