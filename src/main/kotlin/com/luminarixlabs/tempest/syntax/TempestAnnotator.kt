package com.luminarixlabs.tempest.syntax

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.luminarixlabs.tempest.settings.TempestSettings
import java.util.regex.Pattern

class TempestAnnotator : Annotator {

    // Improved regex patterns for Tempest syntax
    private val safeInterpolationPattern = Pattern.compile("""\{\{\s*([^}]+?)\s*\}\}""")
    private val unsafeInterpolationPattern = Pattern.compile("""\{!!\s*([^}]+?)\s*!!\}""")
    private val commentPattern = Pattern.compile("""\{\{--\s*(.*?)\s*--\}\}""", Pattern.DOTALL)
    private val attributePattern = Pattern.compile("""(\s+):([\w-]+)(?:\s*=\s*"([^"]*)")?""")
    private val conditionalPattern = Pattern.compile("""(\s+):(if|elseif|else)(?:\s*=\s*"([^"]*)")?""")
    private val loopPattern = Pattern.compile("""(\s+):(foreach|forelse)(?:\s*=\s*"([^"]*)")?""")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Only process if Tempest support is enabled
        if (!TempestSettings.getInstance().isEnabled) {
            return
        }

        // Only process elements in PHP files that might contain Tempest syntax
        if (!isPhpStringContext(element)) {
            return
        }

        val text = element.text
        if (text.isBlank()) return

        // Track excluded ranges to prevent conflicts
        val excludedRanges = mutableSetOf<IntRange>()

