package com.jackshenorion.cfgplugin;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jackshenorion.cfgplugin.psi.CfgFile;
import com.jackshenorion.cfgplugin.psi.CfgSegment;
import com.jackshenorion.cfgplugin.psi.impl.CfgSegmentImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CfgStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    private NavigatablePsiElement element;

    public CfgStructureViewElement(NavigatablePsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        element.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return element.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element.canNavigateToSource();
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        String name = element.getName();
        return name != null ? name : "";
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        ItemPresentation presentation = element.getPresentation();
        return presentation != null ? presentation : new PresentationData();
    }

    @Override
    public TreeElement[] getChildren() {
        if (element instanceof CfgFile) {
            CfgSegment[] segments = PsiTreeUtil.getChildrenOfType(element, CfgSegment.class);
            List<TreeElement> treeElements = new ArrayList<TreeElement>(segments.length);
            for (CfgSegment segment : segments) {
                treeElements.add(new CfgStructureViewElement((CfgSegmentImpl) segment));
            }
            return treeElements.toArray(new TreeElement[treeElements.size()]);
        } else {
            return EMPTY_ARRAY;
        }
    }
}