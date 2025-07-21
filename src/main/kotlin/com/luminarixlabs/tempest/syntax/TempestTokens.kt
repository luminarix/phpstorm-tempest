package com.luminarixlabs.tempest.syntax

import com.intellij.lang.Language
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.tree.IElementType

object TempestTokens {

    class TempestElementType(debugName: String) : IElementType(debugName, Language.ANY)

    val SAFE_INTERPOLATION_START = TempestElementType("TEMPEST_SAFE_INTERPOLATION_START")
    val SAFE_INTERPOLATION_END = TempestElementType("TEMPEST_SAFE_INTERPOLATION_END")
    val UNSAFE_INTERPOLATION_START = TempestElementType("TEMPEST_UNSAFE_INTERPOLATION_START")
    val UNSAFE_INTERPOLATION_END = TempestElementType("TEMPEST_UNSAFE_INTERPOLATION_END")
    val INTERPOLATION_CONTENT = TempestElementType("TEMPEST_INTERPOLATION_CONTENT")

    val COMMENT_START = TempestElementType("TEMPEST_COMMENT_START")
    val COMMENT_END = TempestElementType("TEMPEST_COMMENT_END")
    val COMMENT_CONTENT = TempestElementType("TEMPEST_COMMENT_CONTENT")

    val ATTRIBUTE_PREFIX = TempestElementType("TEMPEST_ATTRIBUTE_PREFIX")
    val ATTRIBUTE_NAME = TempestElementType("TEMPEST_ATTRIBUTE_NAME")
    val ATTRIBUTE_EQUALS = TempestElementType("TEMPEST_ATTRIBUTE_EQUALS")
    val ATTRIBUTE_VALUE = TempestElementType("TEMPEST_ATTRIBUTE_VALUE")

    val CONDITIONAL_IF = TempestElementType("TEMPEST_CONDITIONAL_IF")
    val CONDITIONAL_ELSEIF = TempestElementType("TEMPEST_CONDITIONAL_ELSEIF")
    val CONDITIONAL_ELSE = TempestElementType("TEMPEST_CONDITIONAL_ELSE")

    val LOOP_FOREACH = TempestElementType("TEMPEST_LOOP_FOREACH")
    val LOOP_FORELSE = TempestElementType("TEMPEST_LOOP_FORELSE")

    val SAFE_INTERPOLATION_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_SAFE_INTERPOLATION",
        DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR
    )

    val UNSAFE_INTERPOLATION_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_UNSAFE_INTERPOLATION",
        DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
    )

    val COMMENT_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_COMMENT",
        DefaultLanguageHighlighterColors.BLOCK_COMMENT
    )

    val COMMENT_DELIMITER_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_COMMENT_DELIMITER",
        DefaultLanguageHighlighterColors.BLOCK_COMMENT
    )

    val ATTRIBUTE_NAME_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_ATTRIBUTE_NAME",
        DefaultLanguageHighlighterColors.METADATA
    )

    val ATTRIBUTE_VALUE_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_ATTRIBUTE_VALUE",
        DefaultLanguageHighlighterColors.STRING
    )

    val CONDITIONAL_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_CONDITIONAL",
        DefaultLanguageHighlighterColors.KEYWORD
    )

    val LOOP_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_LOOP",
        DefaultLanguageHighlighterColors.KEYWORD
    )

    val INTERPOLATION_CONTENT_KEY = TextAttributesKey.createTextAttributesKey(
        "TEMPEST_INTERPOLATION_CONTENT",
        DefaultLanguageHighlighterColors.IDENTIFIER
    )
}