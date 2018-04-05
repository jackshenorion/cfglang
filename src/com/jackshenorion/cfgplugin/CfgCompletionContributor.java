package com.jackshenorion.cfgplugin;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jackshenorion.cfgplugin.psi.CfgSegment;
import com.jackshenorion.cfgplugin.psi.CfgTypes;
import com.jackshenorion.cfgplugin.psi.impl.CfgPropertyImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.codeInsight.completion.CompletionUtil.DUMMY_IDENTIFIER;

public class CfgCompletionContributor extends CompletionContributor {
    public CfgCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(CfgTypes.VALUE).withLanguage(CfgLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        PsiElement element = parameters.getPosition();
                        PsiElement parent = element.getParent();
                        if (parent != null && parent instanceof CfgPropertyImpl) {
                            CfgPropertyImpl property = (CfgPropertyImpl) parent;
                            String key = property.getKey();
                            String value = element.getText();
                            if (key != null && key.equals("job") && value != null) {
                                int endPosition = value.indexOf(DUMMY_IDENTIFIER);
                                String inputString = endPosition >= 0 ? value.substring(0, endPosition) : value;
                                Project project = element.getProject();
                                List<CfgSegment> segments = CfgUtil.findSegments(project);
                                for (CfgSegment segment : segments) {
                                    if (segment.getName().startsWith(inputString)) {
                                        resultSet.addElement(LookupElementBuilder.create(segment.getName()));
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }
}