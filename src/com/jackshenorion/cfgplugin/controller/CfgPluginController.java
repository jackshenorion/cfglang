package com.jackshenorion.cfgplugin.controller;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jackshenorion.cfgplugin.CfgIcons;
import com.jackshenorion.cfgplugin.view.CfgViewerPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.*;

import static com.jackshenorion.cfgplugin.CfgConstants.*;


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
        installListeners();
    }

    @Override
    public void projectClosed() {
        LOG.info("projectClosed " + project.getName());
        uninstallListeners();
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
        createActionToolBar();
        editorListener = new EditorListener(previewPanel, project);
        //todo show name of current file and its package

    }

    private void createActionToolBar() {
        // whether include base file
        // whether auto-open file
        // focus on selected item
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup(ID_ACTION_GROUP, false);
        actionGroup.add(new FocusInEditorAction("Jump To Source", "Jump To Source", CfgIcons.LOCATE, this));
//        actionGroup.add(new PropertyToggleAction("Filter Whitespace",
//                "Remove whitespace elements",
//                Helpers.getIcon(ICON_FILTER_WHITESPACE),
//                this,
//                "filterWhitespace"));
//        actionGroup.add(new PropertyToggleAction("Highlight",
//                "Highlight selected PSI element",
//                Helpers.getIcon(ICON_TOGGLE_HIGHLIGHT),
//                this,
//                "highlighted"));
//        actionGroup.add(new PropertyToggleAction("Properties",
//                "Show PSI element properties",
//                AllIcons.General.Settings,
//                this,
//                "showProperties"));
//        actionGroup.add(new PropertyToggleAction("Autoscroll to Source",
//                "Autoscroll to Source",
//                AllIcons.General.AutoscrollToSource,
//                this,
//                "autoScrollToSource"));
//        actionGroup.add(new PropertyToggleAction("Autoscroll from Source",
//                "Autoscroll from Source111",
//                AllIcons.General.AutoscrollFromSource,
//                this,
//                "autoScrollFromSource"));

        ActionToolbar toolBar = actionManager.createActionToolbar(ID_ACTION_TOOLBAR, actionGroup, true);
        JPanel panel = new JPanel(new HorizontalLayout(0));
        panel.add(toolBar.getComponent());
        previewPanel.add(panel, BorderLayout.NORTH);
    }

    public void onFocusInEditorClicked() {
        previewPanel.onEditSource();
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

    public void installListeners() {
    }

    public void uninstallListeners() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return PLUGIN_NAME + '.' + PROJECT_COMPONENT_NAME;
    }
}
