package com.jackshenorion.cfgplugin.view;

import com.intellij.ui.treeStructure.Tree;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

public class CfgViewerTree extends Tree {

    public CfgViewerTree(TreeModel treemodel) {
        super(treemodel);
        setCellRenderer(new CfgViewerTreeCellRender());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setExpandsSelectedPaths(true);
        setRootVisible(false);
    }
}
