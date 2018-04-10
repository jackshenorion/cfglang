package com.jackshenorion.cfgplugin.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jackshenorion.cfgplugin.CfgPluginController;
import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.psi.CfgFile;
import com.jackshenorion.cfgplugin.psi.CfgProperty;
import com.jackshenorion.cfgplugin.psi.CfgSegment;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

public class CfgViewerTreeModel implements TreeModel {
    private CfgPluginController cfgPluginController;
    private List<CfgFile> normalCfgFiles;
    private Map<String, List<String>> jobAdjacencyList;

    public CfgViewerTreeModel(CfgPluginController cfgPluginController) {
        this.cfgPluginController = cfgPluginController;
        init();
    }

    private void init() {
        this.normalCfgFiles = new ArrayList<>();
        this.jobAdjacencyList = new HashMap<>();
    }

    public void setCfgFiles(List<CfgFile> cfgFiles) {
        init();
        normalCfgFiles = cfgFiles;
        final String[] currentSegment = {null};
        normalCfgFiles.forEach(cfgFile -> {
            currentSegment[0] = null;
            List<PsiElement> elements = PsiTreeUtil.getChildrenOfAnyType(cfgFile, CfgSegment.class, CfgProperty.class);
            elements.forEach(element -> {
                if (element instanceof CfgSegment) {
                    currentSegment[0] = ((CfgSegment) element).getName();
                    jobAdjacencyList.putIfAbsent(currentSegment[0], new ArrayList<>());
                } else if (element instanceof CfgProperty) {
                    CfgProperty property = (CfgProperty) element;
                    if (currentSegment[0] != null && CfgUtil.needJob(property.getKey())) {
                        jobAdjacencyList.get(currentSegment[0]).add(property.getValue());
                    }
                }
            });
        });
        jobAdjacencyList.put("Root", getRootJobs());
    }

    private List<String> getRootJobs() {
        Set<String> keySet = new HashSet<>(jobAdjacencyList.keySet());
        jobAdjacencyList.values().forEach(referencedJobs -> {
            referencedJobs.forEach(referencedJob -> keySet.remove(referencedJob));
        });
        return new ArrayList<>(keySet);
    }

    @Override
    public Object getRoot() {
        return "Root";
    }

    @Override
    public Object getChild(Object parent, int index) {
        return jobAdjacencyList.getOrDefault(parent, Collections.EMPTY_LIST).get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return jobAdjacencyList.getOrDefault(parent, Collections.EMPTY_LIST).size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return jobAdjacencyList.getOrDefault(node, Collections.EMPTY_LIST).size() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return jobAdjacencyList.getOrDefault(parent, Collections.EMPTY_LIST).indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {

    }
}
