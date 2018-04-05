package com.jackshenorion.cfgplugin.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
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

    public static PsiElement setName(CfgSegment element, String newName) {
        ASTNode segmentNameNode = element.getNode().findChildByType(CfgTypes.SEGMENT_NAME);
        if (segmentNameNode != null) {
            CfgSegment segment = CfgElementFactory.createSegment(element.getProject(), newName);
            ASTNode newKeyNode = segment.getFirstChild().getNode();
            element.getNode().replaceChild(segmentNameNode, newKeyNode);
        }
        return element;
    }

    public static PsiElement getNameIdentifier(CfgSegment element) {
        ASTNode keyNode = element.getNode().findChildByType(CfgTypes.SEGMENT_NAME);
        if (keyNode != null) {
            return keyNode.getPsi();
        } else {
            return null;
        }
    }

    public static String getName(CfgProperty element) {
        return getValue(element);
    }

    public static PsiElement setName(CfgProperty element, String newName) {
        ASTNode keyNode = element.getNode().findChildByType(CfgTypes.VALUE);
        if (keyNode != null) {
            CfgProperty property = CfgElementFactory.createProperty(element.getProject(), newName);
            ASTNode newKeyNode = property.getFirstChild().getNode();
            element.getNode().replaceChild(keyNode, newKeyNode);
        }
        return element;
    }

    public static PsiElement getNameIdentifier(CfgProperty element) {
        ASTNode keyNode = element.getNode().findChildByType(CfgTypes.VALUE);
        if (keyNode != null) {
            return keyNode.getPsi();
        } else {
            return null;
        }
    }

    public static PsiReference[] getReferences(CfgProperty element) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element, CfgPropertyImpl.class);
    }

    public static PsiReference[] getReferences(CfgSegment element) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element, CfgSegmentImpl.class);
    }
}