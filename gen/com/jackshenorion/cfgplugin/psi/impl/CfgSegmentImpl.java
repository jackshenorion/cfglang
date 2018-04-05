// This is a generated file. Not intended for manual editing.
package com.jackshenorion.cfgplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jackshenorion.cfgplugin.psi.CfgTypes.*;
import com.jackshenorion.cfgplugin.psi.*;
import com.intellij.psi.PsiReference;

public class CfgSegmentImpl extends CfgNamedElementImpl implements CfgSegment {

  public CfgSegmentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CfgVisitor visitor) {
    visitor.visitSegment(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CfgVisitor) accept((CfgVisitor)visitor);
    else super.accept(visitor);
  }

  public String getName() {
    return CfgPsiImplUtil.getName(this);
  }

  public PsiElement setName(String newName) {
    return CfgPsiImplUtil.setName(this, newName);
  }

  public PsiElement getNameIdentifier() {
    return CfgPsiImplUtil.getNameIdentifier(this);
  }

  public PsiReference[] getReferences() {
    return CfgPsiImplUtil.getReferences(this);
  }

}
