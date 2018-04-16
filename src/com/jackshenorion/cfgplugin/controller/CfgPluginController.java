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
    public Project project;
    public ToolWindow previewWindow;
    public CfgViewerPanel previewPanel;

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
    }

    private void createActionToolBar() {
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup(ID_ACTION_GROUP, false);
        actionGroup.add(new FocusInEditorAction("Jump To Source", "Jump To Source", CfgIcons.LOCATE, this));
        actionGroup.add(new ViewerSwitchAction("Validate Job Classes", "Whether validate jobClass", CfgIcons.ERROR, this, CHECK_JOB_CLASSES_SWITCH_ID));
        actionGroup.add(new ViewerSwitchAction("Display Base Jobs", "Whether display base jobs on root", CfgIcons.BASE_JOB, this, DISPLAY_BASE_JOBS_SWITCH_ID));
        ActionToolbar toolBar = actionManager.createActionToolbar(ID_ACTION_TOOLBAR, actionGroup, true);
        JPanel panel = new JPanel(new HorizontalLayout(0));
        panel.add(toolBar.getComponent());
        previewPanel.add(panel, BorderLayout.NORTH);
    }

    public void onFocusInEditorClicked() {
        previewPanel.onFocusInEditSourceFromTreeSelection();
    }

    private static final String CHECK_JOB_CLASSES_SWITCH_ID = "Check Job Classes";
    private static final String DISPLAY_BASE_JOBS_SWITCH_ID = "Display Base Jobs";
    private boolean checkJobClasses = false;
    private boolean displayBaseJobs = false;

    public void onViewerSwitch(String switchName, boolean isSelected) {
        switch (switchName) {
            case CHECK_JOB_CLASSES_SWITCH_ID:
                checkJobClasses = isSelected;
                break;
            case DISPLAY_BASE_JOBS_SWITCH_ID:
                displayBaseJobs = isSelected;
                break;
        }
        previewPanel.forceRefreshTree();
    }

    public boolean getViewSwitch(String switchName) {
        switch (switchName) {
            case CHECK_JOB_CLASSES_SWITCH_ID:
                return checkJobClasses;
            case DISPLAY_BASE_JOBS_SWITCH_ID:
                return displayBaseJobs;
            default:
                return false;
        }
    }

    public boolean isCheckJobClasses() {
        return checkJobClasses;
    }

    public boolean isDisplayBaseJobs() {
        return displayBaseJobs;
    }

    public void onCurrentPackageChanged(String packageName) {
        getToolWindow().setTitle(packageName);
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
