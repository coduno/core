package uno.cod.validators;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.nio.file.Paths;
import java.net.MalformedURLException;
import java.net.URL;

public class JarURLValidator implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		try {
			URL url = new URL(value);
			
			if (!url.getProtocol().equals("jar"))
				throw new ParameterException("Protocol mismatch (need jar)");
		}
		catch (MalformedURLException e) {
			throw new ParameterException(e);
		}
	}
}
