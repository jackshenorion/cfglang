package com.jackshenorion.cfgplugin.controller;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryAdapter;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBusConnection;
import com.jackshenorion.cfgplugin.CfgIcons;
import com.jackshenorion.cfgplugin.CfgUtil;
import com.jackshenorion.cfgplugin.view.CfgViewerPanel;
import org.jetbrains.annotations.NotNull;

import static com.jackshenorion.cfgplugin.CfgConstants.PLUGIN_NAME;
import static com.jackshenorion.cfgplugin.CfgConstants.PROJECT_COMPONENT_NAME;


public class CfgPluginController implements ProjectComponent {
    public static final Logger LOG = Logger.getInstance("CfgPluginController");
    public static final String PLUGIN_ID = "com.jackshenorion.cfgplugin";
    public static final String PREVIEW_WINDOW_ID = "Smarts Jobs Viewer";

    public boolean projectIsClosed = false;
    public boolean autoScrollToSource = true;
    public boolean autoScrollFromSource = true;

    public Project project;
    public ToolWindow previewWindow;
    public CfgViewerPanel previewPanel;

//    public MyVirtualFileAdapter myVirtualFileAdapter = new MyVirtualFileAdapter();
//    public MyFileEditorManagerAdapter myFileEditorManagerAdapter = new MyFileEditorManagerAdapter();
//    public static final Key<GrammarEditorMouseAdapter> EDITOR_MOUSE_LISTENER_KEY = Key.create("EDITOR_MOUSE_LISTENER_KEY");
    private EditorListener editorListener;

    public CfgPluginController(Project project) {
        this.project = project;
    }

    public static CfgPluginController getInstance(Project project) {
        if (project == null) {
            LOG.error("getInstance: project is null");
            return null;
        }
        CfgPluginController pc = project.getComponent(CfgPluginController.class);
        if (pc == null) {
            LOG.error("getInstance: getComponent() for " + project.getName() + " returns null");
        }
        return pc;
    }

    public Project getProject() {
        return project;
    }

    public boolean isAutoScrollToSource() {
        return autoScrollToSource;
    }

    public boolean isAutoScrollFromSource() {
        return autoScrollFromSource;
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void projectOpened() {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID));
        String version = "unknown";
        if (plugin != null) {
            version = plugin.getVersion();
        }
        LOG.info("Smarts Control Cfg Plugin version " + version + ", Java version " + SystemInfo.JAVA_VERSION);
        createToolWindows();
//        installListeners();
    }

    @Override
    public void projectClosed() {
        LOG.info("projectClosed " + project.getName());
//        uninstallListeners();
        if (editorListener != null) {
            editorListener.stop();
            editorListener = null;
        }
        if (isToolWindowRegistered()) {
            ToolWindowManager.getInstance(project).unregisterToolWindow(PREVIEW_WINDOW_ID);
        }
        projectIsClosed = true;
        previewPanel = null;
        previewWindow = null;
        project = null;
    }

    public void createToolWindows() {
        LOG.info("createToolWindows " + project.getName());
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        previewPanel = new CfgViewerPanel(this);
        previewPanel.addPropertyChangeListener("ancestor", evt -> handleCurrentState());
        Content content = contentFactory.createContent(previewPanel, "", false);
        previewWindow = getToolWindow();
        previewWindow.getContentManager().addContent(content);
        previewWindow.setIcon(CfgIcons.FILE);
        editorListener = new EditorListener(previewPanel, project);
    }

    private ToolWindow getToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        return isToolWindowRegistered() ?
                toolWindowManager.getToolWindow(PREVIEW_WINDOW_ID) :
                toolWindowManager.registerToolWindow(PREVIEW_WINDOW_ID, false, ToolWindowAnchor.RIGHT);
    }

    private boolean isToolWindowRegistered() {
        return ToolWindowManager.getInstance(project).getToolWindow(PREVIEW_WINDOW_ID) != null;
    }

    private void handleCurrentState() {
        if (previewPanel == null)
            return;

        if (previewPanel.isDisplayable()) {
            editorListener.start();
            previewPanel.selectElementAtCaret();
        } else {
            editorListener.stop();
        }
    }

