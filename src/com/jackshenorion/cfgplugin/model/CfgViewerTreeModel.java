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
    private final static String JOB_ROOT = "Root";

    private CfgPluginController cfgPluginController;
    private List<CfgFile> projectCfgFiles;
    private List<CfgFile> baseCfgFiles;
    private Map<String, List<String>> jobAdjacencyList;
    private Map<String, List<String>> parentAdjacencyList;
    private Map<String, CfgViewerTreeNode> baseJobs;
    private Map<String, CfgViewerTreeNode> projectJobs;
    private CfgViewerTreeNode rootJobNode;
    private List<String> errorJobs;

    public CfgViewerTreeModel(CfgPluginController cfgPluginController) {
        this.cfgPluginController = cfgPluginController;
        init();
    }

    private void init() {
        this.projectCfgFiles = new ArrayList<>();
        this.baseCfgFiles = new ArrayList<>();
        this.jobAdjacencyList = new HashMap<>();
        this.parentAdjacencyList = new HashMap<>();
        this.baseJobs = new HashMap<>();
        this.projectJobs = new HashMap<>();
        this.rootJobNode = new CfgViewerTreeNode(JOB_ROOT, false, true);
    }

    public void setCfgFiles(List<CfgFile> projectCfgFiles, List<CfgFile> baseCfgFiles) {
        init();
        this.projectCfgFiles = projectCfgFiles;
        this.baseCfgFiles = baseCfgFiles;
        readBaseJobs();
        readProjectJobs();
        addUndefinedJobs();
    }

    private void readBaseJobs() {
        CfgViewerTreeNode currentJobInfo = null;
        for (CfgFile cfgFile : baseCfgFiles) {
            currentJobInfo = null;
            List<PsiElement> elements = PsiTreeUtil.getChildrenOfAnyType(cfgFile, CfgSegment.class, CfgProperty.class);
            for (PsiElement element : elements) {
                if (element instanceof CfgSegment) {
                    CfgSegment segment = (CfgSegment) element;
                    if (baseJobs.containsKey(segment.getName())) {
                        baseJobs.get(segment.getName()).setDuplicate(true);
                        errorJobs.add(segment.getName());
                        currentJobInfo = null;
                        continue;
                    }
                    currentJobInfo = new CfgViewerTreeNode(segment.getName(), true);
                    baseJobs.put(segment.getName(), currentJobInfo);
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

    private void readProjectJobs() {
        CfgViewerTreeNode currentJobInfo = null;
        for (CfgFile cfgFile : projectCfgFiles) {
            currentJobInfo = null;
            List<PsiElement> elements = PsiTreeUtil.getChildrenOfAnyType(cfgFile, CfgSegment.class, CfgProperty.class);
            for (PsiElement element : elements) {
                if (element instanceof CfgSegment) {
                    CfgSegment segment = (CfgSegment) element;
                    if (projectJobs.containsKey(segment.getName())) {
                        projectJobs.get(segment.getName()).setDuplicate(true);
                        currentJobInfo = null;
                        continue;
                    }
                    currentJobInfo = new CfgViewerTreeNode(segment.getName(), false);
                    projectJobs.put(segment.getName(), currentJobInfo);
                    currentJobInfo.setCfgSegment(segment);
                    jobAdjacencyList.putIfAbsent(segment.getName(), new ArrayList<>());
                } else if (element instanceof CfgProperty) {
                    CfgProperty property = (CfgProperty) element;
                    if (currentJobInfo != null) {
                        currentJobInfo.addCfgProperty(property);
                    }
                    if (currentJobInfo != null && CfgUtil.isJobKey(property.getKey())) {
                        jobAdjacencyList.get(currentJobInfo.getName()).add(property.getValue());
                        parentAdjacencyList.putIfAbsent(property.getValue(), new ArrayList<>());
                        parentAdjacencyList.get(property.getValue()).add(currentJobInfo.getName());
                    }
                }
            }
        }
        jobAdjacencyList.put(JOB_ROOT, getRootJobs());
        projectJobs.put(JOB_ROOT, rootJobNode);
    }

    private void addUndefinedJobs() {
        for (List<String> jobs : jobAdjacencyList.values()) {
            for (String job : jobs) {
                if (projectJobs.getOrDefault(job, baseJobs.get(job)) == null) {
                    CfgViewerTreeNode undefinedJob = new CfgViewerTreeNode(job, false, false, true);
                    projectJobs.put(job, undefinedJob);
                }
            }
        }
    }

    private void markErrorNode() {
        int[] color = new int[jobAdjacencyList.size()];

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
        return rootJobNode;
    }

    @Override
    public Object getChild(Object parent, int index) {
        CfgViewerTreeNode jobInfo = (CfgViewerTreeNode) parent;
        String childJobName = jobAdjacencyList.getOrDefault(jobInfo.getName(), Collections.emptyList()).get(index);
        return projectJobs.getOrDefault(childJobName, baseJobs.get(childJobName));
    }

    @Override
    public int getChildCount(Object parent) {
        CfgViewerTreeNode jobInfo = (CfgViewerTreeNode) parent;
        if (jobInfo.isBaseJob()) {
            return 0;
        }
        return jobAdjacencyList.getOrDefault(jobInfo.getName(), Collections.emptyList()).size();
    }

    @Override
    public boolean isLeaf(Object node) {
        CfgViewerTreeNode jobInfo = (CfgViewerTreeNode) node;
        if (jobInfo.isBaseJob()) {
            return true;
        }
        return jobAdjacencyList.getOrDefault(jobInfo.getName(), Collections.emptyList()).size() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        CfgViewerTreeNode parentJobInfo = (CfgViewerTreeNode) parent;
        CfgViewerTreeNode childJobInfo = (CfgViewerTreeNode) child;
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
    public CfgViewerTreeNode getNode(CfgSegment segment) {
        return projectJobs.get(segment.getName());
    }

    @Nullable
    public CfgViewerTreeNode getParent(CfgViewerTreeNode node) {
        for (String key : jobAdjacencyList.keySet()) {
            if (jobAdjacencyList.get(key).contains(node.getName())) {
                return projectJobs.get(key);
            }
        }
        return null;
    }

    public TreePath getPath(PsiElement element) {
        CfgSegment segment = getSegment(element);
        if (segment == null) {
            return null;
        }

        CfgViewerTreeNode node = getNode(segment);
        if (node == null) {
            return null;
        }

        LinkedList list = new LinkedList();
        while (node != null && node != rootJobNode) {
            list.addFirst(node);
            node = getParent(node);
        }
        if (node != null)
            list.addFirst(node);
        TreePath treePath = new TreePath(list.toArray());
        return treePath;

    }
}
