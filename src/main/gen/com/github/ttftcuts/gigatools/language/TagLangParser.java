// This is a generated file. Not intended for manual editing.
package com.github.ttftcuts.gigatools.language;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.github.ttftcuts.gigatools.language.psi.TagLangTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class TagLangParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return tagFile(b, l + 1);
  }

  /* ********************************************************** */
  // unary_expression (AND_OP unary_expression)*
  public static boolean and_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "and_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, AND_EXPRESSION, "<and expression>");
    r = unary_expression(b, l + 1);
    r = r && and_expression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (AND_OP unary_expression)*
  private static boolean and_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "and_expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!and_expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "and_expression_1", c)) break;
    }
    return true;
  }

  // AND_OP unary_expression
  private static boolean and_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "and_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND_OP);
    r = r && unary_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // or_expression
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION, "<expression>");
    r = or_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // and_expression (OR_OP and_expression)*
  public static boolean or_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OR_EXPRESSION, "<or expression>");
    r = and_expression(b, l + 1);
    r = r && or_expression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (OR_OP and_expression)*
  private static boolean or_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!or_expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "or_expression_1", c)) break;
    }
    return true;
  }

  // OR_OP and_expression
  private static boolean or_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OR_OP);
    r = r && and_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // tag_expression | L_PAREN expression R_PAREN
  public static boolean primary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary_expression")) return false;
    if (!nextTokenIs(b, "<primary expression>", L_PAREN, TAG)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PRIMARY_EXPRESSION, "<primary expression>");
    r = tag_expression(b, l + 1);
    if (!r) r = primary_expression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // L_PAREN expression R_PAREN
  private static boolean primary_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, L_PAREN);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, R_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expression
  static boolean tagFile(PsiBuilder b, int l) {
    return expression(b, l + 1);
  }

  /* ********************************************************** */
  // TAG
  public static boolean tag_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tag_expression")) return false;
    if (!nextTokenIs(b, TAG)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TAG);
    exit_section_(b, m, TAG_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // NOT_OP unary_expression | primary_expression
  public static boolean unary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNARY_EXPRESSION, "<unary expression>");
    r = unary_expression_0(b, l + 1);
    if (!r) r = primary_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NOT_OP unary_expression
  private static boolean unary_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOT_OP);
    r = r && unary_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

}
