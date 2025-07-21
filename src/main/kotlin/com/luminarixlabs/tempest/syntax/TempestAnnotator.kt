package com.luminarixlabs.tempest.syntax

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.luminarixlabs.tempest.settings.TempestSettings
import java.util.regex.Pattern

class TempestAnnotator : Annotator {

    private val safeInterpolationPattern = Pattern.compile("""\{\{\s*([^}]+?)\s*\}\}""")
    private val unsafeInterpolationPattern = Pattern.compile("""\{!!\s*([^}]+?)\s*!!\}""")
    private val commentPattern = Pattern.compile("""\{\{--\s*(.*?)\s*--\}\}""", Pattern.DOTALL)
    private val attributePattern = Pattern.compile("""(\s+):([\w-]+)(?:\s*=\s*"([^"]*)")?""")
    private val conditionalPattern = Pattern.compile("""(\s+):(if|elseif|else)(?:\s*=\s*"([^"]*)")?""")
    private val loopPattern = Pattern.compile("""(\s+):(foreach|forelse)(?:\s*=\s*"([^"]*)")?""")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!TempestSettings.getInstance().isEnabled) {
            return
        }

        if (!isTempestViewFile(element)) {
            return
        }

        val text = element.text
        if (text.isBlank()) return

        val excludedRanges = mutableSetOf<IntRange>()

        processComments(element, holder, text, excludedRanges)
        processInterpolations(element, holder, text, excludedRanges)
        processConditionals(element, holder, text, excludedRanges)
        processLoops(element, holder, text, excludedRanges)
        processAttributes(element, holder, text, excludedRanges)
    }

    private fun isTempestViewFile(element: PsiElement): Boolean {
        return element.containingFile?.name?.endsWith(".view.php") == true
    }

    private fun isRangeExcluded(start: Int, end: Int, excludedRanges: Set<IntRange>): Boolean {
        return excludedRanges.any { excludedRange ->
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

            excludedRanges.add(matcher.start() until matcher.end())

            createAnnotation(holder, startOffset, endOffset, TempestTokens.COMMENT_KEY, "Tempest comment")
        }
    }

    private fun processInterpolations(
        element: PsiElement,
        holder: AnnotationHolder,
        text: String,
        excludedRanges: Set<IntRange>
    ) {
        val safeMatcher = safeInterpolationPattern.matcher(text)
        while (safeMatcher.find()) {
            if (isRangeExcluded(safeMatcher.start(), safeMatcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + safeMatcher.start()
            val endOffset = element.textRange.startOffset + safeMatcher.end()

            createAnnotation(
                holder,
                startOffset,
                startOffset + 2,
                TempestTokens.SAFE_INTERPOLATION_KEY,
                "Safe interpolation start"
            )

            createAnnotation(
                holder,
                endOffset - 2,
                endOffset,
                TempestTokens.SAFE_INTERPOLATION_KEY,
                "Safe interpolation end"
            )

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

        val unsafeMatcher = unsafeInterpolationPattern.matcher(text)
        while (unsafeMatcher.find()) {
            if (isRangeExcluded(unsafeMatcher.start(), unsafeMatcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + unsafeMatcher.start()
            val endOffset = element.textRange.startOffset + unsafeMatcher.end()

            createAnnotation(
                holder,
                startOffset,
                startOffset + 3,
                TempestTokens.UNSAFE_INTERPOLATION_KEY,
                "Unsafe interpolation start"
            )

            createAnnotation(
                holder,
                endOffset - 3,
                endOffset,
                TempestTokens.UNSAFE_INTERPOLATION_KEY,
                "Unsafe interpolation end"
            )

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
            if (isRangeExcluded(matcher.start(), matcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + matcher.start()
            val attributeStart = startOffset + matcher.group(1).length
            val colonEnd = attributeStart + 1
            val nameEnd = attributeStart + 1 + matcher.group(2).length

            createAnnotation(holder, colonEnd, nameEnd, TempestTokens.ATTRIBUTE_NAME_KEY, "Tempest attribute")

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
        excludedRanges: MutableSet<IntRange>
    ) {
        val matcher = conditionalPattern.matcher(text)
        while (matcher.find()) {
            if (isRangeExcluded(matcher.start(), matcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + matcher.start()
            val conditionalStart = startOffset + matcher.group(1).length
            val conditionalEnd = conditionalStart + 1 + matcher.group(2).length

            excludedRanges.add(matcher.start() until matcher.end())

            createAnnotation(
                holder,
                conditionalStart,
                conditionalEnd,
                TempestTokens.CONDITIONAL_KEY,
                "Tempest conditional"
            )

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
        excludedRanges: MutableSet<IntRange>
    ) {
        val matcher = loopPattern.matcher(text)
        while (matcher.find()) {
            if (isRangeExcluded(matcher.start(), matcher.end(), excludedRanges)) {
                continue
            }

            val startOffset = element.textRange.startOffset + matcher.start()
            val loopStart = startOffset + matcher.group(1).length
            val loopEnd = loopStart + 1 + matcher.group(2).length

            excludedRanges.add(matcher.start() until matcher.end())

            createAnnotation(holder, loopStart, loopEnd, TempestTokens.LOOP_KEY, "Tempest loop")

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
