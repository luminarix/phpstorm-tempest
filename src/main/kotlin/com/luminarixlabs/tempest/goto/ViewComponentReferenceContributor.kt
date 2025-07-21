package com.luminarixlabs.tempest.goto

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class ViewComponentReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(XmlTag::class.java),
            ViewComponentReferenceProvider()
        )
    }
}

class ViewComponentReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val xmlTag = element as? XmlTag ?: return PsiReference.EMPTY_ARRAY
        val tagName = xmlTag.name
        
        if (!tagName.startsWith("x-")) {
            return PsiReference.EMPTY_ARRAY
        }
        
        val nameElement = xmlTag.firstChild?.nextSibling ?: return PsiReference.EMPTY_ARRAY
        
        return arrayOf(ViewComponentReference(xmlTag, tagName, nameElement))
    }
}

class ViewComponentReference(
    element: XmlTag,
    private val componentName: String,
    private val nameElement: PsiElement
) : PsiReferenceBase<XmlTag>(element, TextRange.from(nameElement.textOffset - element.textOffset, componentName.length)), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        return multiResolve(false).firstOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        val results = mutableListOf<ResolveResult>()
        
        findFileBasedComponent(project)?.let { results.add(PsiElementResolveResult(it)) }
        
        findClassBasedComponent(project).forEach { results.add(PsiElementResolveResult(it)) }
        
        return results.toTypedArray()
    }
    
    private fun findFileBasedComponent(project: Project): PsiFile? {
        val fileName = "$componentName.view.php"
        val scope = GlobalSearchScope.allScope(project)
        
        val virtualFiles = FilenameIndex.getVirtualFilesByName(fileName, scope)
        
        for (virtualFile in virtualFiles) {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile != null) {
                return psiFile
            }
        }
        
        return null
    }
    
    private fun findClassBasedComponent(project: Project): List<PhpClass> {
        val phpIndex = PhpIndex.getInstance(project)
        val results = mutableListOf<PhpClass>()
        
        val viewComponentInterfaces = setOf("\\Tempest\\View\\ViewComponent", "Tempest\\View\\ViewComponent")
        
        for (interfaceName in viewComponentInterfaces) {
            val viewComponentClasses = phpIndex.getAllSubclasses(interfaceName)
            
            for (phpClass in viewComponentClasses) {
                if (isMatchingViewComponent(phpClass)) {
                    results.add(phpClass)
                }
            }
        }
        
        return results
    }
    
    private fun isMatchingViewComponent(phpClass: PhpClass): Boolean {
        val getNameMethod = phpClass.findMethodByName("getName") ?: return false
        
        val methodBody = getNameMethod.firstPsiChild
        
        val stringLiterals = mutableListOf<StringLiteralExpression>()
        collectStringLiterals(methodBody, stringLiterals)
        
        return stringLiterals.any { it.contents == componentName }
    }
    
    private fun collectStringLiterals(element: PsiElement?, literals: MutableList<StringLiteralExpression>) {
        if (element == null) return
        
        if (element is StringLiteralExpression) {
            literals.add(element)
        }
        
        for (child in element.children) {
            collectStringLiterals(child, literals)
        }
    }
}