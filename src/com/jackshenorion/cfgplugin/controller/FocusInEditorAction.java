package com.jackshenorion.cfgplugin.controller;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;

public class FocusInEditorAction  extends AnAction {

    private CfgPluginController cfgPluginController;

    public FocusInEditorAction(String actionName, String toolTip, Icon icon, CfgPluginController cfgPluginController) {
        super(actionName, toolTip, icon);
        this.cfgPluginController = cfgPluginController;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        cfgPluginController.onFocusInEditorClicked();
    }
}
