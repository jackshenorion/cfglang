package com.jackshenorion.cfgplugin;


import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.Map;

public class CfgColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Key", CfgSyntaxHighlighter.KEY),
            new AttributesDescriptor("Separator", CfgSyntaxHighlighter.SEPARATOR),
            new AttributesDescriptor("Value", CfgSyntaxHighlighter.VALUE),
            new AttributesDescriptor("Segment", CfgSyntaxHighlighter.SEGMENT_NAME),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return CfgIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new CfgSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "#\n" +
                "# SmartsControl.cfg\n" +
                "#\n" +
                "# Configuration file for SmartsControl.pl.\n" +
                "#\n" +
                "# The following parameters are available for dynamic substitution:\n" +
                "# %HOSTNAME%    = the current machine hostname.\n" +
                "# %SMARTS*_     = Path to various SMARTS directories, see SMARTS::Util::Environment.pm documentation for details.\n" +
                "# %MARKET%      = Name of market.\n" +
                "# %YYYY%        = 4 digit year.\n" +
                "# %MM%          = 2 digit month.\n" +
                "# %DD%          = 2 digit day.\n" +
                "# %YYYYMMDD%    = 8 digit year, month and day.\n" +
                "\n" +
                "# General Configuration\n" +
                "[General]\n" +
                "\n" +
                "[LOPBatchJob]\n" +
                "jobClass=BatchFIFO\n" +
                "job=WaitForLOPFilesBatch\n" +
                "job=MoveLOPFilesFromInputToRawBatch\n" +
                "job=LOPSqliteConverterBatch\n" +
                "job=RunLOPReport\n" +
                "\n" +
                "[DcassListenerBatchJob]\n" +
                "jobClass=BatchFIFO\n" +
                "job=OCQListenerJob\n" +
                "job=GzipOCQ\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Cfg";
    }
}