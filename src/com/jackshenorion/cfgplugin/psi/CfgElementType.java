package com.jackshenorion.cfgplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.jackshenorion.cfgplugin.CfgLanguage;
import org.jetbrains.annotations.*;

public class CfgElementType extends IElementType {
    public CfgElementType(@NotNull @NonNls String debugName) {
        super(debugName, CfgLanguage.INSTANCE);
    }
}