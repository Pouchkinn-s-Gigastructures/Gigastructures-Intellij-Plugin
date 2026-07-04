package com.github.ttftcuts.gigatools.language;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.github.ttftcuts.gigatools.language.psi.TagLangTypes;

%%

%class TagLangLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{ return;
%eof}

WHITE_SPACE=[\ \n\t\f]
TAG=\w+

%%

<YYINITIAL> {
    {WHITE_SPACE}+   { return TokenType.WHITE_SPACE; }

    {TAG}            { return TagLangTypes.TAG; }

    "&"              { return TagLangTypes.AND_OP; }
    "|"              { return TagLangTypes.OR_OP; }
    "!"              { return TagLangTypes.NOT_OP; }

    "("              { return TagLangTypes.L_PAREN; }
    ")"              { return TagLangTypes.R_PAREN; }

    [^]              { return TokenType.BAD_CHARACTER; }
}


