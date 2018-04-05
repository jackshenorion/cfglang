// This is a generated file. Not intended for manual editing.
package com.jackshenorion.cfgplugin.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class CfgVisitor extends PsiElementVisitor {

  public void visitProperty(@NotNull CfgProperty o) {
    visitNamedElement(o);
  }

  public void visitSegment(@NotNull CfgSegment o) {
    visitNamedElement(o);
  }

  public void visitNamedElement(@NotNull CfgNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
