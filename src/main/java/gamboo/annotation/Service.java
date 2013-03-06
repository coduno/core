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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Target;
import javax.annotation.processing.SupportedSourceVersion;
import static javax.lang.model.SourceVersion.RELEASE_7;

/**
 * Indicates that this class should be listed in <tt>META-INF/services/<i>interface</i></tt>.
 *
 * @author Kohsuke Kawaguchi, Lorenz Leutgeb
 * @see gamboo.annotation.ServiceProcessor
 */
@Retention(SOURCE)
@Documented
@Target(TYPE)
@SupportedSourceVersion(RELEASE_7)
public @interface Service {

	Class value() default void.class;
	
	/**
	 * If there is more than one implementation of this service's type
	 * (either defined implictily or via {@link #value()}), The value of
	 * {@link #priority()} is crucial for sorting. The higher the priority,
	 * the higher the chances that this service is listed first.
	 */
	int priority() default 0;
}
