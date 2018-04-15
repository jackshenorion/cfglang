package com.jackshenorion.cfgplugin.controller;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public class PackageStateHelper {
    private List<VirtualFile> projectVirtualFiles;
    private List<VirtualFile> baseVirtualFiles;

    public void onVirtualFileSelected(VirtualFile virtualFile) {
        if (projectVirtualFiles.contains(virtualFile)) {
            return;
        }


    }


}
