package gamboo.converters;

import com.beust.jcommander.IStringConverter;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.net.URL;
import gamboo.GambooClassLoader;

public class GambooClassLoaderConverter implements IStringConverter<GambooClassLoader> {
	@Override
	public GambooClassLoader convert(String value) {
		try {
			return new GambooClassLoader(new URL(value));
		}
		catch (MalformedURLException|IllegalArgumentException e) {
			return null;
		}
	}
}
