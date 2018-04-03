package com.jackshenorion.cfgplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.jackshenorion.cfgplugin.CfgLanguage;
import org.jetbrains.annotations.*;

public class CfgTokenType extends IElementType {
    public CfgTokenType(@NotNull @NonNls String debugName) {
        super(debugName, CfgLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "CfgTokenType." + super.toString();
    }
}