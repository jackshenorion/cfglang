package com.jackshenorion.cfgplugin.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.*;
import com.jackshenorion.cfgplugin.CfgIcons;
import com.jackshenorion.cfgplugin.psi.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CfgPsiImplUtil {
    public static String getKey(CfgProperty element) {
        ASTNode keyNode = element.getNode().findChildByType(CfgTypes.KEY);
        if (keyNode != null) {
            // IMPORTANT: Convert embedded escaped spaces to simple spaces
            return keyNode.getText().replaceAll("\\\\ ", " ");
        } else {
            return null;
        }
    }

    public static String getValue(CfgProperty element) {
        ASTNode valueNode = element.getNode().findChildByType(CfgTypes.VALUE);
        if (valueNode != null) {
            return valueNode.getText();
        } else {
            return null;
        }
    }

    public static String getName(CfgSegment element) {
        ASTNode segmentNameNode = element.getNode().findChildByType(CfgTypes.SEGMENT_NAME);
        if (segmentNameNode != null) {
            return segmentNameNode.getText();
        } else {
            return null;
        }
    }
}