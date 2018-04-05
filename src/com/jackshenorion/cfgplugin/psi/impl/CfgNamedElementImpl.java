package com.jackshenorion.cfgplugin.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.jackshenorion.cfgplugin.psi.CfgNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class CfgNamedElementImpl extends ASTWrapperPsiElement implements CfgNamedElement {
    public CfgNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}