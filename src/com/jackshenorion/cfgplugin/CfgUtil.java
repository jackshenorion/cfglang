package com.jackshenorion.cfgplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jackshenorion.cfgplugin.psi.*;

import java.util.*;

public class CfgUtil {
    public static List<CfgProperty> findProperties(Project project, String key) {
        List<CfgProperty> result = null;
        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, CfgFileType.INSTANCE,
                        GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            CfgFile simpleFile = (CfgFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (simpleFile != null) {
                CfgProperty[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, CfgProperty.class);
                if (properties != null) {
                    for (CfgProperty property : properties) {
                        if (key.equals(property.getKey())) {
                            if (result == null) {
                                result = new ArrayList<CfgProperty>();
                            }
                            result.add(property);
                        }
                    }
                }
            }
        }
        return result != null ? result : Collections.<CfgProperty>emptyList();
    }

    public static List<CfgProperty> findProperties(Project project) {
        List<CfgProperty> result = new ArrayList<CfgProperty>();
        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, CfgFileType.INSTANCE,
                        GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            CfgFile simpleFile = (CfgFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (simpleFile != null) {
                CfgProperty[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, CfgProperty.class);
                if (properties != null) {
                    Collections.addAll(result, properties);
                }
            }
        }
        return result;
    }
}