package com.jackshenorion.cfgplugin.psi;


import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jackshenorion.cfgplugin.CfgFileType;

public class CfgElementFactory {
    public static CfgProperty createProperty(Project project, String name) {
        final CfgFile file = createFile(project, "myKey" + "=" + name);
        return (CfgProperty) file.getFirstChild();
    }

    public static CfgSegment createSegment(Project project, String name) {
        final CfgFile file = createFile(project, "[" + name + "]");
        return (CfgSegment) file.getFirstChild();
    }

    public static CfgFile createFile(Project project, String text) {
        String name = "dummy.cfg";
        return (CfgFile) PsiFileFactory.getInstance(project).
                createFileFromText(name, CfgFileType.INSTANCE, text);
    }
}