package com.jackshenorion.cfgplugin;


import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.jackshenorion.cfgplugin.psi.CfgProperty;
import com.jackshenorion.cfgplugin.psi.CfgSegment;

public class CfgRefactoringSupportProvider extends RefactoringSupportProvider {
    @Override
    public boolean isMemberInplaceRenameAvailable(PsiElement element, PsiElement context) {
        boolean canRename = element instanceof CfgProperty || element instanceof CfgSegment;
        System.out.println(canRename + " : CfgRefactoringSupportProvider:" + element + ", " + element.getText());
        return canRename;
    }
}