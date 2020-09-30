import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class responsible for running this project based on the provided command-line arguments. See the
 * README for details.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class Driver {

	/**
	 * logger to log errors and debug
	 */
	private final static Logger log = LogManager.getLogger(Driver.class);

	/**
	 * Initializes the classes necessary based on the provided command-line arguments. This includes
	 * (but is not limited to) how to build or search an inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */

	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		ArgumentParser parser = new ArgumentParser(args);

		InvertedIndex invertedIndex;
		InvertedIndexBuilder builder;
		QueryBuilderInterface queryBuilder;
		int threads;
		ThreadSafeInvertedIndex threadSafe = new ThreadSafeInvertedIndex();

		WorkQueue queue = null;

		URL url = null;
		int limit = 50;

		if (parser.hasFlag("-threads") || parser.hasFlag("-url")) {
			try {
				if (parser.hasFlag("-threads")) {
					threads = Integer.parseInt(parser.getString("-threads"));
					if (threads <= 0) {
						threads = 5;
					}
				} else {
					threads = 5;
				}

			} catch (NumberFormatException e) {
				log.error("Invalid thread value. Must be an integer.");
				threads = 5;
			}
			queue = new WorkQueue(threads);

			invertedIndex = threadSafe;
			builder = new MultiThreadedIndexBuilder(threadSafe, queue);

			queryBuilder = new MultiThreadedQueryBuilder(threadSafe, queue);

		} else {
			invertedIndex = new InvertedIndex();
			builder = new InvertedIndexBuilder(invertedIndex);
			queryBuilder = new QueryBuilder(invertedIndex);
		}

		if (parser.hasFlag("-url")) {

			try {
				url = new URL(parser.getString("-url"));
				if (parser.hasFlag("-limit")) {
					limit = Integer.parseInt(parser.getString("-limit"));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				log.error("Invalid thread value. Must be an integer.");
				limit = 50;
			}
			((MultiThreadedIndexBuilder) builder).parseUrls(url, limit);
		}

		if (parser.hasFlag("-path")) {
			if (parser.getPath("-path") != null) {
				Path inputPath = parser.getPath("-path");
				try {
					builder.parseDirectories(inputPath);
				} catch (IOException e) {
					log.error("Unable to build inverted index from path " + inputPath);
				}
			} else {
				log.debug("The path flag is provided without a value");
			}
		}

		if (parser.hasFlag("-index")) {
			Path outputPath = parser.getPath("-index", Path.of("index.json"));
			try {
				invertedIndex.toJson(outputPath);
			} catch (IOException e) {
				log.error("Unable to write to path " + outputPath);
			}
		}

		if (parser.hasFlag("-counts")) {
			Path countsPath = parser.getPath("-counts", Path.of("counts.json"));
			try {
				invertedIndex.countToJson(countsPath);
			} catch (IOException e) {
				log.error("Unable to write to path " + countsPath.toString());
			}
		}

		if (parser.hasFlag("-query")) {
			Path queryPath = parser.getPath("-query");
			if (queryPath == null) {
				log.debug("Missing query path!");
			} else {
				try {
					queryBuilder.parseQueries(queryPath, parser.hasFlag("-exact"));
				} catch (IOException e) {
					log.error("Unable to write to path " + queryPath.toString());
				}

			}
		}

		if (parser.hasFlag("-results")) {
			Path resultsPath = parser.getPath("-results", Path.of("results.json"));

			try {
				queryBuilder.toJson(resultsPath);
			} catch (IOException e) {
				log.error("Unable to write to path " + resultsPath.toString());
			}
		}

		int number;

		if (parser.hasFlag("-port")) {

			try {
				number = Integer.parseInt(parser.getString("-port"));
				if (number != 8080) {
					number = 8080;
				}
			} catch (NumberFormatException e) {
				log.error("Invalid number");
				number = 8080;
			}

			try {
				ServletContextHandler servletContextHandler = null;

				servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
				servletContextHandler.setContextPath("/");

				DefaultHandler defaultHandler = new DefaultHandler();
				defaultHandler.setServeIcon(true);

				ContextHandler contextHandler = new ContextHandler("/favicon.ico");
				contextHandler.setHandler(defaultHandler);

				SearchServlet searchServlet = new SearchServlet(queryBuilder, invertedIndex, builder);
				ServletHolder servletHolder = new ServletHolder(searchServlet);

				ServletHandler servletHandler = new ServletHandler();
				servletHandler.addServletWithMapping(servletHolder, "/");

				Server server = new Server(number);
				server.setHandler(servletHandler);
				server.start();
				server.join();

			} catch (Exception e) {
				System.err.println("Jetty server Did not work");
			}
		}

		if (queue != null) {
			queue.shutdown();
		}

		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

}
