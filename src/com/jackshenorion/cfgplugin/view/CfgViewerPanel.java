package com.jackshenorion.cfgplugin.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.jackshenorion.cfgplugin.CfgPluginController;
import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.model.CfgViewerTreeModel;

import javax.swing.*;
import java.awt.*;

public class CfgViewerPanel extends JPanel{

    private CfgPluginController cfgPluginController;
    private Project project;
    private CfgViewerTree tree;
    private CfgViewerTreeModel model;

    public CfgViewerPanel(CfgPluginController cfgPluginController) {
        this.cfgPluginController = cfgPluginController;
        this.project = cfgPluginController.getProject();
        setLayout(new BorderLayout());
        model = new CfgViewerTreeModel(cfgPluginController);
        tree = new CfgViewerTree(model);
        add(new JBScrollPane(tree), BorderLayout.CENTER);
        setBackground(Color.WHITE);
    }

    public void resetTree(VirtualFile virtualFile) {
        this.model = new CfgViewerTreeModel(this.cfgPluginController);
        this.model.setCfgFiles(CfgUtil.findNormalCfgFiles(project, virtualFile), CfgUtil.findStandardCfgFiles(project));
        this.tree.setModel(this.model);
    }
}
