package com.jackshenorion.cfgplugin.view;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBScrollPane;
import com.jackshenorion.cfgplugin.CfgLanguage;
import com.jackshenorion.cfgplugin.controller.CfgPluginController;
import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.model.CfgViewerTreeNode;
import com.jackshenorion.cfgplugin.model.CfgViewerTreeModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

public class CfgViewerPanel extends JPanel {

    private CfgPluginController cfgPluginController;
    private Project project;
    private CfgViewerTree tree;
    private CfgViewerTreeModel model;
    private final EditorCaretMover caretMover;
    private final ViewerTreeSelectionListener treeSelectionListener;
    private VirtualFile currentVirtualFile;

    public CfgViewerPanel(CfgPluginController cfgPluginController) {
        this.cfgPluginController = cfgPluginController;
        this.project = cfgPluginController.getProject();
        this.caretMover = new EditorCaretMover(project);
        this.treeSelectionListener = new ViewerTreeSelectionListener();
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        model = new CfgViewerTreeModel(cfgPluginController);
        tree = new CfgViewerTree(model);
        tree.getSelectionModel().addTreeSelectionListener(treeSelectionListener);
        ActionMap actionMap = tree.getActionMap();
        actionMap.put("EditSource", new AbstractAction("EditSource") {
            public void actionPerformed(ActionEvent e) {
                onEditSource();
            }
        });
        InputMap inputMap = tree.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, true), "EditSource");

        add(new JBScrollPane(tree), BorderLayout.CENTER);
        setBackground(Color.WHITE);
    }

    public void onEditSource() {
        if (getSelectedNode() == null || getSelectedNode().getCfgSegment() == null) return;
        Editor editor = caretMover.openInEditor(getSelectedNode().getCfgSegment());
        if (editor == null) {
            return;
        }
        selectElementAtCaret(editor, TREE_SELECTION_CHANGED);
        editor.getContentComponent().requestFocus();
    }

    public void setCurrentFile(VirtualFile virtualFile) {
        if (!CfgUtil.isCfgVirtualFile(virtualFile)) {
            return;
        }
        currentVirtualFile = virtualFile;
        resetTree();
    }

    public VirtualFile getCurrentVirtualFile() {
        return currentVirtualFile;
    }

    private void resetTree() {
        tree.getSelectionModel().removeTreeSelectionListener(treeSelectionListener);

        Enumeration expandedDescendants = null;
        TreePath path = null;
        if (model.getRoot() != null) {
            expandedDescendants = tree.getExpandedDescendants(new TreePath(model.getRoot()));
            path = tree.getSelectionModel().getSelectionPath();
        }

        model = new CfgViewerTreeModel(this.cfgPluginController);
        model.setCfgFiles(CfgUtil.findProjectCfgFiles(project, currentVirtualFile), CfgUtil.findStandardCfgFiles(project));
        tree.setModel(model);
        if (expandedDescendants != null)
            while (expandedDescendants.hasMoreElements()) {
                TreePath treePath = (TreePath) expandedDescendants.nextElement();
                tree.expandPath(treePath);
            }
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);

        tree.getSelectionModel().addTreeSelectionListener(treeSelectionListener);
    }

    private static final String CARET_MOVED = "caret moved";
    private static final String TREE_SELECTION_CHANGED = "tree selection changed";
    private boolean inSetSelectedElement = false;
    private CfgViewerTreeNode selectedJobNode;

    private CfgViewerTreeNode getSelectedNode() {
        return selectedJobNode;
    }

    @Nullable
    private PsiElement getSelectedElement() {
        return (selectedJobNode != null && selectedJobNode.getCfgSegment() != null)
                ? selectedJobNode.getCfgSegment() : null;
    }

    private class ViewerTreeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            setSelectedNode((CfgViewerTreeNode) tree.getLastSelectedPathComponent(),
                    CfgViewerPanel.TREE_SELECTION_CHANGED);
        }
    }

    private void setSelectedNode(CfgViewerTreeNode jobNode, String reason) {
        if (inSetSelectedElement) {
            return;
        }
        try {
            inSetSelectedElement = true;
            selectedJobNode = jobNode;
            if (reason != CARET_MOVED && jobNode != null)
                moveEditorCaret();
        } finally {
            inSetSelectedElement = false;
        }
    }

    private void setSelectedElement(PsiElement element, String reason) {
        if (inSetSelectedElement) {
            return;
        }
        try {
            inSetSelectedElement = true;
            changeTreeSelection(element);
        } finally {
            inSetSelectedElement = false;
        }
    }

    private void changeTreeSelection(PsiElement element) {
        TreePath path = model.getPath(element);
        tree.expandPath(path);
        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);
    }

    private void moveEditorCaret() {
        if (cfgPluginController.isAutoScrollToSource()) {
            caretMover.moveEditorCaret(getSelectedNode());
        }
    }

    public void refreshRootElement() {
        selectElementAtCaret();
    }

    public void selectElementAtCaret() {
        selectElementAtCaret(FileEditorManager.getInstance(project).getSelectedTextEditor(), null);
    }

    public void selectElementAtCaret(@Nullable Editor editor, @Nullable String changeSource) {
        if (editor == null) {
            return;
        }
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        PsiElement elementAtCaret = null;
        if (psiFile != null) {
            FileViewProvider viewProvider = psiFile.getViewProvider();
            PsiFile selectedRoot = viewProvider.getPsi(CfgLanguage.INSTANCE);
            if (selectedRoot == null) {
                return;
            }
            elementAtCaret = viewProvider.findElementAt(editor.getCaretModel().getOffset(), CfgLanguage.INSTANCE);
            if (elementAtCaret != null && elementAtCaret.getParent() != null) {
                if (elementAtCaret.getParent().getChildren().length == 0)
                    elementAtCaret = elementAtCaret.getParent();
            }
        }

        if (elementAtCaret != null && elementAtCaret != getSelectedElement()) {
            setCurrentFile(psiFile.getOriginalFile().getVirtualFile());
            setSelectedElement(elementAtCaret, changeSource == null ? CfgViewerPanel.CARET_MOVED : changeSource);
        }
    }
}
