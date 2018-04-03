package com.jackshenorion.cfgplugin.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.jackshenorion.cfgplugin.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CfgFile extends PsiFileBase {
    public CfgFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, CfgLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return CfgFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Cfg File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }
}