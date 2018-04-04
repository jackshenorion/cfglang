package com.jackshenorion.cfgplugin;

import com.intellij.lang.annotation.*;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.jackshenorion.cfgplugin.psi.CfgSegment;
import com.jackshenorion.cfgplugin.psi.impl.CfgPropertyImpl;
import com.jackshenorion.cfgplugin.psi.impl.CfgSegmentImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CfgAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof CfgPropertyImpl) {
            CfgPropertyImpl property = (CfgPropertyImpl) element;
            String key = property.getKey();
            String value = property.getValue();
            if (key != null && key.equals("job")) {
                Project project = element.getProject();
                List<CfgSegment> segments = CfgUtil.findSegments(project, value);
                if (segments.size() == 1) {
                    TextRange range = new TextRange(element.getTextRange().getStartOffset() + 3,
                            element.getTextRange().getStartOffset() + 3);
                    holder.createInfoAnnotation(range, null);
                } else if (segments.size() == 0) {
                    TextRange range = new TextRange(element.getTextRange().getStartOffset() + 4,
                            element.getTextRange().getEndOffset());
                    holder.createErrorAnnotation(range, "Undefined job");
                } else {
                    TextRange range = new TextRange(element.getTextRange().getStartOffset() + 4,
                            element.getTextRange().getEndOffset());
                    holder.createErrorAnnotation(range, "Duplicated job definition");
                }
            }
        } else if (element instanceof CfgSegmentImpl) {
            CfgSegmentImpl segment = (CfgSegmentImpl) element;
            String segmentName = segment.getName();
            if (segmentName != null && !segmentName.equals("General")) {
                Project project = element.getProject();
                List<CfgSegment> segments = CfgUtil.findSegments(project, segmentName);
                if (segments.size() > 1) {
                    TextRange range = new TextRange(element.getTextRange().getStartOffset(),
                            element.getTextRange().getEndOffset());
                    holder.createErrorAnnotation(range, "Duplicated job definition");
                }
            }
        }
    }
}