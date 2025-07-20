package com.luminarixlabs.tempest.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext
import com.luminarixlabs.tempest.settings.TempestSettings

/**
 * Provides attribute name completions for Tempest expression attributes in HTML contexts.
 */
class TempestCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(XmlAttribute::class.java)
                .inside(PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(".view.php"))),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    resultSet: CompletionResultSet
                ) {
                    if (!TempestSettings.getInstance().isEnabled) {
                        return
                    }
                    val items = listOf(
                        ":if=\"\"" to 1,
                        ":elseif=\"\"" to 1,
                        ":else" to 0,
                        ":foreach=\"\"" to 1,
                        ":forelse=\"\"" to 1
                    )
                    for ((text, back) in items) {
                        val element = LookupElementBuilder.create(text)
                            .withInsertHandler { ctx, _ ->
                                if (back > 0) {
                                    val pos = ctx.selectionEndOffset
                                    ctx.editor.caretModel.moveToOffset(pos - back)
                                }
                            }
                        resultSet.addElement(element)
                    }
                }
            }
        )
    }
}
