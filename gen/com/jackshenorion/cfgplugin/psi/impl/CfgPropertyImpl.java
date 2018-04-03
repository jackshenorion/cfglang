// This is a generated file. Not intended for manual editing.
package com.jackshenorion.cfgplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jackshenorion.cfgplugin.psi.CfgTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.jackshenorion.cfgplugin.psi.*;

public class CfgPropertyImpl extends ASTWrapperPsiElement implements CfgProperty {

  public CfgPropertyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CfgVisitor visitor) {
    visitor.visitProperty(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CfgVisitor) accept((CfgVisitor)visitor);
    else super.accept(visitor);
  }

}
