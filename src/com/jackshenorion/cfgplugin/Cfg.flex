package com.jackshenorion.cfgplugin;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.jackshenorion.cfgplugin.psi.CfgTypes;
import com.intellij.psi.TokenType;

%%

%class CfgLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

CRLF=\R
WHITE_SPACE=[\ \n\t\f]
FIRST_VALUE_CHARACTER=[^ \n\f\\] | "\\"{CRLF} | "\\".
VALUE_CHARACTER=[^\n\f\\] | "\\"{CRLF} | "\\".
END_OF_LINE_COMMENT=("#"|"!")[^\r\n]*
SEPARATOR=[:=]
KEY_CHARACTER=[^:=\ \[\]\n\t\f\\] | "\\ "
SEGMENT_NAME=[^:=\ \n\[\]\t\f\\]+
SEGMENT_BEGIN="["
SEGMENT_END="]"

%state WAITING_VALUE
%state WAITING_SEGMENT_NAME

%%

//<YYINITIAL> {SEGMENT_BEGIN}{SEGMENT_NAME}{SEGMENT_END}      { yybegin(YYINITIAL); return CfgTypes.SEGMENT_NAME;}
<YYINITIAL> {SEGMENT_BEGIN}                                 { yybegin(WAITING_SEGMENT_NAME); return CfgTypes.SEGMENT_BEGIN;}

<WAITING_SEGMENT_NAME> {SEGMENT_NAME}                       { return CfgTypes.SEGMENT_NAME; }

<WAITING_SEGMENT_NAME> {SEGMENT_END}                        { yybegin(YYINITIAL); return CfgTypes.SEGMENT_END;}

<YYINITIAL> {END_OF_LINE_COMMENT}                           { yybegin(YYINITIAL); return CfgTypes.COMMENT; }

<YYINITIAL> {KEY_CHARACTER}+                                { yybegin(YYINITIAL); return CfgTypes.KEY; }

<YYINITIAL> {SEPARATOR}                                     { yybegin(WAITING_VALUE); return CfgTypes.SEPARATOR; }

<WAITING_VALUE> {CRLF}({CRLF}|{WHITE_SPACE})+               { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

<WAITING_VALUE> {WHITE_SPACE}+                              { yybegin(WAITING_VALUE); return TokenType.WHITE_SPACE; }

<WAITING_VALUE> {FIRST_VALUE_CHARACTER}{VALUE_CHARACTER}*   { yybegin(YYINITIAL); return CfgTypes.VALUE; }

({CRLF}|{WHITE_SPACE})+                                     { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

.                                                           { return TokenType.BAD_CHARACTER; }
