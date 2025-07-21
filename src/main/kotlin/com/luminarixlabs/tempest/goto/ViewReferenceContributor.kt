package com.luminarixlabs.tempest.goto

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import java.nio.file.Paths

class ViewReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java),
            ViewReferenceProvider()
        )
    }
}

class ViewReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as? StringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
        val functionCall = PsiTreeUtil.getParentOfType(stringLiteral, FunctionReference::class.java) ?: return PsiReference.EMPTY_ARRAY

        if (functionCall.name?.lowercase() != "view") {
            return PsiReference.EMPTY_ARRAY
        }

        val resolvedFunction = functionCall.resolve()

        if ((resolvedFunction as? PhpNamedElement)?.fqn != "\\Tempest\\view") {
            return PsiReference.EMPTY_ARRAY
        }

        val viewPath = stringLiteral.contents
        if (viewPath.isEmpty()) {
            return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(ViewReference(stringLiteral, viewPath))
    }
}


class ViewReference(element: StringLiteralExpression, private val viewPath: String) :
    PsiReferenceBase<StringLiteralExpression>(element, TextRange(1, element.textLength - 1)), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        return multiResolve(false).firstOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val containingFile = element.containingFile
        val containingDir = containingFile.virtualFile.parent

        val path = Paths.get(containingDir.path, viewPath).normalize()
        val virtualFile = containingDir.fileSystem.findFileByPath(path.toString()) ?: return ResolveResult.EMPTY_ARRAY

        val psiFile = PsiManager.getInstance(element.project).findFile(virtualFile) ?: return ResolveResult.EMPTY_ARRAY
        return arrayOf(PsiElementResolveResult(psiFile))
    }
}
