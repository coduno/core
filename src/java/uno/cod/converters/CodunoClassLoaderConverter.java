package uno.cod.converters;

import com.beust.jcommander.IStringConverter;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.net.URL;
import uno.cod.CodunoClassLoader;

public class CodunoClassLoaderConverter implements IStringConverter<CodunoClassLoader> {
	@Override
	public CodunoClassLoader convert(String value) {
		try {
			return new CodunoClassLoader(new URL(value));
		}
		catch (MalformedURLException|IllegalArgumentException e) {
			return null;
		}
	}
}
