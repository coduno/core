/*
 * The MIT License
 *
 * Copyright (c) 2009-, Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package gamboo.annotation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import static javax.lang.model.type.TypeKind.NONE;
import static javax.lang.model.type.TypeKind.VOID;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import java.io.File;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.DiagnosticListener;
import javax.tools.DiagnosticCollector;
import javax.tools.StandardJavaFileManager;

/**
 * Processes all classes annotated with {@link gamboo.annotation.Service} and
 * generates corresponding files (<tt>META-INF/services/<i>...</i></tt>) at
 * compile time.
 * 
 * @author Kohsuke Kawaguchi, Lorenz Leutgeb
 */
@Service
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ServiceProcessor extends AbstractProcessor {
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(Service.class.getName());
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver())
			return false;
		
		Map<String, TreeMap<Integer, String>> services = new HashMap<>();
		TreeMap<Integer, String> map = null;

		Elements elements = processingEnv.getElementUtils();
		Service service;
		
		for (Element item : roundEnv.getElementsAnnotatedWith(Service.class)) {
			service = item.getAnnotation(Service.class);
			
			if (service == null || (!item.getKind().isClass() && !item.getKind().isInterface()))
				continue;
			
			TypeElement type = (TypeElement)item;
			TypeElement contract = getContract(type, service);
			
			if (contract == null)
				continue;

			String contractName = elements.getBinaryName(contract).toString();

			if ((map = services.get(contractName)) == null) {
				map = new TreeMap<>();
				map.put(service.priority(), elements.getBinaryName(type).toString());
				services.put(contractName, map);
			}
			else {
				map.put(service.priority(), elements.getBinaryName(type).toString());
			}
		}

		Filer filer = processingEnv.getFiler();
		int count = 0;
		BufferedReader reader = null;
		String line;
		
		for (Map.Entry<String, TreeMap<Integer, String>> entry : services.entrySet()) {
			try {
				reader = new BufferedReader(new InputStreamReader(filer.getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + entry.getKey()).openInputStream(), "UTF-8"));
				
				while((line = reader.readLine()) != null)
					entry.getValue().put(Integer.MIN_VALUE + count++, line);
				
				
			} catch (FileNotFoundException e) {
				// doesn't exist
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(ERROR, "Failed to load existing service definition files: " + e);
			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					}
					catch (IOException e) {
						throw new RuntimeException(e); 
					}
				}
			}
		}
		
		PrintWriter writer = null;

		for (Map.Entry<String, TreeMap<Integer, String>> entry : services.entrySet()) {
			try {
				processingEnv.getMessager().printMessage(NOTE, "Writing META-INF/services/" + entry.getKey() + " " + entry.getValue());
				writer = new PrintWriter(new OutputStreamWriter(filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + entry.getKey()).openOutputStream(), "UTF-8"));
				map = entry.getValue();
			
				while (!map.isEmpty())
					writer.println(map.remove(map.lastKey()));

			} catch (IOException e) {
				processingEnv.getMessager().printMessage(ERROR, "Failed to write service definition files: " + e);
			}
			finally {
				if (writer != null)
					writer.close();
			}
		}
		return false;
	}

	private TypeElement getContract(TypeElement type, Service a) {
		// explicitly specified?
		try {
			a.value();
			throw new AssertionError();
		} catch (MirroredTypeException e) {
			TypeMirror m = e.getTypeMirror();
			if (m.getKind() == VOID) {
				// contract inferred from the signature
				boolean hasBaseClass = type.getSuperclass().getKind() != NONE && !isObject(type.getSuperclass());
				boolean hasInterfaces = !type.getInterfaces().isEmpty();
				
				if (hasBaseClass ^ hasInterfaces) {
					if(hasBaseClass)
						return (TypeElement)((DeclaredType)type.getSuperclass()).asElement();
					
					return (TypeElement)((DeclaredType)type.getInterfaces().get(0)).asElement();
				}

				error(type, "Contract type was not specified, but it couldn't be inferred.");
				return null;
			}

			if (m instanceof DeclaredType) {
				DeclaredType dt = (DeclaredType)m;
				return (TypeElement)dt.asElement();
			} else {
				error(type, "Invalid type specified as the contract");
				return null;
			}
		}
	}

	private boolean isObject(TypeMirror t) {
		if (t instanceof DeclaredType) {
			DeclaredType dt = (DeclaredType) t;
			return ((TypeElement)dt.asElement()).getQualifiedName().toString().equals("java.lang.Object");
		}
		return false;
	}

	private void error(Element source, String msg) {
		processingEnv.getMessager().printMessage(ERROR, msg, source);
	}
}
