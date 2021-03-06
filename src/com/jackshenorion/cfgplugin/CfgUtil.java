package com.jackshenorion.cfgplugin;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jackshenorion.cfgplugin.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CfgUtil {
    public static final String PROPERTY_KEY_JOB_CLASS = "jobClass";
    public static final String PROPERTY_KEY_JOB = "job";
    public static final String PROPERTY_KEY_EXTEND_JOB = "extendJob";
    public static final String PROPERTY_KEY_WAIT_JOB = "waitJob";

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
        return findSegments(project, segmentName, getVirtualFile(element));
    }

    public static List<CfgSegment> findSegments(Project project, PsiElement element) {
        return findSegments(project, getVirtualFile(element));
    }

    private static List<CfgSegment> findSegments(Project project, String segmentName, VirtualFile currentFile) {
        if (segmentName == null) {
            return Collections.emptyList();
        }
        final List<CfgSegment> result = new ArrayList<>();
        for (CfgFile cfgFile : findCfgFilesInSameScope(project, currentFile)) {
            CfgSegment[] segments = PsiTreeUtil.getChildrenOfType(cfgFile, CfgSegment.class);
            if (segments != null) {
                for (CfgSegment segment : segments) {
                    if (segmentName.equals(segment.getName())) {
                        result.add(segment);
                    }
                }
            }
        }
        return result;
    }

    public static List<CfgSegment> findSegments(Project project, VirtualFile currentFile) {
        List<CfgSegment> result = new ArrayList<CfgSegment>();
        for (CfgFile cfgFile : findCfgFilesInSameScope(project, currentFile)) {
            CfgSegment[] segments = PsiTreeUtil.getChildrenOfType(cfgFile, CfgSegment.class);
            if (segments != null) {
                Collections.addAll(result, segments);
            }
        }
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

    public static List<CfgFile> findProjectCfgFiles(Project project, VirtualFile virtualFile) {
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

    public static boolean isScopeChanged(VirtualFile oldFile, VirtualFile newFile) {
        return oldFile == null || (!isSamePackage(oldFile, newFile) && !isStandardCfgFile(newFile));
    }

    public static boolean isSamePackage(VirtualFile fileToMatch, VirtualFile fileToCheck) {
        return fileToMatch != null && fileToMatch.getParent().getCanonicalPath().equals(fileToCheck.getParent().getCanonicalPath());
    }

    public static boolean isStandardCfgFile(VirtualFile fileToCheck) {
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

    public static boolean isJobKey(String key) {
        return key.equals(PROPERTY_KEY_JOB) || key.equals(PROPERTY_KEY_WAIT_JOB) || key.equals(PROPERTY_KEY_EXTEND_JOB);
    }

    public static boolean isExtendJobKey(String key) {
        return key.equals(PROPERTY_KEY_EXTEND_JOB);
    }

    @Nullable
    public static VirtualFile getVirtualFile(PsiElement psiElement) {
        if (psiElement == null //|| !psiElement.isValid()
                || psiElement.getContainingFile() == null
                || psiElement.getContainingFile().getOriginalFile() == null) {
//            System.out.println("getVirtualFile: null: " + psiElement);
            return null;
        }
        return psiElement.getContainingFile().getOriginalFile().getVirtualFile();
    }

    @Nullable
    public static PsiFile getContainingFile(PsiElement psiElement) {
        if (psiElement == null) {// || !psiElement.isValid()) {
            return null;
        }
        return psiElement.getContainingFile().getOriginalFile();
    }

    public static boolean isElementInSelectedFile(Project project, PsiElement psiElement) {
        VirtualFile elementFile = getVirtualFile(psiElement);
        if (elementFile == null) {
            return false;
        }
        VirtualFile[] currentEditedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
        for (VirtualFile file : currentEditedFiles) {
            if (elementFile.equals(file)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Editor getEditorIfSelected(Project project, PsiElement psiElement) {
        VirtualFile elementFile = getVirtualFile(psiElement);
        if (elementFile == null) {
            return null;
        }
        FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(elementFile);
        Editor editor = null;
        if (fileEditor != null && fileEditor instanceof TextEditor) {
            editor = ((TextEditor) fileEditor).getEditor();
        }
        return editor;
    }
}