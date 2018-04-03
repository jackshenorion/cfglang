// This is a generated file. Not intended for manual editing.
package com.jackshenorion.cfgplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.jackshenorion.cfgplugin.psi.impl.*;

public interface CfgTypes {

  IElementType PROPERTY = new CfgElementType("PROPERTY");

  IElementType COMMENT = new CfgTokenType("COMMENT");
  IElementType CRLF = new CfgTokenType("CRLF");
  IElementType KEY = new CfgTokenType("KEY");
  IElementType SEPARATOR = new CfgTokenType("SEPARATOR");
  IElementType VALUE = new CfgTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == PROPERTY) {
        return new CfgPropertyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
