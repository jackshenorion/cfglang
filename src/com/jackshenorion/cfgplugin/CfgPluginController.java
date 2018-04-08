package com.jackshenorion.cfgplugin;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
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
import com.jackshenorion.cfgplugin.controller.MyTypedHandler;
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

    public MyVirtualFileAdapter myVirtualFileAdapter = new MyVirtualFileAdapter();
    public MyFileEditorManagerAdapter myFileEditorManagerAdapter = new MyFileEditorManagerAdapter();
    public static final Key<GrammarEditorMouseAdapter> EDITOR_MOUSE_LISTENER_KEY = Key.create("EDITOR_MOUSE_LISTENER_KEY");

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
        installListeners();
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
        uninstallListeners();
		projectIsClosed = true;
		console.dispose();
		previewPanel = null;
		previewWindow = null;
		consoleWindow = null;
		project = null;
	}

    public void installListeners() {
        LOG.info("installListeners "+project.getName());
        // Listen for .g4 file saves
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
                        if ( vfile!=null && vfile.getName().endsWith(".cfg") ) {
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

    private class MyVirtualFileAdapter implements VirtualFileListener {
        @Override
        public void contentsChanged(VirtualFileEvent event) {
            onFileEvent(event.getFile().getName() + " contentsChanged");
//            final VirtualFile vfile = event.getFile();
//            if ( !vfile.getName().endsWith(".cfg") ) return;
//            if ( !projectIsClosed ) onFileEvent(vfile.getName() + " contentsChanged");
        }

        @Override
        public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
            onFileEvent(event.getFile().getName() + " propertyChanged");
        }

        @Override
        public void fileCreated(@NotNull VirtualFileEvent event) {
            onFileEvent(event.getFile().getName() + " fileCreated");
        }

        @Override
        public void fileDeleted(@NotNull VirtualFileEvent event) {
            onFileEvent(event.getFile().getName() + " fileDeleted");
        }

        @Override
        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            onFileEvent(event.getFile().getName() + " fileMoved");
        }

        @Override
        public void fileCopied(@NotNull VirtualFileCopyEvent event) {
            onFileEvent(event.getFile().getName() + " fileCopied");
        }

        @Override
        public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {
            onFileEvent(event.getFile().getName() + " beforePropertyChange");
        }

        @Override
        public void beforeContentsChange(@NotNull VirtualFileEvent event) {
            onFileEvent(event.getFile().getName() + " beforeContentsChange");
        }

        @Override
        public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
            onFileEvent(event.getFile().getName() + " beforeFileDeletion");
        }

        @Override
        public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
            onFileEvent(event.getFile().getName() + " beforeFileMovement");
        }
    }

    private void onFileEvent(String display) {
	    System.out.println(display);
    }

    private class MyFileEditorManagerAdapter implements FileEditorManagerListener {
        @Override
        public void selectionChanged(FileEditorManagerEvent event) {
            onFileEvent("selectionChanged: " + event);
        }

        @Override
        public void fileClosed(FileEditorManager source, VirtualFile file) {
            onFileEvent("fileClosed: " + file.getName());
        }

        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            onFileEvent("fileOpened: " + file.getName());
        }
    }

    private class GrammarEditorMouseAdapter extends EditorMouseAdapter {
        @Override
        public void mouseClicked(EditorMouseEvent e) {
            Document doc = e.getEditor().getDocument();
            VirtualFile vfile = FileDocumentManager.getInstance().getFile(doc);
            if ( vfile!=null ) {
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(doc);
                e.getEditor().getCaretModel().getOffset();
                PsiElement element = psiFile.findElementAt(e.getEditor().getCaretModel().getOffset());
                System.out.println("PSI element selected:" + element.getText());
                mouseEnteredGrammarEditorEvent(vfile, e);
            }
        }
    }

    public void mouseEnteredGrammarEditorEvent(VirtualFile vfile, EditorMouseEvent e) {
        onFileEvent("mouseEnteredGrammarEditorEvent:" + vfile.getName() + ";" + e);
    }


}
