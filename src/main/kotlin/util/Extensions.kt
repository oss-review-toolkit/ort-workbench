package org.ossreviewtoolkit.workbench.util

import org.ossreviewtoolkit.utils.common.titlecase

fun String.enumcase() = replace("_", " ").titlecase()
