package org.jerkar.eclipseplugin.commands;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jerkar.eclipseplugin.Activator;
import org.jerkar.eclipseplugin.utils.ConsoleHelper;
import org.jerkar.eclipseplugin.utils.ConsoleHelper.StreamGobbler;
import org.jerkar.eclipseplugin.utils.JerkarHelper;

class JerkarExecutor {

    static Object run(ExecutionEvent event, String... commands) throws ExecutionException {
        ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (selection != null & selection instanceof IStructuredSelection) {
            IStructuredSelection strucSelection = (IStructuredSelection) selection;
            Object projectSelection = strucSelection.getFirstElement();
            if (projectSelection instanceof IJavaProject) {
                IJavaProject javaProject = (IJavaProject) projectSelection;
                JerkarJob jerkarJob = new JerkarJob(javaProject, commands);
                jerkarJob.schedule();
            }
        }
        return null;
    }
    
    private static class JerkarJob extends Job {
        
        private final IJavaProject javaProject;
        
        private final String[] commands;

        public JerkarJob(IJavaProject javaProject, String[] commands) {
            super("Jerkar");
            this.javaProject = javaProject;
            this.commands = commands;
        }

        @Override
        protected IStatus run(IProgressMonitor arg0) {
            IResource resource = javaProject.getResource();
            IPath path = resource.getLocation();
            File file = path.toFile();
            ProcessBuilder builder = JerkarHelper.processBuilder(commands);
            builder.directory(file);
            try {
                MessageConsole console = ConsoleHelper.console();
                console.clearConsole();
                MessageConsoleStream consoleOutputStream = console.newMessageStream();
                consoleOutputStream.setActivateOnWrite(true);
                Process process = builder.start();
                final StreamGobbler outputStreamGobbler = new StreamGobbler(process.getInputStream(),
                        consoleOutputStream);
                final StreamGobbler errorStreamGobbler = new StreamGobbler(process.getErrorStream(),
                        consoleOutputStream);
                process.waitFor();
                outputStreamGobbler.stop();
                errorStreamGobbler.stop();
                consoleOutputStream.close();
                javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
                return Status.OK_STATUS;
            } catch (Exception e) {
                return new Status(Status.ERROR, Activator.PLUGIN_ID, "Error while running Jerkar",  e);
            }

        }
        
    }

}