//    public void installListeners() {
//        LOG.info("installListeners " + project.getName());
//        VirtualFileManager.getInstance().addVirtualFileListener(myVirtualFileAdapter);
//
//        MessageBusConnection msgBus = project.getMessageBus().connect(project);
//        msgBus.subscribe(
//                FileEditorManagerListener.FILE_EDITOR_MANAGER,
//                myFileEditorManagerAdapter
//        );
//
//        EditorFactory factory = EditorFactory.getInstance();
//        factory.addEditorFactoryListener(
//                new EditorFactoryAdapter() {
//                    @Override
//                    public void editorCreated(@NotNull EditorFactoryEvent event) {
//                        final Editor editor = event.getEditor();
//                        final Document doc = editor.getDocument();
//                        VirtualFile vfile = FileDocumentManager.getInstance().getFile(doc);
//                        if (vfile != null && vfile.getName().endsWith(".cfg")) {
//                            GrammarEditorMouseAdapter listener = new GrammarEditorMouseAdapter();
//                            editor.putUserData(EDITOR_MOUSE_LISTENER_KEY, listener);
//                            editor.addEditorMouseListener(listener);
//                        }
//                    }
//
//                    @Override
//                    public void editorReleased(@NotNull EditorFactoryEvent event) {
//                        Editor editor = event.getEditor();
//                        if (editor.getProject() != null && editor.getProject() != project) {
//                            return;
//                        }
//                        GrammarEditorMouseAdapter listener = editor.getUserData(EDITOR_MOUSE_LISTENER_KEY);
//                        if (listener != null) {
//                            editor.removeEditorMouseListener(listener);
//                            editor.putUserData(EDITOR_MOUSE_LISTENER_KEY, null);
//                        }
//                    }
//                }
//        );
//
//    }

//    public void uninstallListeners() {
//        VirtualFileManager.getInstance().removeVirtualFileListener(myVirtualFileAdapter);
//        MessageBusConnection msgBus = project.getMessageBus().connect(project);
//        msgBus.disconnect();
//    }

    @NotNull
    @Override
    public String getComponentName() {
        return PLUGIN_NAME + '.' + PROJECT_COMPONENT_NAME;
    }

//    private class MyVirtualFileAdapter implements VirtualFileListener {
//        @Override
//        public void contentsChanged(VirtualFileEvent event) {
//            if (CfgUtil.isCfgVirtualFile(event.getFile()) && !projectIsClosed) {
//                previewPanel.setCurrentFile(event.getFile());
//            }
//        }
//
//        @Override
//        public void fileCreated(@NotNull VirtualFileEvent event) {
//            // refresh the tree of the package
//        }
//
//        @Override
//        public void fileDeleted(@NotNull VirtualFileEvent event) {
//            // refresh the tree of the package
//        }
//
//        @Override
//        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
//            // refresh the tree of the packages involved
//        }
//
//        @Override
//        public void fileCopied(@NotNull VirtualFileCopyEvent event) {
//            // refresh the tree of the packages involved
//        }
//    }

//    private class MyFileEditorManagerAdapter implements FileEditorManagerListener {
//        @Override
//        public void selectionChanged(FileEditorManagerEvent event) {
//            if (CfgUtil.isCfgVirtualFile(event.getNewFile()) && !projectIsClosed) {
//                previewPanel.setCurrentFile(event.getNewFile());
//            }
//        }
//
//        @Override
//        public void fileClosed(FileEditorManager source, VirtualFile file) {
//        }
//
//        @Override
//        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//            if (CfgUtil.isCfgVirtualFile(file) && !projectIsClosed) {
//                previewPanel.setCurrentFile(file);
//            }
//        }
//    }

//    private class GrammarEditorMouseAdapter extends EditorMouseAdapter {
//        @Override
//        public void mouseClicked(EditorMouseEvent e) {
//            Document doc = e.getEditor().getDocument();
//            VirtualFile vfile = FileDocumentManager.getInstance().getFile(doc);
//            if (vfile != null) {
//                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(doc);
//                e.getEditor().getCaretModel().getOffset();
//                PsiElement element = psiFile.findElementAt(e.getEditor().getCaretModel().getOffset());
//            }
//
//            // if mouse clicked, then go to the segment where the mouse clicked
//        }
//    }

}
