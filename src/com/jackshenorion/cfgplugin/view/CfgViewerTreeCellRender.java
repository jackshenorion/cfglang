package com.jackshenorion.cfgplugin.view;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.jackshenorion.cfgplugin.CfgIcons;
import com.jackshenorion.cfgplugin.model.CfgViewerTreeNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.ui.SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES;

public class CfgViewerTreeCellRender extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        CfgViewerTreeNode jobInfo = (CfgViewerTreeNode) value;
        if (jobInfo == null) {
            append("null");
        }

        if (jobInfo.isBaseJob()) {
            setIcon(CfgIcons.STANDARD_TASK);
        } else if (jobInfo.isUndefined()) {
            setIcon(CfgIcons.UNDEFINED_TASK);
        } else {
            setIcon(leaf ? CfgIcons.TASK : CfgIcons.TASK_GROUP);
        }
        append(jobInfo.getName());
        append(" " + jobInfo.getJobClass().getName(), GRAY_ITALIC_ATTRIBUTES);
    }
}
