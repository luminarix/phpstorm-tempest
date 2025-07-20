package com.luminarixlabs.tempest.syntax

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class TempestLexer : LexerBase() {
    
    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0
    private var state = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null
    
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.state = initialState
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        this.tokenType = null
        advance()
    }
    
    override fun advance() {
        tokenStart = tokenEnd
        
        if (tokenEnd >= endOffset) {
            tokenType = null
            return
        }
        
        // For now, let's use a simple approach that doesn't cause issues
        // The annotator will handle the actual syntax highlighting
        tokenEnd = kotlin.math.min(tokenEnd + 1, endOffset)
        tokenType = null
    }
    
    override fun getTokenType(): IElementType? = tokenType
    
    override fun getTokenStart(): Int = tokenStart
    
    override fun getTokenEnd(): Int = tokenEnd
    
    override fun getBufferSequence(): CharSequence = buffer
    
    override fun getBufferEnd(): Int = endOffset
    
    override fun getState(): Int = state
}