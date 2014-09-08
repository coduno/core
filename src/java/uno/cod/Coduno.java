package uno.cod;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import uno.cod.validators.*;
import uno.cod.converters.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import static java.lang.Long.MAX_VALUE;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;

/**
 * Main entry point and static class that manages Coduno.
 *
 * @author Lorenz Leutgeb
 */
public class Coduno {
	@Parameter(description = "Parameters depending on the game")
	private static List<String> args = new ArrayList<String>();

	/**
	 * This ClassLoader is initialized with the URL specified by the
	 * commandline argument <code>--game</code>. It is used to load the
	 * Game implementation and other dependencies (extensions of the
	 * Solution interface) provided by the game.
	 */
	@Parameter(names = "--game", description = "URL of the jar file that provides the game as a service", required = true, converter = CodunoClassLoaderConverter.class, validateWith = JarURLValidator.class)
	private static CodunoClassLoader loader;

	/**
	 * Time for the whole game to complete (in milliseconds). Specified by
	 * commandline argument <code>--game-timeout</code>.
	 */
	@Parameter(names = "--game-timeout", description = "Timeout for running the game itself [ms]")
	private static Long gameTimeout = 1000L;

	/**
	 * The list of player IDs competing in this run. Values correlate with
	 * user IDs in the Coduno SQL database. Specified by commandline argument
	 * <code>--player</code> (multiple times).
	 */
	@Parameter(names = "--player", description = "List of player IDs participating in this game", required = true)
	private static List<Integer> players = new ArrayList<>();

	/**
	 * Because Java cannot wait for a {@link java.lang.Thread} for a specific
	 * amount of CPU time, we wait for <code>real time * timeFactor</code>
	 * and then check if it is running and the Thread's CPU time.
	 * Specified by commandline argument <code>--time-factor</code>.
	 */
	@Parameter(names = "--time-factor", description = "Realtime/CPU-time ratio for timeouts.")
	private static double timeFactor = 1.3;
	
	/**
	 * Name of the directory where player code and classes are stored, used
	 * to point ClassLoaders.
	 */
	@Parameter(names = "--player-classpath", description = "Directory where player code/binaries are rooted", validateWith = URLValidator.class, converter = URLConverter.class)
	private static URL classes;

	/**
	 * Simulation data generated by the Game is stored in a file in this
	 * directory.
	 */
	@Parameter(names = "--logpath", description = "Directory where simulation output is rooted", validateWith = URLValidator.class, converter = URLConverter.class)	
	private static URL logs;

	/**
	 * The interface between player code and game is normally provided via
	 * the {@link uno.cod.annotation.CodunoInterface} annotation, but can be
	 * overriden via <code>--player-interface</code>. In this case,
	 * {@link #face} is already set when {@link #run} is called.
	 */
	@Parameter(names = "--player-interface", description = "To overwrite the interface name normally specified via annotation", converter = ClassConverter.class)
	private static Class<Solution> face = null;

	/**
	 * To prevent players from cheating, games can order to block static
	 * variables (they still will be accessible, but only affect one and the
	 * same instance). The annotation value always overrides this value
	 * (see {@link #run}.
	 * Set to true with commandline flag <code>--avoid-static</code>.
	 */
	@Parameter(names = "--avoid-static", description = "Prevent players from communicating inside a class via static variables. If enabled, class files are loaded explicitly at every instantiation.")
	private static boolean avoidStatic = false;

	/**
	 * Loglevel for the whole Coduno environment.
	 *
	 * Specified via commandline argument <code>--loglevel</code>.
	 *
	 * @see java.util.logging.Level
	 */
	@Parameter(names = "--loglevel", description = "Sets the loglevel (see java.util.logging.Level docs)", converter = LevelConverter.class, validateWith = LevelValidator.class)
	private static Level level = SEVERE;

	/**
	 * Indicates if help was requested via commandline.
	 */
	@Parameter(names = { "-?", "-h", "--help" }, description = "Displays usage information", help = true)
	private static boolean help;

	/**
	 * Holds the current instance of Game to be passed statically.
	 */
	private static Game game;

	static {
		try {
			classes = new URL("file:///home/lorenz/workspace/coduno/players/");
		}
		catch (MalformedURLException e) {
			System.err.println("Static error");
			System.exit(1);
		}
	}

