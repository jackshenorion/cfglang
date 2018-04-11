package com.jackshenorion.cfgplugin.model;

import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.psi.CfgProperty;
import com.jackshenorion.cfgplugin.psi.CfgSegment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CfgJobInfo implements Comparable<CfgJobInfo> {
    private String name;
    private boolean isStandardJob = false;
    private boolean isRoot = false;

    private CfgSegment cfgSegment;
    private List<CfgProperty> cfgPropertyList = new ArrayList<>();

    public CfgJobInfo(String name, boolean standardJob) {
        this(name, standardJob, false);
    }

    public CfgJobInfo(String name, boolean standardJob, boolean isRoot) {
        this.name = name;
        this.isStandardJob = standardJob;
        this.isRoot = isRoot;
    }

    public String getName() {
        return name;
    }

    public boolean isStandardJob() {
        return isStandardJob;
    }

    public CfgSegment getCfgSegment() {
        return cfgSegment;
    }

    public List<CfgProperty> getCfgPropertyList() {
        return new ArrayList<>(cfgPropertyList);
    }

    public CfgJobInfo setCfgSegment(CfgSegment segment) {
        this.cfgSegment = segment;
        return this;
    }

    public CfgJobInfo addCfgProperty(CfgProperty property) {
        this.cfgPropertyList.add(property);
        return this;
    }

    public CfgJobClass getJobClass() {
        if (isRoot) {
            return CfgJobClass.Root;
        }
        CfgJobClass[] cfgJobClasses = {CfgJobClass.Unknown};
        cfgPropertyList.forEach(cfgProperty -> {
            if (cfgProperty.getKey().equals(CfgUtil.PROPERTY_KEY_JOBCLASS)) {
                cfgJobClasses[0] = CfgJobClass.fromName(cfgProperty.getValue());
            }
        });
        return cfgJobClasses[0];
    }

    @Override
    public int compareTo(@NotNull CfgJobInfo o) {
        return this.getName().compareTo(o.getName());
    }
}
