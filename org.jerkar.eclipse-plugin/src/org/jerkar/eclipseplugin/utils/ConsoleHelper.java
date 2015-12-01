package org.jerkar.eclipseplugin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleHelper {
	
	public static MessageConsole console() {
		return findConsole("Jerkar");
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
	
	public static void bringToFront(MessageConsole console) {
	    try{
	        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	        String id = IConsoleConstants.ID_CONSOLE_VIEW;
	        IConsoleView view = (IConsoleView) page.showView(id);
	        view.display(console);
	    } catch(PartInitException e) {
	        e.printStackTrace();
	    }
	}
	
	

	/**
	 * Runs a thread copying all data from a stream to a specified writer. The
	 * thread is started when the instance is created. You have to call
	 * {@link #stop()} to stop the thread.
	 */
	public static final class StreamGobbler {

		private final InnerRunnable innerRunnable;

		public StreamGobbler(InputStream is, OutputStream os) {
			this.innerRunnable = new InnerRunnable(is, os);
			new Thread(innerRunnable).start();
		}

		/**
		 * Stop the gobbling, meaning stop the thread.
		 */
		public StreamGobbler stop() {
			this.innerRunnable.stop.set(true);
			return this;
		}

		private static class InnerRunnable implements Runnable {

			private final InputStream in;

			private final OutputStream out;

			private final AtomicBoolean stop = new AtomicBoolean(false);

			private InnerRunnable(InputStream is, OutputStream os) {
				this.in = is;
				this.out = os;
			}

			@Override
			public void run() {
				try {
					final InputStreamReader isr = new InputStreamReader(in);
					final BufferedReader br = new BufferedReader(isr);
					String line = null;
					while (!stop.get() && (line = br.readLine()) != null) {
						final byte[] bytes = line.getBytes();
						out.write(bytes, 0, bytes.length);
						out.write('\n');
					}
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

		}

	}

}