	/**
	 * Returns the currently loaded/ongoing Game.
	 */
	public static Game getGame() {
		return game;
	}

	/**
	 * Returns the supertype that {@link #game} requires all Solutions to
	 * implement in order to be compatible with the Game implementation.
	 * All classes implementing this interfaces are considered as third
	 * party and are gerneall not trusted.
	 */
	public static Class getInterface() {
		return face;
	}

	public static void main(final String[] args) {
		Coduno main = new Coduno();
		JCommander commander;

		try {
			commander = new JCommander(main, args);
		}
		catch (ParameterException e) {
			System.err.println(e.getMessage());
			System.exit(1);
			return;
		}

		commander.setProgramName("java [java-options] uno.cod.Coduno");

		if (help) {
			commander.usage();
			return;
		}

		if (!logs.getProtocol().equals("file")) {
			System.out.println("Currently, only local directories are supported as logpath. (file://)");
			System.exit(1);
		}

		if (!logs.toString().endsWith("/")) {
			System.out.println("logpath must be a directory.");
			System.exit(1);
		}

		Logger.getGlobal().setLevel(level);

		ServiceLoader<Game> gameService = ServiceLoader.load(Game.class, loader);

		for (Game item : gameService) {
			if (item != null) {
				for (Type element : item.getClass().getGenericInterfaces())
					if (((ParameterizedType)element).getRawType().equals(Game.class))
						face = (Class<Solution>)((ParameterizedType)element).getActualTypeArguments()[0];

				if (face == null)
					continue;

				game = item;
				break;
			}
		}

		if (game == null) {
			System.out.println("Unable to load Game. Please check META-INF/services/uno.cod.Game");
			System.exit(1);
		}

		Logger.getGlobal().info("Found " + players.size() + " players: " + players);

		String gamedir = loader.getURLs()[0].toString();
		gamedir = gamedir.substring(0, gamedir.indexOf(".jar!/"));
		gamedir = gamedir.substring(gamedir.lastIndexOf("/"));
		final File logfile = new File(logs.getPath() + gamedir + "/" + players.toString().replace("[", "").replace("]", "").replace(", ", "-") + ".log");

		if (!logfile.exists())
			logfile.getParentFile().mkdirs();

		List<Solution> list = new ArrayList<Solution>(players.size());

		boolean simulate = !logfile.exists();

		for (Integer item : players) {
			try {
				if (!simulate)
					if (logfile.lastModified() < new File(classes.getPath() + item.toString() + ".jar").lastModified())
						simulate = true;

				ServiceLoader<Solution> service = ServiceLoader.load(face, new CodunoClassLoader(new URL("jar:" + classes.toString() + item.toString() + "/tictactoe.jar!/"), loader));

				if (!service.iterator().hasNext()) {
					System.err.println("Unable to load Solution for player " + item + ". Please check " + classes + "/" + item + ".jar!/META-INF/services/" + face.getName());
					System.exit(1);
				}

				Solution solution = service.iterator().next();

				if (solution == null) {
					System.err.println("Unable to load Solution for player " + item + ". Please check " + classes + "/" + item + ".jar!/META-INF/services/" + face.getName());
					System.exit(1);
				}

				list.add(solution);
			}
			catch (IOException|SecurityException|ServiceConfigurationError e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		if (!simulate) {
			System.out.println("The player's jar files have not been modified.");
			System.exit(0);
		}

		game.load(list);

		System.setSecurityManager(new CodunoSecurityManager(classes.toString()));

		if (players.size() != Coduno.game.getPlayerCount()) {
			System.out.println("Wrong player count!");
			System.exit(1);
		}

		try {
			final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(logfile));
			run(game.getClass().getName() + "#run", new Runnable() {
				@Override
				public void run() {
					game.run(Coduno.args, writer);

					try {
						writer.close();
					}
					catch (IOException e) {}
				}
			}, gameTimeout);
		}
		catch (CodunoTimeoutException|FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Class result = game.getWinner().getClass();

		if (result == null) {
			System.out.println("Tie!");
			return;
		}
		try {
			System.out.println("Winner: " + resolve(result));
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * To prevent cheating via static variables, this method can be used
	 * to obtain a clone of a Solution, that is itself a Solution but does
	 * not share the same static space. This is achieved by loading the
	 * class again with a different ClassLoader.
	 */
	public Solution reload(final Solution solution) {
		// TODO iterate

		ServiceLoader<Solution> loaders = ServiceLoader.load(face, new CodunoClassLoader(solution.getClass().getProtectionDomain().getCodeSource().getLocation(), loader));

		if (!loaders.iterator().hasNext()) {
			return null;
		}

		return loaders.iterator().next();
	}

	/**
	 * Basically the same as {@link #run(String, Runnable, long, Thread.UncaughtExceptionHandler)}
	 * but does install a standard {@link java.lang.Thread.UncaughtExceptionHandler}.
	 *
	 * @see #run(String, Runnable, long, Thread.UncaughtExceptionHandler)
	 */
	public static long run(String name, final Runnable runnable, Long timeout) throws CodunoTimeoutException {
		return run(name, runnable, timeout, new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				if (e.getClass() != ThreadDeath.class) {
					System.err.println("UncaughtException " + e + " in " + t.getName() + ":");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Runs a {@link java.lang.Runnable} in a {@link uno.cod.CodunoThread} with
	 * a given name and respects a maximum CPU-running-time.
	 *
	 * @param name gives the {@link java.lang.Thread} a name. It is appended
	 *             to <code>"coduno-<i>id</i>"</code>.
	 * @param runnable the class, but basically Method, to be run by the
	 *                 thread.
	 * @param timeout maximum CPU-time the {@link java.lang.Thread} is
	 *                is allowed to run for.
	 * @return the time the {@link java.lang.Thread} actually executed in
	 *         nanosecond precision, but not necessarily nanosecond accuracy.
	 * @throws CodunoTimeoutException if the thread attempted to execute
	 *                                longer than <code>timeout</code>
	 *                                milliseconds.
	 * @see java.lang.management.ThreadMXBean#getCurrentThreadCpuTime
	 */
	@SuppressWarnings("deprecation")
	public static long run(String name, final Runnable runnable, long timeout, Thread.UncaughtExceptionHandler handler) throws CodunoTimeoutException {
		if (name == null) name = Long.toString(System.nanoTime(), 32);

		CodunoThread thread = new CodunoThread(runnable, "coduno-" + Long.toString(System.nanoTime(), 16) + "-" + name, timeout * 1000000);
		thread.setUncaughtExceptionHandler(handler);
		thread.start();

		try {
			thread.join((int)Math.ceil(timeout * timeFactor));
		}
		catch (InterruptedException e) {
			thread.stop();
			return thread.getTotalTime();
		}

/*		if (thread.isAlive()) {
			try {
				thread.stop();
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
			finally {
				throw new CodunoTimeoutException("Thread " + thread.getName() + " ran more than " + timeout * timeFactor + "ms realtime.");
			}
		} */
		while (thread.getRemainingTime() > 0 && thread.isAlive()) {
			try {
				thread.join((int)Math.ceil(timeout * (timeFactor - 1.0)));
			}
			catch (InterruptedException e) {
				thread.stop();
				return thread.getTotalTime();
			}
		}

		if (thread.getRemainingTime() < 0) {
			thread.stop();

			throw new CodunoTimeoutException("Thread " + thread.getName() + " ran " + thread.getTotalTime() + "ns CPU-time.");
		}

		return thread.getTotalTime();
	}

	/**
	 * Gets the unique ID of the player that wrote the provided solution by
	 * looking at where it was loaded from.
	 *
	 * @param c the Class of the user that should be resolved.
	 * @return the ID of the User (correlating with the Coduno SQL database.)
	 * @throws IllegalArgumentException if <code>c</code> is <code>null</code>
	 *         or {@link #face} is not assignable from <code>c</code>
	 *         or the jar file where this class was loaded from does not lie
	 *         in {@link #classes} or the resulting ID is no integer.
	 */
	public static String resolve(final Class c) {
		if (c == null)
			throw new IllegalArgumentException();

		if (!face.isAssignableFrom(c))
			throw new IllegalArgumentException();

		String id = c.getProtectionDomain().getCodeSource().getLocation().getPath();
		id = id.substring(0, id.indexOf("!/") - 4);

		try {
			id = new URL(id).getPath();
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		if (!id.startsWith(classes.getPath()))
			throw new IllegalArgumentException();

		try {
			return id.substring(classes.getPath().length());
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}
}