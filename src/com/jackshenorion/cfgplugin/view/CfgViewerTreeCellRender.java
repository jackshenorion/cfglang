package com.jackshenorion.cfgplugin.view;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.jackshenorion.cfgplugin.CfgIcons;
import com.jackshenorion.cfgplugin.model.CfgJobInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CfgViewerTreeCellRender extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        CfgJobInfo jobInfo = (CfgJobInfo) value;
        setIcon(CfgIcons.FILE);
        append(jobInfo != null ?
                ((jobInfo.isStandardJob() ? "Standard." : "")
                        + jobInfo.getJobClass().getName()
                        + ":" + jobInfo.getName()) : "null");
    }
}
