package uno.cod.validators;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.util.logging.Level;

public class LevelValidator implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		try {
			Level.parse(value.toUpperCase());
		}
		catch (IllegalArgumentException|NullPointerException e) {
			throw new ParameterException("Parameter " + name + " should be one of [ ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING ]");
		}
	}
}
