// This is a generated file. Not intended for manual editing.
package com.github.ttftcuts.gigatools.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.github.ttftcuts.gigatools.language.psi.impl.*;

public interface TagLangTypes {

  IElementType AND_EXPRESSION = new TagElementType("AND_EXPRESSION");
  IElementType EXPRESSION = new TagElementType("EXPRESSION");
  IElementType OR_EXPRESSION = new TagElementType("OR_EXPRESSION");
  IElementType PRIMARY_EXPRESSION = new TagElementType("PRIMARY_EXPRESSION");
  IElementType TAG_EXPRESSION = new TagElementType("TAG_EXPRESSION");
  IElementType UNARY_EXPRESSION = new TagElementType("UNARY_EXPRESSION");

  IElementType AND_OP = new TagTokenType("AND_OP");
  IElementType L_PAREN = new TagTokenType("L_PAREN");
  IElementType NOT_OP = new TagTokenType("NOT_OP");
  IElementType OR_OP = new TagTokenType("OR_OP");
  IElementType R_PAREN = new TagTokenType("R_PAREN");
  IElementType TAG = new TagTokenType("TAG");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == AND_EXPRESSION) {
        return new TagLangAndExpressionImpl(node);
      }
      else if (type == EXPRESSION) {
        return new TagLangExpressionImpl(node);
      }
      else if (type == OR_EXPRESSION) {
        return new TagLangOrExpressionImpl(node);
      }
      else if (type == PRIMARY_EXPRESSION) {
        return new TagLangPrimaryExpressionImpl(node);
      }
      else if (type == TAG_EXPRESSION) {
        return new TagLangTagExpressionImpl(node);
      }
      else if (type == UNARY_EXPRESSION) {
        return new TagLangUnaryExpressionImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
