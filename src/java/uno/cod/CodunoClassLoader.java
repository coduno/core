package uno.cod;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ServiceLoader;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Arrays;

import java.net.JarURLConnection;
import java.net.URLConnection;
import java.io.File;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.security.Principal;

/**
 * A {@link ClassLoader} that can be used to load multiple implementations of the same class.
 */
public class CodunoClassLoader extends URLClassLoader {
	private final ClassLoader parent;

	/**
	 * Initializes a new CodunoClassLoader pointing at a specific jar file.
	 *
	 * The structure of the jar file itself must project package
	 * structure and class names:
	 *
	 * Class <code>examplepkg.FooBar</code> must be placed in
	 * <code>/examplepkg/FooBar.class</code>.
	 *
	 * Please consider letting the players sign their jar files before
	 * loading them.
	 *
	 * @param url points this to the jar file, to load the classes from.
	 */		
	public CodunoClassLoader(URL url, ClassLoader parent) {
		super(new URL[] { url }, parent);
				
		if (!url.getProtocol().equals("jar"))
			throw new IllegalArgumentException("Please specify a jar URL (protocol mismatch)");

		this.parent = parent;
	}
	
	public CodunoClassLoader() {
		super(null, null);
		
		parent = null;
	}
	
	public CodunoClassLoader(URL url) {
		this(url, ClassLoader.getSystemClassLoader());
	}

	/**
	 * Loads a class, regardless if it has already been loaded or not. Resulting
	 * duplicates are used to execute multiple implementations of the same
	 * class concurrently.
	 * 
	 * @param name Fully qualified name of the class to be loaded. Example:
	 *             <code>examplepkg.utils.FooBar</code>
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			JarURLConnection connection = (JarURLConnection)getURLs()[0].openConnection();
			JarFile file = connection.getJarFile();
			JarEntry entry = file.getJarEntry(name.replace(".", "/") + ".class");
			byte[] bytes = read(file, entry);
			return defineClass(name, bytes, 0, bytes.length, new ProtectionDomain(new CodeSource(getURLs()[0], entry.getCertificates()), new Permissions()));
		}
		catch (IOException|NullPointerException|SecurityException e) {
			return super.loadClass(name);
		}
	}
	
/*	public Class<?> loadClass(URL url) throws ClassNotFoundException {
		if (!url.getPath().endsWith(".jar"))
			throw new IllegalArgumentException("Please specify a jar URL (protocol mismatch)");
	
		try {
			addURL(url);
			JarURLConnection conn = (JarURLConnection)url.openConnection();
			JarEntry entry = conn.getJarEntry();
			byte[] bytes = read(conn.getJarFile(), entry);
		
			return defineClass(url.toString().substring(url.toString().indexOf("!")).replace("/", ".").replace(".class", ""), bytes, 0, bytes.length, new ProtectionDomain(new CodeSource(getURLs()[0], entry.getCertificates()), new Permissions()));
		}
		catch (IOException|NullPointerException e) {
			return ClassLoader.getSystemClassLoader().loadClass(url.toString().substring(url.toString().indexOf("!")).replace("/", ".").replace(".class", ""));
		}
	} */
	
	@Override
	public String toString() {
		return super.toString() + Arrays.toString(getURLs());
	}
	
	protected byte[] read(JarFile file, JarEntry entry) throws IOException {
		byte[] bytes = new byte[(int)entry.getSize()];
		InputStream stream = file.getInputStream(entry);
		stream.read(bytes);
		stream.close();
		return bytes;
	}

	/**
	 * Provides similar functionality to {@link java.util.ServiceLoader#load(Class, ClassLoader}
	 * but does not instantiate the target service, so dependencies in the
	 * constructor of the services must not be fulfilled at the time this
	 * method is called.
	 *
	 * A corresponding <code>META-INF/services/<i>service</i></code> is
	 * still needed to resolve.
	 *
	 * @param service The class/type of the service to be loaded
	 */	
/*	public Class<?> loadServiceClass(Class<?> service) throws ClassNotFoundException, IOException {
		JarURLConnection connection = (JarURLConnection)getURLs()[0].openConnection();
		JarFile file = connection.getJarFile();
		JarEntry entry = file.getJarEntry("META-INF/services/" + service.getName());
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(entry)));
		return this.loadClass(reader.readLine());
	} */
}
