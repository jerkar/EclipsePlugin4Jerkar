package org.jerkar.eclipseplugin.commands;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jerkar.eclipseplugin.utils.ConsoleHelper;
import org.jerkar.eclipseplugin.utils.JerkarHelper;
import org.jerkar.eclipseplugin.utils.ConsoleHelper.StreamGobbler;

class JerkarExecutor {
	
	static Object run(ExecutionEvent event, String... commands) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		if (selection != null & selection instanceof IStructuredSelection) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object projectSelection = strucSelection.getFirstElement();
			if (projectSelection instanceof IJavaProject) {
				IJavaProject javaProject = (IJavaProject) projectSelection;
				IResource resource = javaProject.getResource();
				IPath path = resource.getLocation();
				File file = path.toFile();
				ProcessBuilder builder = JerkarHelper.processBuilder(commands);
				builder.directory(file);
				
				try {
					MessageConsole console = ConsoleHelper.console();
					Process process = builder.start();
					ConsoleHelper.bringToFront(console);
					final StreamGobbler outputStreamGobbler = new StreamGobbler(process.getInputStream(), 
							console.newOutputStream());
		            final StreamGobbler errorStreamGobbler = new StreamGobbler(process.getErrorStream(), 
							console.newOutputStream());
		            process.waitFor();
		            outputStreamGobbler.stop();
		            errorStreamGobbler.stop();
					javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (Exception e) {
					throw new ExecutionException(
							"Error while generating Eclipse .classpath file.",
							e);
				}
				
			}
		}
		return null;
		
	}

}