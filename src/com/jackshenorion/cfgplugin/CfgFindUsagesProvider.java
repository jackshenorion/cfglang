package com.jackshenorion.cfgplugin;

import com.intellij.lang.cacheBuilder.*;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.*;
import com.intellij.psi.tree.TokenSet;
import com.jackshenorion.cfgplugin.psi.*;
import org.jetbrains.annotations.*;

public class CfgFindUsagesProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new CfgLexerAdapter(),
                TokenSet.create(CfgTypes.VALUE),
                TokenSet.create(CfgTypes.SEGMENT_NAME),
                TokenSet.create(CfgTypes.COMMENT),
                TokenSet.EMPTY);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof CfgProperty) {
            return "cfg property";
        } else if (element instanceof CfgSegment) {
            return "cfg segment";
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof CfgProperty) {
            return ((CfgProperty) element).getValue();
        } else if (element instanceof CfgSegment) {
            return ((CfgSegment) element).getName();
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof CfgProperty) {
            return ((CfgProperty) element).getKey() + ":" + ((CfgProperty) element).getValue();
        } else if (element instanceof CfgSegment) {
            return ((CfgSegment) element).getName();
        } else {
            return "";
        }
    }
}