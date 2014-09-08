package uno.cod.converters;

import com.beust.jcommander.IStringConverter;
import java.util.logging.Level;

public class LevelConverter implements IStringConverter<Level> {
	@Override
	public Level convert(String value) {
		return Level.parse(value);
	}
}
