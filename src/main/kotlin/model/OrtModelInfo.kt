package org.ossreviewtoolkit.workbench.model

/**
 * A holder for a summary of a loaded ORT result file to be used in the overview of loaded files.
 */
data class OrtModelInfo(
        /** A name for the ORT result, derived from the ORT result file and its content. */
        val name: String,

        /** The absolute path of the loaded ORT result file. */
        val filePath: String,

        /** The size of the loaded ORT result file. */
        val fileSize: Long,

        /** The count of projects by package manager. */
        val projectsByPackageManager: Map<String, Int>
)
