package com.luminarixlabs.tempest.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.luminarixlabs.tempest.settings.TempestSettings

class TempestToggleAction : ToggleAction("Enable Tempest Support", "Enable or disable Tempest syntax highlighting", null), DumbAware {
    
    override fun isSelected(e: AnActionEvent): Boolean {
        return TempestSettings.getInstance().isEnabled
    }
    
    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val settings = TempestSettings.getInstance()
        settings.isEnabled = state
        
        // Refresh all open editors to apply/remove highlighting
        e.project?.let { project ->
            ApplicationManager.getApplication().runReadAction {
                val fileEditorManager = FileEditorManager.getInstance(project)
                fileEditorManager.allEditors.forEach { editor ->
                    // Force rehighlight by simulating file change
                    editor.file?.let { file ->
                        ApplicationManager.getApplication().invokeLater {
                            fileEditorManager.closeFile(file)
                            fileEditorManager.openFile(file, true)
                        }
                    }
                }
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.text = if (isSelected(e)) {
            "Disable Tempest Support"
        } else {
            "Enable Tempest Support"
        }
    }
}