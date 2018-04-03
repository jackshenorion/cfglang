package com.jackshenorion.cfgplugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class CfgFileType extends LanguageFileType {
    public static final CfgFileType INSTANCE = new CfgFileType();

    private CfgFileType() {
        super(CfgLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Cfg file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Cfg language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "cfg";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return CfgIcons.FILE;
    }
}