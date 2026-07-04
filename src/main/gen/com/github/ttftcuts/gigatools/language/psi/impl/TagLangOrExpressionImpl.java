// This is a generated file. Not intended for manual editing.
package com.github.ttftcuts.gigatools.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.ttftcuts.gigatools.language.psi.TagLangTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.github.ttftcuts.gigatools.language.psi.*;

public class TagLangOrExpressionImpl extends ASTWrapperPsiElement implements TagLangOrExpression {

  public TagLangOrExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull TagLangVisitor visitor) {
    visitor.visitOrExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TagLangVisitor) accept((TagLangVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<TagLangAndExpression> getAndExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TagLangAndExpression.class);
  }

}
