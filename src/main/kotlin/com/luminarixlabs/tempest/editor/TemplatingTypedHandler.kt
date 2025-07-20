package com.luminarixlabs.tempest.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.luminarixlabs.tempest.settings.TempestSettings

/**
 * Inserts closing templating tokens when typing space after opening tokens.
 */
class TemplatingTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (!TempestSettings.getInstance().isEnabled) {
            return Result.CONTINUE
        }

        if (!file.name.endsWith(".view.php")) {
            return Result.CONTINUE
        }

        if (c != ' ') {
            return Result.CONTINUE
        }

        val document = editor.document
        val offset = editor.caretModel.offset
        val seq = document.charsSequence
        // {{ -> }}
        if (offset >= 3 && seq[offset - 3] == '{' && seq[offset - 2] == '{') {
            document.insertString(offset, " }}")
            return Result.STOP
        }
        // {!! -> !!}}
        if (offset >= 5 && seq[offset - 5] == '{' && seq[offset - 4] == '{' && seq[offset - 3] == '!' && seq[offset - 2] == '!') {
            document.insertString(offset, " !!}}")
            return Result.STOP
        }
        // {{-- -> --}}
        if (offset >= 5 && seq[offset - 5] == '{' && seq[offset - 4] == '{' && seq[offset - 3] == '-' && seq[offset - 2] == '-') {
            document.insertString(offset, " --}}")
            return Result.STOP
        }
        return Result.CONTINUE
    }
}
