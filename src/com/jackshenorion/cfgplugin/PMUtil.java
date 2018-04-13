package com.jackshenorion.cfgplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;

import java.util.ArrayList;
import java.util.List;

public class PMUtil {
    public static List<VirtualFile> findAllPmVirtualFiles(Project project) {
        return new ArrayList<>(FilenameIndex.getAllFilesByExt(project,"pm"));
    }
}
