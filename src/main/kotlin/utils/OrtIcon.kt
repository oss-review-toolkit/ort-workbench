package org.ossreviewtoolkit.workbench.utils

import org.jetbrains.compose.resources.DrawableResource

import org.ossreviewtoolkit.workbench.ort_workbench.generated.resources.*

enum class OrtIcon(val resource: DrawableResource) {
    ADVISOR(Res.drawable.advisor),
    ANALYZER(Res.drawable.analyzer),
    EVALUATOR(Res.drawable.evaluator),
    SCANNER(Res.drawable.scanner)
}
