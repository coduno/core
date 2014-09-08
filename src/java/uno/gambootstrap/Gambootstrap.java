package gambootstrap;

import java.util.Locale;
import java.nio.charset.Charset;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.DiagnosticListener;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;


public class Gambootstrap {
	public static void main(String[] args) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(new GambootstrapDiagnosticListener(), Locale.GERMAN, Charset.forName("UTF-8"));
		
		Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(find(new File("/home/lorenz/Dropbox/gamboo/java/src/gamboo/"), new JavaFileFilter()));
		compiler.getTask(null, fileManager, new GambootstrapDiagnosticListener(), null, null, compilationUnit).call();
	}
	
	private static List<File> find(File path, FileFilter filter) {
		List<File> list = new LinkedList<>();
		find(path, filter, list);
		return list;
	}
	
	private static void find(File path, FileFilter filter, List<File> list) {
		for (File file : path.listFiles(filter))
			if (file.isDirectory()) {
				System.out.println("Recursing into " + file);
				find(file, filter, list);
			}
			else {
				System.out.println(file);
				list.add(file);
			}
	}
	
	private static class GambootstrapDiagnosticListener implements DiagnosticListener<JavaFileObject> {
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			System.err.println(diagnostic);
		}
	}
	
	private static class JavaFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return true;
			return pathname.getName().endsWith(".java");
		}
	}
}
