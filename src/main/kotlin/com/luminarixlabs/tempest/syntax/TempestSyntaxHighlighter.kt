package com.luminarixlabs.tempest.syntax

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.luminarixlabs.tempest.settings.TempestSettings

class TempestSyntaxHighlighter : SyntaxHighlighterBase() {
    
    override fun getHighlightingLexer(): Lexer {
        return TempestLexer()
    }
    
    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        // Check if Tempest highlighting is enabled
        if (!TempestSettings.getInstance().isEnabled) {
            return EMPTY_KEYS
        }
        
        return when (tokenType) {
            // Comments - highest precedence
            TempestTokens.COMMENT_START,
            TempestTokens.COMMENT_END,
            TempestTokens.COMMENT_CONTENT -> arrayOf(TempestTokens.COMMENT_KEY)
            
            // Safe interpolation braces
            TempestTokens.SAFE_INTERPOLATION_START,
            TempestTokens.SAFE_INTERPOLATION_END -> arrayOf(TempestTokens.SAFE_INTERPOLATION_KEY)
            
            // Unsafe interpolation braces
            TempestTokens.UNSAFE_INTERPOLATION_START,
            TempestTokens.UNSAFE_INTERPOLATION_END -> arrayOf(TempestTokens.UNSAFE_INTERPOLATION_KEY)
            
            // Interpolation content - different highlighting based on context
            TempestTokens.INTERPOLATION_CONTENT -> arrayOf(TempestTokens.INTERPOLATION_CONTENT_KEY)
            
            // Attributes
            TempestTokens.ATTRIBUTE_PREFIX,
            TempestTokens.ATTRIBUTE_NAME -> arrayOf(TempestTokens.ATTRIBUTE_NAME_KEY)
            
            TempestTokens.ATTRIBUTE_EQUALS -> arrayOf(TempestTokens.ATTRIBUTE_NAME_KEY)
            TempestTokens.ATTRIBUTE_VALUE -> arrayOf(TempestTokens.ATTRIBUTE_VALUE_KEY)
            
            // Conditionals
            TempestTokens.CONDITIONAL_IF,
            TempestTokens.CONDITIONAL_ELSEIF,
            TempestTokens.CONDITIONAL_ELSE -> arrayOf(TempestTokens.CONDITIONAL_KEY)
            
            // Loops
            TempestTokens.LOOP_FOREACH,
            TempestTokens.LOOP_FORELSE -> arrayOf(TempestTokens.LOOP_KEY)
            
            else -> EMPTY_KEYS
        }
    }
    
    companion object {
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }
}
