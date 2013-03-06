package gamboo.validators;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PathValidator implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		if (Files.notExists(Paths.get(value)))
			throw new ParameterException("Directory '" + value + "' not found.");
	}
}
