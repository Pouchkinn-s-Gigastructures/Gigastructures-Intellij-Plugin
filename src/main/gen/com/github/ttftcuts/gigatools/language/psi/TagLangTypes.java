// This is a generated file. Not intended for manual editing.
package com.github.ttftcuts.gigatools.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.github.ttftcuts.gigatools.language.psi.impl.*;

public interface TagLangTypes {

  IElementType PROPERTY = new TagElementType("PROPERTY");

  IElementType COMMENT = new TagTokenType("COMMENT");
  IElementType CRLF = new TagTokenType("CRLF");
  IElementType KEY = new TagTokenType("KEY");
  IElementType SEPARATOR = new TagTokenType("SEPARATOR");
  IElementType VALUE = new TagTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == PROPERTY) {
        return new TagLangPropertyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
