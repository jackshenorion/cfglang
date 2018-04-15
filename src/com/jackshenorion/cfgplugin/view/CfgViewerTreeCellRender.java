package com.jackshenorion.cfgplugin.view;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.jackshenorion.cfgplugin.CfgIcons;
import com.jackshenorion.cfgplugin.model.ViewerTreeNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.ui.SimpleTextAttributes.ERROR_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES;

public class CfgViewerTreeCellRender extends ColoredTreeCellRenderer {

    private final static SimpleTextAttributes MY_NORMAL_NAME = SimpleTextAttributes.REGULAR_ATTRIBUTES;
    private final static SimpleTextAttributes MY_ERROR_NAME = new SimpleTextAttributes(SimpleTextAttributes.STYLE_WAVED, null, JBColor.red);
    private final static SimpleTextAttributes MY_ERROR_JOB_CLASS = new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, JBColor.red);
    private final static SimpleTextAttributes MY_SUPER_JOB = new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, JBColor.BLUE);
    private final static SimpleTextAttributes MY_SUPER_JOB_Name = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.ORANGE);

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        ViewerTreeNode jobInfo = (ViewerTreeNode) value;
        if (jobInfo == null) {
            append("null");
        }
        if (jobInfo.isExtendedByOtherJob()) {
            setIcon(CfgIcons.Right);
        } else if (jobInfo.isBaseJob()) {
            setIcon(CfgIcons.BASE_JOB);
        } else {
            setIcon(leaf ? CfgIcons.PROJECT_JOB : CfgIcons.TASK_GROUP);
        }
        if (jobInfo.isExtendedByOtherJob()) {
            append("extends ", MY_SUPER_JOB);
        }
        if (jobInfo.isOnErrorPath()) {
            append("" + jobInfo.getName(), MY_ERROR_NAME);
        } else {
            append("" + jobInfo.getName());
        }
        if (jobInfo.isUndefined()) {
            append(" Undefined", ERROR_ATTRIBUTES);
        } else {
            append(" " + jobInfo.getJobClass(), jobInfo.isJobClassUndefined() ? MY_ERROR_JOB_CLASS : GRAY_ITALIC_ATTRIBUTES);
            if (jobInfo.isDuplicate()) {
                append(" Duplicate", ERROR_ATTRIBUTES);
            }
        }
    }
}
