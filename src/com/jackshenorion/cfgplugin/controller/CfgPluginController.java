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
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class CfgPluginController implements ProjectComponent {
    public static final Logger LOG = Logger.getInstance("CfgPluginController");
    public static final String PLUGIN_ID = "com.jackshenorion.cfgplugin";
    public static final String PREVIEW_WINDOW_ID = "Cfg Tree View";

    public boolean projectIsClosed = false;
    public boolean autoScrollToSource = true;
    public boolean autoScrollFromSource = true;

    public Project project;
    public ToolWindow previewWindow;
    public CfgViewerPanel previewPanel;

    public MyVirtualFileAdapter myVirtualFileAdapter = new MyVirtualFileAdapter();
    public MyFileEditorManagerAdapter myFileEditorManagerAdapter = new MyFileEditorManagerAdapter();
    public static final Key<GrammarEditorMouseAdapter> EDITOR_MOUSE_LISTENER_KEY = Key.create("EDITOR_MOUSE_LISTENER_KEY");
    private EditorListener _editorListener;

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
        // make sure the tool windows are created early
        createToolWindows();
        installListeners();
    }

    public void createToolWindows() {
        LOG.info("createToolWindows " + project.getName());
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        previewPanel = new CfgViewerPanel(this);

        previewPanel.addPropertyChangeListener("ancestor", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                handleCurrentState();
            }
        });


        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(previewPanel, "", false);

        previewWindow = toolWindowManager.registerToolWindow(PREVIEW_WINDOW_ID, false, ToolWindowAnchor.RIGHT);
        previewWindow.getContentManager().addContent(content);
        previewWindow.setIcon(CfgIcons.FILE);
        _editorListener = new EditorListener(previewPanel, project);
    }

    @Override
    public void projectClosed() {
        LOG.info("projectClosed " + project.getName());
        uninstallListeners();
        projectIsClosed = true;
        previewPanel = null;
        previewWindow = null;
        project = null;
        if (_editorListener != null) {
            _editorListener.stop();
            _editorListener = null;
        }

    }

    private void handleCurrentState() {
        if (previewPanel == null)
            return;

        if (previewPanel.isDisplayable()) {
            _editorListener.start();
            previewPanel.selectElementAtCaret();
        } else {
            _editorListener.stop();
        }
    }


    public void installListeners() {
        LOG.info("installListeners " + project.getName());
        VirtualFileManager.getInstance().addVirtualFileListener(myVirtualFileAdapter);

//        final EditorActionManager actionManager = EditorActionManager.getInstance();
//        final TypedAction typedAction = actionManager.getTypedAction();
//        typedAction.setupHandler(new MyTypedHandler());

        // Listen for editor window changes
        MessageBusConnection msgBus = project.getMessageBus().connect(project);
        msgBus.subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                myFileEditorManagerAdapter
        );

        EditorFactory factory = EditorFactory.getInstance();
        factory.addEditorFactoryListener(
                new EditorFactoryAdapter() {
                    @Override
                    public void editorCreated(@NotNull EditorFactoryEvent event) {
                        final Editor editor = event.getEditor();
                        final Document doc = editor.getDocument();
                        VirtualFile vfile = FileDocumentManager.getInstance().getFile(doc);
                        if (vfile != null && vfile.getName().endsWith(".cfg")) {
                            GrammarEditorMouseAdapter listener = new GrammarEditorMouseAdapter();
                            editor.putUserData(EDITOR_MOUSE_LISTENER_KEY, listener);
                            editor.addEditorMouseListener(listener);
                        }
                    }

                    @Override
                    public void editorReleased(@NotNull EditorFactoryEvent event) {
                        Editor editor = event.getEditor();
                        if (editor.getProject() != null && editor.getProject() != project) {
                            return;
                        }
                        GrammarEditorMouseAdapter listener = editor.getUserData(EDITOR_MOUSE_LISTENER_KEY);
                        if (listener != null) {
                            editor.removeEditorMouseListener(listener);
                            editor.putUserData(EDITOR_MOUSE_LISTENER_KEY, null);
                        }
                    }
                }
        );

    }

    public void uninstallListeners() {
        VirtualFileManager.getInstance().removeVirtualFileListener(myVirtualFileAdapter);
        MessageBusConnection msgBus = project.getMessageBus().connect(project);
        msgBus.disconnect();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "cfgplugin.ProjectComponent";
    }

    public ToolWindow getPreviewWindow() {
        return previewWindow;
    }

    private class MyVirtualFileAdapter implements VirtualFileListener {
        @Override
        public void contentsChanged(VirtualFileEvent event) {
            if (CfgUtil.isCfgVirtualFile(event.getFile()) && !projectIsClosed) {
                previewPanel.resetTree(event.getFile());
            }
        }

        @Override
        public void fileCreated(@NotNull VirtualFileEvent event) {
            // refresh the tree of the package
        }

        @Override
        public void fileDeleted(@NotNull VirtualFileEvent event) {
            // refresh the tree of the package
        }

        @Override
        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            // refresh the tree of the packages involved
        }

        @Override
        public void fileCopied(@NotNull VirtualFileCopyEvent event) {
            // refresh the tree of the packages involved
        }
    }

    private class MyFileEditorManagerAdapter implements FileEditorManagerListener {
        @Override
        public void selectionChanged(FileEditorManagerEvent event) {
            if (CfgUtil.isCfgVirtualFile(event.getNewFile()) && !projectIsClosed) {
                previewPanel.resetTree(event.getNewFile());
            }
        }

        @Override
        public void fileClosed(FileEditorManager source, VirtualFile file) {
        }

        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (CfgUtil.isCfgVirtualFile(file) && !projectIsClosed) {
                previewPanel.resetTree(file);
            }
        }
    }

    private class GrammarEditorMouseAdapter extends EditorMouseAdapter {
        @Override
        public void mouseClicked(EditorMouseEvent e) {
            Document doc = e.getEditor().getDocument();
            VirtualFile vfile = FileDocumentManager.getInstance().getFile(doc);
            if (vfile != null) {
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(doc);
                e.getEditor().getCaretModel().getOffset();
                PsiElement element = psiFile.findElementAt(e.getEditor().getCaretModel().getOffset());
            }

            // if mouse clicked, then go to the segment where the mouse clicked
        }
    }

}
