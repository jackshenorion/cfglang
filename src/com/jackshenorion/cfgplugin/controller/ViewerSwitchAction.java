package com.jackshenorion.cfgplugin.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

import javax.swing.*;

public class ViewerSwitchAction extends ToggleAction {

    private CfgPluginController cfgPluginController;
    private String switchName;

    public ViewerSwitchAction(String actionName, String toolTip, Icon icon, CfgPluginController cfgPluginController, String switchName) {
        super(actionName, toolTip, icon);
        this.cfgPluginController = cfgPluginController;
        this.switchName = switchName;
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        return cfgPluginController.getViewSwitch(switchName);
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        cfgPluginController.onViewerSwitch(switchName, state);
    }
}
