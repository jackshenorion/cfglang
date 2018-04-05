package com.jackshenorion.cfgplugin;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jackshenorion.cfgplugin.psi.CfgElementType;
import com.jackshenorion.cfgplugin.psi.CfgProperty;
import com.jackshenorion.cfgplugin.psi.CfgSegment;
import com.jackshenorion.cfgplugin.psi.CfgTypes;
import com.jackshenorion.cfgplugin.psi.impl.CfgPropertyImpl;
import com.jackshenorion.cfgplugin.psi.impl.CfgSegmentImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.codeInsight.completion.CompletionUtil.DUMMY_IDENTIFIER;

public class CfgReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
//        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiElement.class),
//                new PsiReferenceProvider() {
//                    @NotNull
//                    @Override
//                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
//                                                                 @NotNull ProcessingContext
//                                                                         context) {
//                        System.out.println(element + ":" + element.getText());
//                        return PsiReference.EMPTY_ARRAY;
//                    }
//                });

        registrar.registerReferenceProvider(PlatformPatterns.psiElement(CfgTypes.PROPERTY),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext
                                                                         context) {
                        System.out.println(element);
                        CfgProperty property = (CfgProperty) element;
                        String key = property.getKey();
                        String value = property.getValue() instanceof String ?
                                (String) property.getValue() : null;
                        if (key != null && needJob(key) && value != null) {
                            return new PsiReference[]{
                                    new CfgReference(element, new TextRange(4, value.length() + 4))};
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                });

        registrar.registerReferenceProvider(PlatformPatterns.psiElement(CfgSegmentImpl.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext
                                                                         context) {
                        CfgSegmentImpl segment = (CfgSegmentImpl) element;
                        String name = segment.getName() instanceof String ?
                                (String) segment.getName() : null;
                        if (name != null) {
                            return new PsiReference[]{
                                    new CfgReference(element, new TextRange(1, name.length() + 1))};
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                });
    }

    private static boolean needJob(String key) {
        return key.equals("job") || key.equals("waitJob");
    }
}