package com.jackshenorion.cfgplugin.controller;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MyTypedHandler implements TypedActionHandler {
    @Override
    public void execute(@NotNull Editor editor, char c, @NotNull DataContext dataContext) {
        final Document document = editor.getDocument();
        Project project = editor.getProject();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                document.insertString(editor.getCaretModel().getOffset(), c + "");
                editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + 1);
                System.out.println("MyTypedHandler:" + c + " inputted");
            }
        };
        WriteCommandAction.runWriteCommandAction(project, runnable);
    }
}