import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * QueryBuilderInterface
 */
public interface QueryBuilderInterface {

	/**
	 * default method which parses queries
	 *
	 * @param queryPath    the path of the query
	 * @param hasExactFlag takes in exact flag to see which search we are using
	 * @throws IOException throws exception
	 */
	default void parseQueries(Path queryPath, boolean hasExactFlag) throws IOException {
		;
		try (BufferedReader reader = Files.newBufferedReader(queryPath, StandardCharsets.UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				parseQueries(line, hasExactFlag);
			}
		}
	}

	/**
	 * parses queries line by line
	 *
	 * @param line         the path of the query
	 * @param hasExactFlag hasExactFlag takes in exact flag to see which search we are using
	 * @throws IOException throws exception
	 */
	public void parseQueries(String line, boolean hasExactFlag) throws IOException;

	/**
	 * Calls method that writes in nice JSON format
	 *
	 * @param resultsPath resultsPath
	 * @throws IOException IOException
	 *
	 */
	public void toJson(Path resultsPath) throws IOException;

}
