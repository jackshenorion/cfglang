package com.jackshenorion.cfgplugin.model;

import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.psi.CfgProperty;
import com.jackshenorion.cfgplugin.psi.CfgSegment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CfgViewerTreeNode implements Comparable<CfgViewerTreeNode> {
    private String name;
    private boolean isBaseJob = false;
    private boolean isRoot = false;
    private boolean undefined = false;
    private boolean isDuplicate = false;
    private boolean isOnErrorPath = false;
    private boolean isJobClassUndefined = false;

    private CfgSegment cfgSegment;
    private List<CfgProperty> cfgPropertyList = new ArrayList<>();

    public CfgViewerTreeNode(String name, boolean isBaseJob, boolean isRoot, boolean undefined) {
        this(name, isBaseJob, isRoot);
        this.undefined = undefined;
    }

    public CfgViewerTreeNode(String name, boolean baseJob) {
        this(name, baseJob, false);
    }

    public CfgViewerTreeNode(String name, boolean standardJob, boolean isRoot) {
        this.name = name;
        this.isBaseJob = standardJob;
        this.isRoot = isRoot;
    }

    public String getName() {
        return name;
    }

    public boolean isBaseJob() {
        return isBaseJob;
    }

    public boolean isUndefined() {
        return undefined;
    }

    public CfgSegment getCfgSegment() {
        return cfgSegment;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public CfgViewerTreeNode setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
        return this;
    }

    public boolean isOnErrorPath() {
        return isOnErrorPath;
    }

    public void setOnErrorPath(boolean onErrorPath) {
        isOnErrorPath = onErrorPath;
    }

    public boolean isJobClassUndefined() {
        return isJobClassUndefined;
    }

    public CfgViewerTreeNode setJobClassUndefined(boolean jobClassUndefined) {
        isJobClassUndefined = jobClassUndefined;
        return this;
    }

    public List<CfgProperty> getCfgPropertyList() {
        return new ArrayList<>(cfgPropertyList);
    }

    public CfgViewerTreeNode setCfgSegment(CfgSegment segment) {
        this.cfgSegment = segment;
        return this;
    }

    public CfgViewerTreeNode addCfgProperty(CfgProperty property) {
        this.cfgPropertyList.add(property);
        return this;
    }

    public String getJobClass() {
        if (isRoot) {
            return "Root";
        }
        for (CfgProperty cfgProperty : cfgPropertyList) {
            if (cfgProperty.getKey().equals(CfgUtil.PROPERTY_KEY_JOB_CLASS)) {
                return cfgProperty.getValue();
            }
        }
        return "Unknown";
    }

    @Override
    public int compareTo(@NotNull CfgViewerTreeNode o) {
        return this.getName().compareTo(o.getName());
    }
}