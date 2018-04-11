package com.jackshenorion.cfgplugin.view;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBScrollPane;
import com.jackshenorion.cfgplugin.CfgPluginController;
import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.model.CfgJobInfo;
import com.jackshenorion.cfgplugin.model.CfgViewerTreeModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CfgViewerPanel extends JPanel {

    private CfgPluginController cfgPluginController;
    private Project project;
    private CfgViewerTree tree;
    private CfgViewerTreeModel model;
    private final EditorCaretMover caretMover;
    private final ViewerTreeSelectionListener treeSelectionListener;

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
                if (getSelectedNode() == null || getSelectedNode().getCfgSegment() == null) return;
                Editor editor = caretMover.openInEditor(getSelectedNode().getCfgSegment());
                selectElementAtCaret(editor, TREE_SELECTION_CHANGED);
                editor.getContentComponent().requestFocus();
            }
        });
        InputMap inputMap = tree.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, true), "EditSource");

        add(new JBScrollPane(tree), BorderLayout.CENTER);
        setBackground(Color.WHITE);
    }

    public void resetTree(VirtualFile virtualFile) {
        tree.getSelectionModel().removeTreeSelectionListener(treeSelectionListener);
        this.model = new CfgViewerTreeModel(this.cfgPluginController);
        this.model.setCfgFiles(CfgUtil.findNormalCfgFiles(project, virtualFile), CfgUtil.findStandardCfgFiles(project));
        this.tree.setModel(this.model);
        tree.getSelectionModel().addTreeSelectionListener(treeSelectionListener);
    }

    private static final String CARET_MOVED = "caret moved";
    private static final String TREE_SELECTION_CHANGED = "tree selection changed";
    private boolean inSetSelectedElement = false;
    private CfgJobInfo selectedJobNode;

    private CfgJobInfo getSelectedNode() {
        return selectedJobNode;
    }

    private class ViewerTreeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            setSelectedNode((CfgJobInfo) tree.getLastSelectedPathComponent(),
                    CfgViewerPanel.TREE_SELECTION_CHANGED);
        }
    }

    private void setSelectedNode(CfgJobInfo jobNode, String reason) {
        if (inSetSelectedElement) {
            return;
        }
        try {
            inSetSelectedElement = true;
            selectedJobNode = jobNode;
            updatePropertySheet();
            if (reason != TREE_SELECTION_CHANGED)
                changeTreeSelection();
            if (reason != CARET_MOVED && jobNode != null)
                moveEditorCaret();
        } finally {
            inSetSelectedElement = false;
        }
    }

    private void changeTreeSelection() {
//        TreePath path = getPath(getSelectedNode());
//        tree.expandPath(path);
//        tree.scrollPathToVisible(path);
//        tree.setSelectionPath(path);
    }

    private TreePath getPath(CfgJobInfo node) {
//        if (node == null) return null;
//        LinkedList list = new LinkedList();
//        while (node != null && node != _rootElement)
//        {
//            list.addFirst(node);
//            node = node.getParent();
//        }
//        if (node != null)
//            list.addFirst(node);
//        TreePath treePath = new TreePath(list.toArray());
//        return treePath;
        return null;
    }


    private void moveEditorCaret() {
        if (cfgPluginController.isAutoScrollToSource()) {
            caretMover.moveEditorCaret(getSelectedNode());
        }
    }

    private void updatePropertySheet() {
//        if (!_projectComponent.isShowProperties())
//            return;
//        _propertyPanel.setTarget(_selectedElement);
//        _propertyPanel.getTable().getTableHeader().setReorderingAllowed(false);
//
//        _propertyHeaderRenderer.setIconForElement(_selectedElement);
//        _propertyPanel.getTable().getColumnModel().getColumn(0).setHeaderRenderer(_propertyHeaderRenderer);
//        _propertyPanel.getTable().getColumnModel().getColumn(1).setHeaderRenderer(_valueHeaderRenderer);
//
//        if (_selectedElement != null)
//            _splitPane.setDividerLocation(_projectComponent.getSplitDividerLocation());
    }

    public void selectElementAtCaret()
    {
        selectElementAtCaret(FileEditorManager.getInstance(project).getSelectedTextEditor(), null);
    }

    public void selectElementAtCaret(@Nullable Editor editor, @Nullable String changeSource) {
        if (editor == null) {
            return;
        }

//        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
//
//        PsiElement elementAtCaret = null;
//        if (psiFile != null) {
//            Language selectedLanguage = cfgPluginController.getSelectedLanguage();
//            FileViewProvider viewProvider = psiFile.getViewProvider();
//
//            if (selectedLanguage != null) {
//                PsiFile selectedRoot = viewProvider.getPsi(selectedLanguage);
//                if (selectedRoot == null) {
//                    selectedLanguage = null;
//                }
//            }
//
//            if (selectedLanguage == null) {
//                selectedLanguage = psiFile.getLanguage();
//            }
//
//            elementAtCaret = viewProvider.findElementAt(editor.getCaretModel().getOffset(), selectedLanguage);
//
//            if (elementAtCaret != null && elementAtCaret.getParent() != null) {
//                if (elementAtCaret.getParent().getChildren().length == 0)
//                    elementAtCaret = elementAtCaret.getParent();
//            }
//        }
//
//        if (elementAtCaret != null && elementAtCaret != getSelectedElement()) {
//            debug("new element at caret " + elementAtCaret + ", current root=" + getRootElement());
//            if (!PsiTreeUtil.isAncestor(getRootElement(), elementAtCaret, false))
//                selectRootElement(psiFile, TITLE_PREFIX_CURRENT_FILE);
//            setSelectedElement(elementAtCaret, changeSource == null ? PsiViewerPanel.CARET_MOVED : changeSource);
//        }
    }
}
