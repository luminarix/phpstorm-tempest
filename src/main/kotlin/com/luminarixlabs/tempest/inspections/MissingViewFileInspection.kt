package com.luminarixlabs.tempest.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.luminarixlabs.tempest.goto.ViewReference

class MissingViewFileInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpStringLiteralExpression(expression: StringLiteralExpression) {
                val viewReference = expression.references.find { it is ViewReference } as? ViewReference
                if (viewReference != null && viewReference.resolve() == null) {
                    holder.registerProblem(expression, "View file not found")
                }
            }
        }
    }
}
