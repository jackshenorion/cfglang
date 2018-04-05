// This is a generated file. Not intended for manual editing.
package com.jackshenorion.cfgplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface CfgProperty extends CfgNamedElement {

  String getKey();

  String getValue();

  String getName();

  PsiElement setName(String newName);

  PsiElement getNameIdentifier();

  PsiReference[] getReferences();

}
