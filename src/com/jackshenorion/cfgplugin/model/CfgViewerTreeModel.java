package com.jackshenorion.cfgplugin.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jackshenorion.cfgplugin.controller.CfgPluginController;
import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.psi.CfgFile;
import com.jackshenorion.cfgplugin.psi.CfgProperty;
import com.jackshenorion.cfgplugin.psi.CfgSegment;

import javax.annotation.Nullable;
import javax.swing.event.TreeModelListener;
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
    private CfgJobInfo root;

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
        this.root = new CfgJobInfo("Root", false, true);
    }

    public void setCfgFiles(List<CfgFile> cfgFiles, List<CfgFile> baseCfgFiles) {
        init();
        this.normalCfgFiles = cfgFiles;
        this.standardCfgFiles = baseCfgFiles;
        readStandardJobs();
        readNormalJobs();
    }

    private void readStandardJobs() {
        CfgJobInfo currentJobInfo = null;
        for (CfgFile cfgFile : standardCfgFiles) {
            currentJobInfo = null;
            List<PsiElement> elements = PsiTreeUtil.getChildrenOfAnyType(cfgFile, CfgSegment.class, CfgProperty.class);
            for (PsiElement element : elements) {
                if (element instanceof CfgSegment) {
                    CfgSegment segment = (CfgSegment) element;
                    currentJobInfo = new CfgJobInfo(segment.getName(), true);
                    standardJobs.put(segment.getName(), currentJobInfo);
                    currentJobInfo.setCfgSegment(segment);
                } else if (element instanceof CfgProperty) {
                    CfgProperty property = (CfgProperty) element;
                    if (currentJobInfo != null) {
                        currentJobInfo.addCfgProperty(property);
                    }
                }
            }
        }
    }

    private void readNormalJobs() {
        CfgJobInfo currentJobInfo = null;
        for (CfgFile cfgFile : normalCfgFiles) {
            currentJobInfo = null;
            List<PsiElement> elements = PsiTreeUtil.getChildrenOfAnyType(cfgFile, CfgSegment.class, CfgProperty.class);
            for (PsiElement element : elements) {
                if (element instanceof CfgSegment) {
                    CfgSegment segment = (CfgSegment) element;
                    currentJobInfo = new CfgJobInfo(segment.getName(), false);
                    normalJobs.put(segment.getName(), currentJobInfo);
                    currentJobInfo.setCfgSegment(segment);
                    jobAdjacencyList.putIfAbsent(segment.getName(), new ArrayList<>());
                } else if (element instanceof CfgProperty) {
                    CfgProperty property = (CfgProperty) element;
                    if (currentJobInfo != null) {
                        currentJobInfo.addCfgProperty(property);
                    }
                    if (currentJobInfo != null && CfgUtil.needJob(property.getKey())) {
                        jobAdjacencyList.get(currentJobInfo.getName()).add(property.getValue());
                    }
                }
            }
        }
        jobAdjacencyList.put("Root", getRootJobs());
        normalJobs.put("Root", root);
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
        return root;
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

    @Nullable
    public CfgSegment getSegment(PsiElement element) {
        return element instanceof CfgSegment ?
                (CfgSegment) element : PsiTreeUtil.getPrevSiblingOfType(element, CfgSegment.class);
    }

    @Nullable
    public CfgJobInfo getNode(CfgSegment segment) {
        return normalJobs.get(segment.getName());
    }

    @Nullable
    public CfgJobInfo getParent(CfgJobInfo node) {
        for (String key : jobAdjacencyList.keySet()) {
            if (jobAdjacencyList.get(key).contains(node.getName())) {
                return normalJobs.get(key);
            }
        }
        return null;
    }

    public TreePath getPath(PsiElement element) {
        CfgSegment segment = getSegment(element);
        if (segment == null) {
            return null;
        }

        CfgJobInfo node = getNode(segment);
        if (node == null) {
            return null;
        }

        LinkedList list = new LinkedList();
        while (node != null && node != root) {
            list.addFirst(node);
            node = getParent(node);
        }
        if (node != null)
            list.addFirst(node);
        TreePath treePath = new TreePath(list.toArray());
        return treePath;

    }
}
