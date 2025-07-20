package com.luminarixlabs.tempest.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.luminarixlabs.tempest.syntax.TempestSyntaxHighlighter
import com.luminarixlabs.tempest.syntax.TempestTokens
import javax.swing.Icon

class TempestColorSettingsPage : ColorSettingsPage {
    
    override fun getDisplayName(): String = "Tempest"
    
    override fun getIcon(): Icon? = null
    
    override fun getHighlighter(): SyntaxHighlighter = TempestSyntaxHighlighter()
    
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = arrayOf(
        AttributesDescriptor("Safe Interpolation", TempestTokens.SAFE_INTERPOLATION_KEY),
        AttributesDescriptor("Unsafe Interpolation", TempestTokens.UNSAFE_INTERPOLATION_KEY),
        AttributesDescriptor("Comments", TempestTokens.COMMENT_KEY),
        AttributesDescriptor("Attribute Names", TempestTokens.ATTRIBUTE_NAME_KEY),
        AttributesDescriptor("Attribute Values", TempestTokens.ATTRIBUTE_VALUE_KEY),
        AttributesDescriptor("Conditionals", TempestTokens.CONDITIONAL_KEY),
        AttributesDescriptor("Loops", TempestTokens.LOOP_KEY),
        AttributesDescriptor("Interpolation Content", TempestTokens.INTERPOLATION_CONTENT_KEY),
        AttributesDescriptor("Braces", TempestTokens.BRACES_KEY)
    )
    
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    
    override fun getDemoText(): String = """
        <?php
        
        namespace App\View\Components;
        
        use Tempest\View\ViewComponent;
        
        class UserProfile extends ViewComponent
        {
            public function __construct(
                public string ${'$'}name,
                public string ${'$'}email,
                public array ${'$'}classes = ['user-profile'],
                public bool ${'$'}isActive = true
            ) {}
            
            public function render(): string
            {
                return '
                    <div <attr>:class</attr>="<attrval>${'$'}classes</attrval>">
                        <comment>{{-- User profile component --}}</comment>
                        
                        <conditional>:if</conditional>="<attrval>${'$'}isActive</attrval>">
                            <h2>Welcome, <safe>{{ ${'$'}name }}</safe>!</h2>
                            <p>Email: <safe>{{ ${'$'}email }}</safe></p>
                        </div>
                        
                        <div <conditional>:else</conditional>>
                            <p>User is not active</p>
                        </div>
                        
                        <ul>
                            <li <loop>:foreach</loop>="<attrval>${'$'}this->links as ${'$'}link</attrval>">
                                <a href="<unsafe>{!! ${'$'}link->url !!}</unsafe>">
                                    <safe>{{ ${'$'}link->title }}</safe>
                                </a>
                            </li>
                            <li <loop>:forelse</loop>>
                                No links available
                            </li>
                        </ul>
                    </div>
                ';
            }
        }
        """.trimIndent()
    
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = mapOf(
        "safe" to TempestTokens.SAFE_INTERPOLATION_KEY,
        "unsafe" to TempestTokens.UNSAFE_INTERPOLATION_KEY,
        "comment" to TempestTokens.COMMENT_KEY,
        "attr" to TempestTokens.ATTRIBUTE_NAME_KEY,
        "attrval" to TempestTokens.ATTRIBUTE_VALUE_KEY,
        "conditional" to TempestTokens.CONDITIONAL_KEY,
        "loop" to TempestTokens.LOOP_KEY
    )
}