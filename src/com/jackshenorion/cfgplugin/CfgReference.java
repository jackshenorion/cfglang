package com.jackshenorion.cfgplugin;

import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.jackshenorion.cfgplugin.psi.CfgProperty;
import com.jackshenorion.cfgplugin.psi.CfgSegment;
import com.jackshenorion.cfgplugin.psi.impl.CfgPsiImplUtil;
import org.jetbrains.annotations.*;

import java.util.*;

public class CfgReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private String key;

    public CfgReference(@NotNull PsiElement element, TextRange textRange) {
        super(element, textRange);
        key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        final List<CfgProperty> properties = CfgUtil.findProperties(project, key);
        final List<CfgSegment> segments = CfgUtil.findSegments(project, key);
        List<ResolveResult> results = new ArrayList<ResolveResult>();
        for (CfgProperty property : properties) {
            results.add(new PsiElementResolveResult(property));
        }
        for (CfgSegment segment : segments) {
            results.add(new PsiElementResolveResult(segment));
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Project project = myElement.getProject();
        List<CfgProperty> properties = CfgUtil.findProperties(project);
        List<CfgSegment> segments = CfgUtil.findSegments(project);
        List<LookupElement> variants = new ArrayList<LookupElement>();
        for (final CfgProperty property : properties) {
            if (property.getKey() != null && property.getKey().length() > 0) {
                variants.add(LookupElementBuilder.create(property).
                        withIcon(CfgIcons.FILE).
                        withTypeText(property.getContainingFile().getName())
                );
            }
        }
        for (final CfgSegment segment : segments) {
            if (segment.getName() != null && segment.getName().length() > 0) {
                variants.add(LookupElementBuilder.create(segment).
                        withIcon(CfgIcons.FILE).
                        withTypeText(segment.getContainingFile().getName())
                );
            }
        }
        return variants.toArray();
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        CfgPsiImplUtil.rename(myElement, newElementName);
        return myElement;
    }
}

