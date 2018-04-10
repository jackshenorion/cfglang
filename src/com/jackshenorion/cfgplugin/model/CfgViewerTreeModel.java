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
import javax.swing.text.Segment;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

public class CfgViewerTreeModel implements TreeModel {
    private CfgPluginController cfgPluginController;
    private List<CfgFile> normalCfgFiles;
    private List<CfgFile> standardCfgFiles;
    private Map<String, List<String>> jobAdjacencyList;
    private Map<String, CfgJobInfo> standardJobs;
    private Map<String, CfgJobInfo> normalJobs;

    public CfgViewerTreeModel(CfgPluginController cfgPluginController) {
        this.cfgPluginController = cfgPluginController;
        init();
    }

    private void init() {
        this.normalCfgFiles = new ArrayList<>();
        this.standardCfgFiles = new ArrayList<>();
        this.jobAdjacencyList = new HashMap<>();
        this.standardJobs = new HashMap<>();
        this.normalJobs = new HashMap<>();
    }

    public void setCfgFiles(List<CfgFile> cfgFiles, List<CfgFile> baseCfgFiles) {
        init();
        this.normalCfgFiles = cfgFiles;
        this.standardCfgFiles = baseCfgFiles;
        readStandardJobs();
        readNormalJobs();
    }

    private void readStandardJobs() {
        final CfgJobInfo[] currentJobInfo = {null};
        standardCfgFiles.forEach(cfgFile -> {
            currentJobInfo[0] = null;
            List<PsiElement> elements = PsiTreeUtil.getChildrenOfAnyType(cfgFile, CfgSegment.class, CfgProperty.class);
            elements.forEach(element -> {
                if (element instanceof CfgSegment) {
                    CfgSegment segment = (CfgSegment) element;
                    currentJobInfo[0] = new CfgJobInfo(segment.getName(), true);
                    standardJobs.put(segment.getName(), currentJobInfo[0]);
                    currentJobInfo[0].setCfgSegment(segment);
                } else if (element instanceof CfgProperty) {
                    CfgProperty property = (CfgProperty) element;
                    if (currentJobInfo[0] != null) {
                        currentJobInfo[0].addCfgProperty(property);
                    }
                }
            });
        });

    }

    private void readNormalJobs() {
        final CfgJobInfo[] currentJobInfo = {null};
        normalCfgFiles.forEach(cfgFile -> {
            currentJobInfo[0] = null;
            List<PsiElement> elements = PsiTreeUtil.getChildrenOfAnyType(cfgFile, CfgSegment.class, CfgProperty.class);
            elements.forEach(element -> {
                if (element instanceof CfgSegment) {
                    CfgSegment segment = (CfgSegment) element;
                    currentJobInfo[0] = new CfgJobInfo(segment.getName(), false);
                    normalJobs.put(segment.getName(), currentJobInfo[0]);
                    currentJobInfo[0].setCfgSegment(segment);
                    jobAdjacencyList.putIfAbsent(segment.getName(), new ArrayList<>());
                } else if (element instanceof CfgProperty) {
                    CfgProperty property = (CfgProperty) element;
                    if (currentJobInfo[0] != null) {
                        currentJobInfo[0].addCfgProperty(property);
                    }
                    if (currentJobInfo[0] != null && CfgUtil.needJob(property.getKey())) {
                        jobAdjacencyList.get(currentJobInfo[0].getName()).add(property.getValue());
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
        return new CfgJobInfo("Root", false, true);
    }

    @Override
    public Object getChild(Object parent, int index) {
        CfgJobInfo jobInfo = (CfgJobInfo) parent;
        String childJobName = jobAdjacencyList.getOrDefault(jobInfo.getName(), Collections.emptyList()).get(index);
        return normalJobs.getOrDefault(childJobName, standardJobs.get(childJobName));
    }

    @Override
    public int getChildCount(Object parent) {
        CfgJobInfo jobInfo = (CfgJobInfo) parent;
        return jobAdjacencyList.getOrDefault(jobInfo.getName(), Collections.emptyList()).size();
    }

    @Override
    public boolean isLeaf(Object node) {
        CfgJobInfo jobInfo = (CfgJobInfo) node;
        return jobAdjacencyList.getOrDefault(jobInfo.getName(), Collections.emptyList()).size() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        CfgJobInfo parentJobInfo = (CfgJobInfo) parent;
        CfgJobInfo childJobInfo = (CfgJobInfo) child;
        return jobAdjacencyList.getOrDefault(parentJobInfo.getName(), Collections.emptyList()).indexOf(childJobInfo.getName());
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {

    }
}
