package com.jackshenorion.cfgplugin.model;

public enum CfgJobClass {
    BatchFIFO("BatchFIFO"),
    BatchConcurrent("BatchConcurrent"),
    Converter("Converter"),
    WaitOnFile("WaitOnFile"),
    WaitOnJob("WaitOnJob"),
    FileCopy("FileCopy"),
    Gzip("Gzip"),
    JavaRunner("JavaRunner"),
    ReportGenerator("ReportGenerator"),
    RunOnFile("RunOnFile"),
    MplOpZip("MplOpZip"),
    Unknown("Unknown"),
    Root("Root");

    private String name;

    CfgJobClass(String name) {
        this.name = name;
    }

    public static CfgJobClass fromName(String s) {
        for (CfgJobClass cfgJobClass : CfgJobClass.values()) {
            if (cfgJobClass.getName().equals(s)) {
                return cfgJobClass;
            }
        }
        return Unknown;
    }

    public String getName() {
        return name;
    }
}
