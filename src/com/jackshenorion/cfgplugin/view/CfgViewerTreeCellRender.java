package com.jackshenorion.cfgplugin.view;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.jackshenorion.cfgplugin.CfgIcons;
import com.jackshenorion.cfgplugin.model.CfgJobInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.ui.SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES;

public class CfgViewerTreeCellRender extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        CfgJobInfo jobInfo = (CfgJobInfo) value;
        if (jobInfo == null) {
            append("null");
        }

        if (jobInfo.isStandardJob()) {
            setIcon(CfgIcons.STANDARD_TASK);
        } else {
            setIcon(leaf ? CfgIcons.TASK : CfgIcons.TASK_GROUP);
        }
        append(jobInfo.getName());
        append(" " + jobInfo.getJobClass().getName(), GRAY_ITALIC_ATTRIBUTES);
    }
}
