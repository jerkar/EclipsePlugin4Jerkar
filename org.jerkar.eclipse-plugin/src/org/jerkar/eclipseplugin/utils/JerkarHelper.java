package org.jerkar.eclipseplugin.utils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JerkarHelper {
	
	public static final boolean IS_WINDOWS = isWindows();
	
	public static ProcessBuilder processBuilder(String ...commands) {
		List<String> cmd = new LinkedList<>();
		cmd.add(cmd());
		cmd.addAll(Arrays.asList(commands));
		ProcessBuilder builder = new ProcessBuilder(cmd);
		return builder;
	}
	
	public static final File jerkarHome() {
		String path = System.getenv("PATH");
		String cmd = cmd();
		for (String item : path.split(File.pathSeparator)) {
			File file = new File(item, cmd);
			if (file.exists()) {
				return file.getParentFile();
			}
		}
		return null;
	}
	
	private static final boolean isWindows() {
        final String osName = System.getProperty("os.name");
        if (osName == null) {
            return false;
        }
        return osName.startsWith("Windows");
    }
	
	private static String cmd() {
		return IS_WINDOWS ? "jerkar.bat" : "jerkar";
	}

}
