package gamboo.validators;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.net.MalformedURLException;
import java.net.URL;

public class URLValidator implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		try {
			new URL(value);
		}
		catch (MalformedURLException e) {
			throw new ParameterException(e);
		}
	}
}
