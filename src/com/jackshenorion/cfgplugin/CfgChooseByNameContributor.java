package com.jackshenorion.cfgplugin;

import com.intellij.navigation.*;
import com.intellij.openapi.project.Project;
import com.jackshenorion.cfgplugin.psi.CfgSegment;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CfgChooseByNameContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        List<CfgSegment> segments = CfgUtil.findSegments(project);
        List<String> names = new ArrayList<String>(segments.size());
        for (CfgSegment segment : segments) {
            if (segment.getName() != null && segment.getName().length() > 0) {
                names.add(segment.getName());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        List<CfgSegment> segments = CfgUtil.findSegments(project, name);
        return segments.toArray(new NavigationItem[segments.size()]);
    }
}