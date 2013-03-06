package gamboo;

import java.io.File;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.X509Certificate;

/**
 * A custom SecurityManager that watches implementations of
 * {@link gamboo.Gamboo#getInterface()}.
 */
@SuppressWarnings("unchecked") // Class.isAssignableFrom(Class<?>) calls are unchecked
class GambooSecurityManager extends SecurityManager {
	private final String path;

	public GambooSecurityManager(String path) {
		this.path = path;
	}

	@Override
	public void checkPermission(Permission perm) {
		if (perm.getClass().equals(java.io.FilePermission.class)) {
			Class[] context = getClassContext();
		
			// GambooClassLoader is run from evil classes, but it's trusted
			// (needed for dependency resolution)
			for (int i = 1; i < context.length; i++)
				if (context[i] == GambooClassLoader.class || context[i] == ClassLoader.class)
					return;
		}
		checkOrigin(perm.toString());
	}

	@Override
	public void checkAccept(String host, int port) {
		checkOrigin("ServerSocket.accept(\"" + host + "\", " + port + ")");
		super.checkAccept(host, port);
	}

	@Override
	public void checkAccess(Thread t) {
		checkOrigin("Thread access to " +  t.toString());
		super.checkAccess(t);
	}

	@Override
	public void checkAccess(ThreadGroup g) {
		checkOrigin("ThreadGroup access to " + g.toString());
		super.checkAccess(g);
	}

	@Override
	public void checkConnect(String host, int port) {
		checkOrigin("Socket.connect(\"" + host + "\", " + port + ")");
		super.checkConnect(host, port);
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		checkOrigin("Socket.connect(\"" + host + "\", " + port + ") in context " + context.toString());
		super.checkConnect(host, port, context);
	}

	@Override
	public void checkCreateClassLoader() {
		checkOrigin("Create ClassLoader");
		super.checkCreateClassLoader();
	}

	@Override
	public void checkDelete(String file) {
		if (new File(file).getAbsolutePath().startsWith("/tmp/"))
			return;

		checkOrigin("File.delete(\"" + file + "\")");
		super.checkDelete(file);
	}

	@Override
	public void checkExec(String cmd) {
		checkOrigin("Runtime.exec(\"" + cmd + "\")");
		super.checkExec(cmd);
	}

	@Override
	public void checkExit(int status) {
		checkOrigin("System.exit(" + status + ")");
		super.checkExit(status);
	}

	@Override
	public void checkLink(String lib) {
		return;
//		checkOrigin("Runtime.load()/loadLibrary() " + lib);
//		super.checkLink(lib);
	}

	@Override
	public void checkListen(int port) {
		checkOrigin("Listen to port " + port);
		super.checkListen(port);
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
		checkOrigin("Multicast at " + maddr.toString());
		super.checkMulticast(maddr);	
	}

	@Override
	public void checkPrintJobAccess() {
		checkOrigin("Print");
		super.checkPrintJobAccess();
	}

	@Override
	public void checkPropertiesAccess() {
		checkOrigin("System.set/getProperties()");
		super.checkPropertiesAccess();
	}

	@Override
	public void checkPropertyAccess(String key) {
		// allow access to some harmless properties
		switch (key) {
			case "line.separator":
			case "user.dir":
			case "file.separator":
			case "java.class.version":
			case "file.encoding":
			case "java.compiler":
			case "user.timezone":
			case "user.country":
			return;
		}
		checkOrigin("System.getProperty(" + key + ")");
		super.checkPropertyAccess(key);
	}

	@Override
	public void checkRead(FileDescriptor fd) {
		checkOrigin("Write from FileDescriptor " + fd.toString());
		super.checkRead(fd);
	}

	@Override
	public void checkRead(String filename) {
		filename = new File(filename).getAbsolutePath();
		
		if (filename.substring(0, filename.lastIndexOf("/") + 1).equals("/tmp/"))
			return;

		checkOrigin("Read from file \"" + filename + "\""); 
		super.checkRead(filename);
	}

	@Override
	public void checkRead(String file, Object context) {
		super.checkRead(file, context);
	}

	@Override
	public void checkSetFactory() {
		checkOrigin("Set Factory");
		throw new SecurityException("Setting Factories is forbidden.");
	}

	@Override
	public void checkSystemClipboardAccess() {
		checkOrigin("SystemClipboard access");
		throw new SecurityException("Accessing the clipboard is forbidden.");
	}

	@Override
	public void checkWrite(FileDescriptor fd) {
		checkOrigin("Write to FileDescriptor " + fd.toString());
		super.checkWrite(fd);
	}

	@Override
	public void checkWrite(String filename) {
		filename = new File(filename).getAbsolutePath();
	
		if (filename.substring(0, filename.lastIndexOf("/") + 1).equals("/tmp/"))
			return;

		checkOrigin("Write to file \"" + filename + "\"");
		super.checkWrite(filename);
	}

	@SuppressWarnings("deprecation")
	private void checkOrigin(final String type) throws SecurityException {
		Class[] context = getClassContext();
		for (int i = 2; i < context.length; i++) {
			if (context[i] == this.getClass() || context[i] == ClassLoader.class)
				return;

			if (Gamboo.getInterface().isAssignableFrom(context[i])) {
				CodeSource cs = context[i].getProtectionDomain().getCodeSource();
				System.out.print("SECURITY: " + type + " from source " + cs.getLocation());
				
				if (cs.getCertificates() != null)
					System.out.print(" issued by {" + ((X509Certificate)cs.getCertificates()[0]).getIssuerDN() + "}");

				System.out.println();

				final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
				for (int j = 0; j < trace.length; j++) System.out.println("\tat " + trace[j]);
				
				//Thread.currentThread().stop();
				throw new SecurityException(type);
			}
		}
	}
}
