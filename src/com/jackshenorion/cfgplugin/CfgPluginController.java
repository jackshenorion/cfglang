package com.jackshenorion.cfgplugin;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class CfgPluginController implements ProjectComponent {
	public static final String PLUGIN_ID = "com.jackshenorion.cfgplugin";

	public static final Logger LOG = Logger.getInstance("CfgPluginController");

	public static final String PREVIEW_WINDOW_ID = "Cfg Preview";
	public static final String CONSOLE_WINDOW_ID = "Tool Output";

	public boolean projectIsClosed = false;

	public Project project;
	public ConsoleView console;
	public ToolWindow consoleWindow;

	public ToolWindow previewWindow;	// same for all grammar editor
	public JLabel previewPanel;	// same for all grammar editor


	public CfgPluginController(Project project) {
		this.project = project;
	}

	public static CfgPluginController getInstance(Project project) {
		if ( project==null ) {
			LOG.error("getInstance: project is null");
			return null;
		}
		CfgPluginController pc = project.getComponent(CfgPluginController.class);
		if ( pc==null ) {
			LOG.error("getInstance: getComponent() for "+project.getName()+" returns null");
		}
		return pc;
	}

	@Override
	public void initComponent() {
	}

	@Override
	public void projectOpened() {
		IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID));
		String version = "unknown";
		if ( plugin!=null ) {
			version = plugin.getVersion();
		}
		LOG.info("Smarts Control Cfg Plugin version "+version+", Java version "+ SystemInfo.JAVA_VERSION);
		// make sure the tool windows are created early
		createToolWindows();
	}

	public void createToolWindows() {
		LOG.info("createToolWindows "+project.getName());
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

		JLabel previewPanel = new JLabel("Hello, World!");

		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		Content content = contentFactory.createContent(previewPanel, "", false);

		previewWindow = toolWindowManager.registerToolWindow(PREVIEW_WINDOW_ID, false, ToolWindowAnchor.RIGHT);
		previewWindow.getContentManager().addContent(content);
		previewWindow.setIcon(CfgIcons.FILE);

		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder consoleBuilder = factory.createBuilder(project);
		this.console = consoleBuilder.getConsole();

		JComponent consoleComponent = console.getComponent();
		content = contentFactory.createContent(consoleComponent, "", false);

		consoleWindow = toolWindowManager.registerToolWindow(CONSOLE_WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
		consoleWindow.getContentManager().addContent(content);
		consoleWindow.setIcon(CfgIcons.FILE);
	}

	@Override
	public void projectClosed() {
		LOG.info("projectClosed " + project.getName());
		projectIsClosed = true;
		console.dispose();
		previewPanel = null;
		previewWindow = null;
		consoleWindow = null;
		project = null;
	}

	@Override
	public void disposeComponent() {
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "cfgplugin.ProjectComponent";
	}

	public ConsoleView getConsole() {
		return console;
	}

	public ToolWindow getConsoleWindow() {
		return consoleWindow;
	}

	public static void showConsoleWindow(final Project project) {
		ApplicationManager.getApplication().invokeLater(
			new Runnable() {
				@Override
				public void run() {
					CfgPluginController.getInstance(project).getConsoleWindow().show(null);
				}
			}
		);
	}

	public ToolWindow getPreviewWindow() {
		return previewWindow;
	}
}
