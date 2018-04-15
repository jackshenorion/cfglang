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
import com.jackshenorion.cfgplugin.model.ViewerTreeNode;
import com.jackshenorion.cfgplugin.model.ViewerTreeModel;
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
    private ViewerTreeModel model;
    private final EditorCaretMover caretMover;
    private final ViewerTreeSelectionListener treeSelectionListener;
    private VirtualFile projectRepresentativeFile;

    public CfgViewerPanel(CfgPluginController cfgPluginController) {
        this.cfgPluginController = cfgPluginController;
        this.project = cfgPluginController.getProject();
        this.caretMover = new EditorCaretMover(project);
        this.treeSelectionListener = new ViewerTreeSelectionListener();
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        model = new ViewerTreeModel(cfgPluginController);
        tree = new CfgViewerTree(model);
        tree.getSelectionModel().addTreeSelectionListener(treeSelectionListener);
        ActionMap actionMap = tree.getActionMap();
        actionMap.put("EditSource", new AbstractAction("EditSource") {
            public void actionPerformed(ActionEvent e) {
                onFocusInEditSourceFromTreeSelection();
            }
        });
        InputMap inputMap = tree.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, true), "EditSource");

        add(new JBScrollPane(tree), BorderLayout.CENTER);
        setBackground(Color.WHITE);
    }

    public void onFocusInEditSourceFromTreeSelection() {
        if (getSelectedNode() == null || getSelectedNode().getCfgSegment() == null) {
            return;
        }
        Editor editor = caretMover.openInEditor(getSelectedNode().getCfgSegment());
        if (editor == null) {
            return;
        }
        selectElementAtCaret(editor, TREE_SELECTION_CHANGED);
        editor.getContentComponent().requestFocus();
    }

    private static final String CARET_MOVED = "caret moved";
    private static final String TREE_SELECTION_CHANGED = "tree selection changed";
    private boolean inSetSelectedElement = false;
    private ViewerTreeNode selectedJobNode;

    private ViewerTreeNode getSelectedNode() {
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
            setSelectedNode((ViewerTreeNode) tree.getLastSelectedPathComponent(), CfgViewerPanel.TREE_SELECTION_CHANGED);
        }
    }

    private void setSelectedNode(ViewerTreeNode jobNode, String reason) {
        if (inSetSelectedElement) {
            return;
        }
        try {
            inSetSelectedElement = true;
            selectedJobNode = jobNode;
            if (reason != CARET_MOVED && jobNode != null) {
                moveEditorCaret();
            }
        } finally {
            inSetSelectedElement = false;
        }
    }

    private void reflectSelectedElementOnTree(PsiElement element, String reason) {
        if (inSetSelectedElement) {
            return;
        }
        try {
            inSetSelectedElement = true;
            if (!reason.equals(TREE_SELECTION_CHANGED)) {
                changeTreeSelection(element);
            }
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
        projectRepresentativeFile = null;
        selectedJobNode = null;
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
            setProjectRepresentativeFile(psiFile.getOriginalFile().getVirtualFile());
            setSelectedJobNode(elementAtCaret);
            reflectSelectedElementOnTree(elementAtCaret, changeSource == null ? CfgViewerPanel.CARET_MOVED : changeSource);
        }
    }

    private void setSelectedJobNode(PsiElement element) {
        ViewerTreeNode node = model.getNode(element);
        if (node == null) {
            return;
        }
        setSelectedJobNode(node);
    }

    private void setSelectedJobNode(ViewerTreeNode node) {
        selectedJobNode = node;
    }

    public void setProjectRepresentativeFile(VirtualFile virtualFile) {
        if (!CfgUtil.isCfgVirtualFile(virtualFile)) {
            return;
        }
        if (CfgUtil.isScopeChanged(projectRepresentativeFile, virtualFile)) {
            projectRepresentativeFile = virtualFile;
            resetTree();
        }
    }


    public VirtualFile getProjectRepresentativeFile() {
        return projectRepresentativeFile;
    }

    private void resetTree() {
        tree.getSelectionModel().removeTreeSelectionListener(treeSelectionListener);

        Enumeration expandedDescendants = null;
        TreePath path = null;
        if (model.getRoot() != null) {
            expandedDescendants = tree.getExpandedDescendants(new TreePath(model.getRoot()));
            path = tree.getSelectionModel().getSelectionPath();
        }

        model = new ViewerTreeModel(this.cfgPluginController);
        model.setCfgFiles(CfgUtil.findProjectCfgFiles(project, projectRepresentativeFile), CfgUtil.findStandardCfgFiles(project));
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
}
