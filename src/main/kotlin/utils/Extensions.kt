package org.ossreviewtoolkit.workbench.utils

/**
 * Return the value corresponding to the given [key] or `0` if the key is not present.
 */
fun <K> Map<K, Int>.getOrZero(key: K) = getOrDefault(key, 0)

/**
 * Remove the first line that reads "---" when serializing YAML with Jackson.
 */
// TODO: Create a dedicated YAML mapper with `.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)` instead.
fun String.removeYamlPrefix() = removePrefix("---\n")
