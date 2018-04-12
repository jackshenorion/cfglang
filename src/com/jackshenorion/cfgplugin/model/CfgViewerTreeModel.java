package com.jackshenorion.cfgplugin.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiFileImpl;
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
    private CfgViewerTreeNode rootJobNode;

    private Map<String, List<String>> adjacencyList;
    private Map<String, List<String>> reverseAdjacencyList;
    private Map<String, CfgViewerTreeNode> jobNameToTreeNode;

    public CfgViewerTreeModel(CfgPluginController cfgPluginController) {
        this.cfgPluginController = cfgPluginController;
        init();
    }

    private void init() {
        projectCfgFiles = new ArrayList<>();
        baseCfgFiles = new ArrayList<>();
        adjacencyList = new LinkedHashMap<>();
        reverseAdjacencyList = new HashMap<>();
        jobNameToTreeNode = new LinkedHashMap<>();
        rootJobNode = new CfgViewerTreeNode(JOB_ROOT, false, true);
    }

    public void setCfgFiles(List<CfgFile> projectCfgFiles, List<CfgFile> baseCfgFiles) {
        init();
        Comparator<CfgFile> cfgFileComparator = Comparator.comparing(PsiFileImpl::getName);
        this.projectCfgFiles = projectCfgFiles;
        this.baseCfgFiles = baseCfgFiles;
        Collections.sort(this.projectCfgFiles, cfgFileComparator);
        Collections.sort(this.baseCfgFiles, cfgFileComparator);
        readJobs();
        addUndefinedJobs();
    }

    private void readJobs() {
        readJobs(projectCfgFiles, false); // project jobs has higher priority, so we read them first
        readJobs(baseCfgFiles, true);
        adjacencyList.put(JOB_ROOT, getRootJobs());
        jobNameToTreeNode.put(JOB_ROOT, rootJobNode);
    }

    private void readJobs(List<CfgFile> cfgFiles, boolean isBaseCfgFile) {
        CfgViewerTreeNode[] currentTreeNode = {null};
        for (CfgFile cfgFile : cfgFiles) {
            currentTreeNode[0] = null;
            List<PsiElement> elements = PsiTreeUtil.getChildrenOfAnyType(cfgFile, CfgSegment.class, CfgProperty.class);
            for (PsiElement element : elements) {
                if (element instanceof CfgSegment) {
                    onCfgSegment(currentTreeNode, (CfgSegment) element, isBaseCfgFile);
                } else if (element instanceof CfgProperty) {
                    onCfgProperty(currentTreeNode, (CfgProperty) element);
                }
            }
        }
    }

    private void onCfgSegment(CfgViewerTreeNode[] currentTreeNode, CfgSegment segment, boolean isBaseJob) {
        if (jobNameToTreeNode.containsKey(segment.getName())) {
            if (!isBaseJob) {
                jobNameToTreeNode.get(segment.getName()).setDuplicate(true);
            }
            currentTreeNode[0] = null;
            return;
        }
        CfgViewerTreeNode newNode = new CfgViewerTreeNode(segment.getName(), isBaseJob);
        newNode.setCfgSegment(segment);
        currentTreeNode[0] = newNode;
        jobNameToTreeNode.put(segment.getName(), newNode);
        adjacencyList.put(newNode.getName(), new ArrayList<>());
    }

    private void onCfgProperty(CfgViewerTreeNode[] currentTreeNode, CfgProperty property) {
        if (currentTreeNode[0] == null) {
            return;
        }
        CfgViewerTreeNode currentNode = currentTreeNode[0];
        currentNode.addCfgProperty(property);
        if (CfgUtil.isJobKey(property.getKey())) {
            String targetJobName = property.getValue();
            adjacencyList.get(currentNode.getName()).add(targetJobName);
            reverseAdjacencyList.putIfAbsent(targetJobName, new ArrayList<>());
            reverseAdjacencyList.get(targetJobName).add(currentNode.getName());
        }
    }

    private void addUndefinedJobs() {
        for (List<String> jobs : adjacencyList.values()) {
            for (String job : jobs) {
                if (jobNameToTreeNode.get(job) == null) {
                    CfgViewerTreeNode undefinedJobNode = new CfgViewerTreeNode(job, false, false, true);
                    jobNameToTreeNode.put(job, undefinedJobNode);
                }
            }
        }
    }

    private List<String> getRootJobs() {
        List<String> rootJobList = new ArrayList<>();
        for (String sourceJobName : adjacencyList.keySet()) {
            if (reverseAdjacencyList.get(sourceJobName) == null) { // this node point to no parent
                rootJobList.add(sourceJobName);
            }
        }
        return rootJobList;
    }

//    private void markErrorPath() {
//        int[] color = new int[jobAdjacencyList.size()];
//        for (int i = 0; i < color.length; i++) {
//            String startJobName =
//                    projectJobs.getOrDefault();
//
//
//        }
//    }

    @Override
    public Object getRoot() {
        return rootJobNode;
    }

    @Override
    public Object getChild(Object parent, int index) {
        CfgViewerTreeNode jobInfo = (CfgViewerTreeNode) parent;
        String childJobName = adjacencyList.get(jobInfo.getName()).get(index);
        return jobNameToTreeNode.get(childJobName);
    }

    @Override
    public int getChildCount(Object parent) {
        CfgViewerTreeNode jobInfo = (CfgViewerTreeNode) parent;
        return adjacencyList.getOrDefault(jobInfo.getName(), Collections.emptyList()).size();
    }

    @Override
    public boolean isLeaf(Object node) {
        CfgViewerTreeNode jobInfo = (CfgViewerTreeNode) node;
        return adjacencyList.getOrDefault(jobInfo.getName(), Collections.emptyList()).size() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        CfgViewerTreeNode parentJobInfo = (CfgViewerTreeNode) parent;
        CfgViewerTreeNode childJobInfo = (CfgViewerTreeNode) child;
        return adjacencyList.getOrDefault(parentJobInfo.getName(), Collections.emptyList()).indexOf(childJobInfo.getName());
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
        return jobNameToTreeNode.get(segment.getName());
    }

    @Nullable
    public CfgViewerTreeNode getParent(CfgViewerTreeNode node) {
        if (adjacencyList.get(node.getName()).size() == 0) {
            return null;
        }
        return jobNameToTreeNode.get(adjacencyList.get(node.getName()).get(0));
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
