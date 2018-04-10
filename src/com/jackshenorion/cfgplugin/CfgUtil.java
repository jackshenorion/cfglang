package com.jackshenorion.cfgplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jackshenorion.cfgplugin.psi.*;

import java.util.*;
import java.util.stream.Collectors;

public class CfgUtil {
    public static final String PROPERTY_KEY_JOBCLASS = "jobClass";

    public static List<CfgSegment> findSegments(Project project, String segmentName) {
        if (segmentName == null) {
            return Collections.emptyList();
        }
        final List<CfgSegment> result = new ArrayList<>();
        findAllCfgFiles(project).forEach(cfgFile -> {
            CfgSegment[] segments = PsiTreeUtil.getChildrenOfType(cfgFile, CfgSegment.class);
            if (segments != null) {
                for (CfgSegment segment : segments) {
                    if (segmentName.equals(segment.getName())) {
                        result.add(segment);
                    }
                }
            }
        });
        return result;
    }

    public static List<CfgSegment> findSegments(Project project) {
        List<CfgSegment> result = new ArrayList<CfgSegment>();
        findAllCfgFiles(project).forEach(cfgFile -> {
            CfgSegment[] segments = PsiTreeUtil.getChildrenOfType(cfgFile, CfgSegment.class);
            if (segments != null) {
                Collections.addAll(result, segments);
            }
        });
        return result;
    }

    public static List<CfgSegment> findSegments(Project project, String segmentName, PsiElement element) {
        return findSegments(project, segmentName, element.getContainingFile().getOriginalFile().getVirtualFile());
    }

    public static List<CfgSegment> findSegments(Project project, PsiElement element) {
        return findSegments(project, element.getContainingFile().getOriginalFile().getVirtualFile());
    }

    private static List<CfgSegment> findSegments(Project project, String segmentName, VirtualFile currentFile) {
        if (segmentName == null) {
            return Collections.emptyList();
        }
        final List<CfgSegment> result = new ArrayList<>();
        findCfgFilesInSameScope(project, currentFile).forEach(cfgFile -> {
            CfgSegment[] segments = PsiTreeUtil.getChildrenOfType(cfgFile, CfgSegment.class);
            if (segments != null) {
                for (CfgSegment segment : segments) {
                    if (segmentName.equals(segment.getName())) {
                        result.add(segment);
                    }
                }
            }
        });
        return result;
    }

    public static List<CfgSegment> findSegments(Project project, VirtualFile currentFile) {
        List<CfgSegment> result = new ArrayList<CfgSegment>();
        findCfgFilesInSameScope(project, currentFile).forEach(cfgFile -> {
            CfgSegment[] segments = PsiTreeUtil.getChildrenOfType(cfgFile, CfgSegment.class);
            if (segments != null) {
                Collections.addAll(result, segments);
            }
        });
        return result;
    }

    public static List<CfgFile> findCfgFilesInSameScope(Project project, VirtualFile virtualFile) {
        if (!isCfgVirtualFile(virtualFile)) {
            return Collections.emptyList();
        }
        return findAllCfgVirtualFiles(project).stream()
                .filter(fileToCheck -> isSamePackage(virtualFile, fileToCheck) || isStandardCfgFile(fileToCheck))
                .map(vfile -> PsiManager.getInstance(project).findFile(vfile))
                .filter(file -> file instanceof CfgFile)
                .map(file -> (CfgFile) file)
                .filter(cfgFile -> cfgFile != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<CfgFile> findNormalCfgFiles(Project project, VirtualFile virtualFile) {
        if (!isCfgVirtualFile(virtualFile)) {
            return Collections.emptyList();
        }
        return findAllCfgVirtualFiles(project).stream()
                .filter(fileToCheck -> isSamePackage(virtualFile, fileToCheck))
                .map(vfile -> PsiManager.getInstance(project).findFile(vfile))
                .filter(file -> file instanceof CfgFile)
                .map(file -> (CfgFile) file)
                .filter(cfgFile -> cfgFile != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<CfgFile> findStandardCfgFiles(Project project) {
        return findAllCfgVirtualFiles(project).stream()
                .filter(fileToCheck -> isStandardCfgFile(fileToCheck))
                .map(vfile -> PsiManager.getInstance(project).findFile(vfile))
                .filter(file -> file instanceof CfgFile)
                .map(file -> (CfgFile) file)
                .filter(cfgFile -> cfgFile != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<CfgFile> findAllCfgFiles(Project project) {
        return findAllCfgVirtualFiles(project).stream()
                .map(vfile -> PsiManager.getInstance(project).findFile(vfile))
                .filter(file -> file instanceof CfgFile)
                .map(file -> (CfgFile) file)
                .filter(cfgFile -> cfgFile != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<VirtualFile> findAllCfgVirtualFiles(Project project) {
        return FileTypeIndex.getFiles(CfgFileType.INSTANCE, GlobalSearchScope.allScope(project)).stream()
                .filter(CfgUtil::isCfgVirtualFile)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static boolean isSamePackage(VirtualFile fileToMatch, VirtualFile fileToCheck) {
        return fileToMatch.getParent().getCanonicalPath().equals(fileToCheck.getParent().getCanonicalPath());
    }

    private static boolean isStandardCfgFile(VirtualFile fileToCheck) {
        return isStandardCfgFile(fileToCheck.getName());
    }

    public static boolean isCfgVirtualFile(VirtualFile virtualFile) {
        return virtualFile != null
                && !virtualFile.isDirectory()
                && (virtualFile.getName().startsWith(CfgFileType.PREFIX_SMARTSCONTROL) || virtualFile.getName().startsWith(CfgFileType.PREFIX_STANDARDCONTROL))
                && virtualFile.getName().endsWith(".cfg");
    }

    private static boolean isStandardCfgFile(String fileName) {
        return fileName.startsWith(CfgFileType.PREFIX_STANDARDCONTROL);
    }

    public static boolean needJob(String key) {
        return key.equals("job") || key.equals("waitJob");
    }
}