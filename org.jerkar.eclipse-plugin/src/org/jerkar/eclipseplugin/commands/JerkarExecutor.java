package org.jerkar.eclipseplugin.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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

public class JerkarExecutor {

    public static Object run(ExecutionEvent event, String... commands) throws ExecutionException {
        ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (selection != null & selection instanceof IStructuredSelection) {
            IStructuredSelection strucSelection = (IStructuredSelection) selection;
            Object projectSelection = strucSelection.getFirstElement();
            if (projectSelection instanceof IJavaProject) {
                IJavaProject javaProject = (IJavaProject) projectSelection;
                JerkarJob jerkarJob = new JerkarJob(javaProject.getProject(), commands, null);
                jerkarJob.schedule();
            }
        }
        return null;
    }
    
    public static Object runCmdLine(IProject project, String commandLine, Runnable after) {
        String[] items = translateCommandline(commandLine);
        JerkarJob jerkarJob = new JerkarJob(project, items, after);
        jerkarJob.schedule();
        return  null;
    }
    
    // Borrowed from ANT project
    private static String[] translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.length() == 0) {
            // no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        final ArrayList<String> result = new ArrayList<String>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            final String nextTok = tok.nextToken();
            switch (state) {
            case inQuote:
                if ("\'".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            case inDoubleQuote:
                if ("\"".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            default:
                if ("\'".equals(nextTok)) {
                    state = inQuote;
                } else if ("\"".equals(nextTok)) {
                    state = inDoubleQuote;
                } else if (" ".equals(nextTok)) {
                    if (lastTokenHasBeenQuoted || current.length() != 0) {
                        result.add(current.toString());
                        current.setLength(0);
                    }
                } else {
                    current.append(nextTok);
                }
                lastTokenHasBeenQuoted = false;
                break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new IllegalArgumentException("unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[result.size()]);
    }
    
    private static class JerkarJob extends Job {
        
        private final IProject project;
        
        private final String[] commands;

        public JerkarJob(IProject project, String[] commands, final Runnable after) {
            super("Jerkar");
            this.project = project;
            this.commands = commands;
            if (after != null) {
                this.addJobChangeListener(new JobChangeAdapter() {
                    
                    @Override
                    public void done(IJobChangeEvent event) {
                        if (event.getResult().equals(Status.OK_STATUS)) {
                            after.run();
                        }
                        
                    }
      
                });
            }
        }

        @Override
        protected IStatus run(IProgressMonitor arg0) {
            IPath path = project.getLocation();
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
                project.refreshLocal(IResource.DEPTH_INFINITE, null);
                return Status.OK_STATUS;
            } catch (Exception e) {
                return new Status(Status.ERROR, Activator.PLUGIN_ID, "Error while running Jerkar",  e);
            }

        }
        
    }

}
