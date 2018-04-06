package com.jackshenorion.cfgplugin;

import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;
import com.jackshenorion.cfgplugin.psi.CfgFile;
import org.jetbrains.annotations.NotNull;

public class CfgStructureViewModel extends StructureViewModelBase implements
        StructureViewModel.ElementInfoProvider {
    public CfgStructureViewModel(PsiFile psiFile) {
        super(psiFile, new CfgStructureViewElement(psiFile));
    }

    @NotNull
    public Sorter[] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }


    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element instanceof CfgFile;
    }
}