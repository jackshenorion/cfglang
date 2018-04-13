package com.jackshenorion.cfgplugin.view;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.jackshenorion.cfgplugin.CfgIcons;
import com.jackshenorion.cfgplugin.model.CfgViewerTreeNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.ui.SimpleTextAttributes.ERROR_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES;

public class CfgViewerTreeCellRender extends ColoredTreeCellRenderer {

    private final static SimpleTextAttributes MY_ERROR_NAME = new SimpleTextAttributes(SimpleTextAttributes.STYLE_WAVED, null, JBColor.red);
    private final static SimpleTextAttributes MY_ERROR_JOB_CLASS = new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, JBColor.red);

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        CfgViewerTreeNode jobInfo = (CfgViewerTreeNode) value;
        if (jobInfo == null) {
            append("null");
        }
        if (jobInfo.isBaseJob()) {
            setIcon(CfgIcons.BASE_JOB);
        } else {
            setIcon(leaf ? CfgIcons.PROJECT_JOB : CfgIcons.TASK_GROUP);
        }
        if (jobInfo.isOnErrorPath()) {
            append(jobInfo.getName(), MY_ERROR_NAME);
        } else {
            append(jobInfo.getName());
        }
        append(" " + jobInfo.getJobClass(), jobInfo.isJobClassUndefined() ? MY_ERROR_JOB_CLASS : GRAY_ITALIC_ATTRIBUTES);
        if (jobInfo.isUndefined()) {
            append(" Undefined", ERROR_ATTRIBUTES);
        }
        if (jobInfo.isDuplicate()) {
            append(" Duplicate", ERROR_ATTRIBUTES);
        }
    }
}
