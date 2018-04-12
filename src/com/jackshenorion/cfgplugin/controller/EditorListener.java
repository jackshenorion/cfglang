package com.jackshenorion.cfgplugin.controller;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.view.CfgViewerPanel;
import org.jetbrains.annotations.NotNull;

public class EditorListener implements CaretListener, FileEditorManagerListener {
    private final CfgViewerPanel _viewer;
    private final Project _project;
    private final PsiTreeChangeAdapter _treeChangeListener;
    private Editor _currentEditor;
    private MessageBusConnection _msgbus;

    public EditorListener(CfgViewerPanel viewer, Project project) {
        _viewer = viewer;
        _project = project;
        _treeChangeListener = new PsiTreeChangeAdapter() {
            public void childrenChanged(@NotNull final PsiTreeChangeEvent event) {
                updateTreeFromPsiTreeChange(event);
            }

            public void childAdded(@NotNull PsiTreeChangeEvent event) {
                updateTreeFromPsiTreeChange(event);
            }

            public void childMoved(@NotNull PsiTreeChangeEvent event) {
                updateTreeFromPsiTreeChange(event);
            }

            public void childRemoved(@NotNull PsiTreeChangeEvent event) {
                updateTreeFromPsiTreeChange(event);
            }

            public void childReplaced(@NotNull PsiTreeChangeEvent event) {
                updateTreeFromPsiTreeChange(event);
            }

            public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
                updateTreeFromPsiTreeChange(event);
            }
        };
    }

    private void updateTreeFromPsiTreeChange(final PsiTreeChangeEvent event) {
        if (onCfgViewerTree(event)) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    _viewer.refreshRootElement();
                }
            });
        }
    }

    private boolean onCfgViewerTree(final PsiTreeChangeEvent event) {
        if (_viewer.getCurrentVirtualFile() == null) {
            return false;
        }
        PsiElement elementChangedByPsi = event.getParent();
        if (elementChangedByPsi == null) {
            return false;
        }
        VirtualFile changedVirtualFile = elementChangedByPsi.getContainingFile().getOriginalFile().getVirtualFile();
        if (changedVirtualFile == null || !CfgUtil.isCfgVirtualFile(changedVirtualFile)) {
            return false;
        }
        return CfgUtil.isSamePackage(changedVirtualFile, _viewer.getCurrentVirtualFile()) || CfgUtil.isStandardCfgFile(changedVirtualFile);
    }

    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        System.out.println("fileOpened: " + file);
    }

    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        System.out.println("fileClosed: " + file);
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        System.out.println("selectionChanged: " + event);
        if (event.getNewFile() == null) {
            return;
        }
        Editor newEditor = event.getManager().getSelectedTextEditor();
        if (_currentEditor != newEditor) {
            _currentEditor.getCaretModel().removeCaretListener(this);
        }
        _viewer.selectElementAtCaret();
        if (newEditor != null) {
            _currentEditor = newEditor;
        }
        _currentEditor.getCaretModel().addCaretListener(this);
    }


    @Override
    public void caretPositionChanged(CaretEvent event) {
        System.out.println("caretPositionChanged: " + event);
        _viewer.selectElementAtCaret();
    }

    public void start() {
        _msgbus = _project.getMessageBus().connect();
        _msgbus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
        PsiManager.getInstance(_project).addPsiTreeChangeListener(_treeChangeListener);
        _currentEditor = FileEditorManager.getInstance(_project).getSelectedTextEditor();
        if (_currentEditor != null)
            _currentEditor.getCaretModel().addCaretListener(this);
    }

    public void stop() {
        if (_msgbus != null) {
            _msgbus.disconnect();
            _msgbus = null;
        }

        PsiManager.getInstance(_project).removePsiTreeChangeListener(_treeChangeListener);
    }
}
