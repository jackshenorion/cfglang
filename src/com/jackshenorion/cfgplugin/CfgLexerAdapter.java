package com.jackshenorion.cfgplugin;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;

public class CfgLexerAdapter extends FlexAdapter {
    public CfgLexerAdapter() {
        super(new CfgLexer((Reader) null));
    }
}