        // Process in order of precedence - comments first to prevent conflicts
        processComments(element, holder, text, excludedRanges)
        processInterpolations(element, holder, text, excludedRanges)
        processAttributes(element, holder, text, excludedRanges)
        processConditionals(element, holder, text, excludedRanges)
        processLoops(element, holder, text, excludedRanges)
    }

    private fun isPhpStringContext(element: PsiElement): Boolean {
        // Check if we're in a PHP context that might contain Tempest syntax
        return element.containingFile?.name?.endsWith(".view.php") == true
    }

    private fun isRangeExcluded(start: Int, end: Int, excludedRanges: Set<IntRange>): Boolean {
        return excludedRanges.any { excludedRange ->
            // Check if any part of the range overlaps with excluded ranges
            start < excludedRange.last && end > excludedRange.first
        }
    }

    private fun processComments(
        element: PsiElement,
        holder: AnnotationHolder,
        text: String,
        excludedRanges: MutableSet<IntRange>
    ) {
        val matcher = commentPattern.matcher(text)
        while (matcher.find()) {
            val startOffset = element.textRange.startOffset + matcher.start()
            val endOffset = element.textRange.startOffset + matcher.end()

            // Mark this range as excluded from other processing
            excludedRanges.add(matcher.start() until matcher.end())

            // Highlight the entire comment block with comment color
            createAnnotation(holder, startOffset, endOffset, TempestTokens.COMMENT_KEY, "Tempest comment")
        }
    }

    private fun processInterpolations(
        element: PsiElement,
        holder: AnnotationHolder,
        text: String,
        excludedRanges: Set<IntRange>
    ) {
        // Process safe interpolations {{ }}
        val safeMatcher = safeInterpolationPattern.matcher(text)
        while (safeMatcher.find()) {
            // Skip if this range is excluded (e.g., inside a comment)
            if (isRangeExcluded(safeMatcher.start(), safeMatcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + safeMatcher.start()
            val endOffset = element.textRange.startOffset + safeMatcher.end()

            // Highlight the opening braces
            createAnnotation(
                holder,
                startOffset,
                startOffset + 2,
                TempestTokens.SAFE_INTERPOLATION_KEY,
                "Safe interpolation start"
            )

            // Highlight the closing braces
            createAnnotation(
                holder,
                endOffset - 2,
                endOffset,
                TempestTokens.SAFE_INTERPOLATION_KEY,
                "Safe interpolation end"
            )

            // Highlight the content (excluding the braces)
            val contentStart = startOffset + 2
            val contentEnd = endOffset - 2
            if (contentStart < contentEnd) {
                createAnnotation(
                    holder,
                    contentStart,
                    contentEnd,
                    TempestTokens.INTERPOLATION_CONTENT_KEY,
                    "Interpolation content"
                )
            }
        }

        // Process unsafe interpolations {!! !!}
        val unsafeMatcher = unsafeInterpolationPattern.matcher(text)
        while (unsafeMatcher.find()) {
            // Skip if this range is excluded (e.g., inside a comment)
            if (isRangeExcluded(unsafeMatcher.start(), unsafeMatcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + unsafeMatcher.start()
            val endOffset = element.textRange.startOffset + unsafeMatcher.end()

            // Highlight the opening braces
            createAnnotation(
                holder,
                startOffset,
                startOffset + 3,
                TempestTokens.UNSAFE_INTERPOLATION_KEY,
                "Unsafe interpolation start"
            )

            // Highlight the closing braces
            createAnnotation(
                holder,
                endOffset - 3,
                endOffset,
                TempestTokens.UNSAFE_INTERPOLATION_KEY,
                "Unsafe interpolation end"
            )

            // Highlight the content (excluding the braces)
            val contentStart = startOffset + 3
            val contentEnd = endOffset - 3
            if (contentStart < contentEnd) {
                createAnnotation(
                    holder,
                    contentStart,
                    contentEnd,
                    TempestTokens.INTERPOLATION_CONTENT_KEY,
                    "Interpolation content"
                )
            }
        }
    }

    private fun processAttributes(
        element: PsiElement,
        holder: AnnotationHolder,
        text: String,
        excludedRanges: Set<IntRange>
    ) {
        val matcher = attributePattern.matcher(text)
        while (matcher.find()) {
            // Skip if this range is excluded
            if (isRangeExcluded(matcher.start(), matcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + matcher.start()
            val attributeStart = startOffset + matcher.group(1).length // Skip whitespace
            val colonEnd = attributeStart + 1 // Just the colon
            val nameEnd = attributeStart + 1 + matcher.group(2).length

            // Highlight the colon and attribute name
            createAnnotation(holder, colonEnd, nameEnd, TempestTokens.ATTRIBUTE_NAME_KEY, "Tempest attribute")

            // Highlight the value if present
            val value = matcher.group(3)
            if (value != null) {
                val valueStart =
                    text.indexOf("\"", nameEnd - element.textRange.startOffset) + element.textRange.startOffset
                val valueEnd = text.indexOf(
                    "\"",
                    valueStart - element.textRange.startOffset + 1
                ) + element.textRange.startOffset + 1
                if (valueStart >= 0 && valueEnd > valueStart) {
                    createAnnotation(
                        holder,
                        valueStart,
                        valueEnd,
                        TempestTokens.ATTRIBUTE_VALUE_KEY,
                        "Tempest attribute value"
                    )
                }
            }
        }
    }

    private fun processConditionals(
        element: PsiElement,
        holder: AnnotationHolder,
        text: String,
        excludedRanges: Set<IntRange>
    ) {
        val matcher = conditionalPattern.matcher(text)
        while (matcher.find()) {
            // Skip if this range is excluded
            if (isRangeExcluded(matcher.start(), matcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + matcher.start()
            val conditionalStart = startOffset + matcher.group(1).length // Skip whitespace
            val conditionalEnd = conditionalStart + 1 + matcher.group(2).length

            createAnnotation(
                holder,
                conditionalStart,
                conditionalEnd,
                TempestTokens.CONDITIONAL_KEY,
                "Tempest conditional"
            )

            // Highlight the value if present
            val value = matcher.group(3)
            if (value != null) {
                val valueStart =
                    text.indexOf("\"", conditionalEnd - element.textRange.startOffset) + element.textRange.startOffset
                val valueEnd = text.indexOf(
                    "\"",
                    valueStart - element.textRange.startOffset + 1
                ) + element.textRange.startOffset + 1
                if (valueStart >= 0 && valueEnd > valueStart) {
                    createAnnotation(
                        holder,
                        valueStart,
                        valueEnd,
                        TempestTokens.ATTRIBUTE_VALUE_KEY,
                        "Tempest conditional value"
                    )
                }
            }
        }
    }

    private fun processLoops(
        element: PsiElement,
        holder: AnnotationHolder,
        text: String,
        excludedRanges: Set<IntRange>
    ) {
        val matcher = loopPattern.matcher(text)
        while (matcher.find()) {
            // Skip if this range is excluded
            if (isRangeExcluded(matcher.start(), matcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + matcher.start()
            val loopStart = startOffset + matcher.group(1).length // Skip whitespace
            val loopEnd = loopStart + 1 + matcher.group(2).length

            createAnnotation(holder, loopStart, loopEnd, TempestTokens.LOOP_KEY, "Tempest loop")

            // Highlight the value if present
            val value = matcher.group(3)
            if (value != null) {
                val valueStart =
                    text.indexOf("\"", loopEnd - element.textRange.startOffset) + element.textRange.startOffset
                val valueEnd = text.indexOf(
                    "\"",
                    valueStart - element.textRange.startOffset + 1
                ) + element.textRange.startOffset + 1
                if (valueStart >= 0 && valueEnd > valueStart) {
                    createAnnotation(
                        holder,
                        valueStart,
                        valueEnd,
                        TempestTokens.ATTRIBUTE_VALUE_KEY,
                        "Tempest loop value"
                    )
                }
            }
        }
    }

    private fun createAnnotation(
        holder: AnnotationHolder,
        startOffset: Int,
        endOffset: Int,
        key: com.intellij.openapi.editor.colors.TextAttributesKey,
        message: String
    ) {
        if (startOffset < endOffset) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(startOffset, endOffset))
                .textAttributes(key)
                .create()
        }
    }
}
