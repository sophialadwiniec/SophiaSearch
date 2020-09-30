import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible for storing and parsing the queries.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class QueryBuilder implements QueryBuilderInterface {

	/**
	 * Stores the query string and the results in an collection
	 */
	private final TreeMap<String, Collection<InvertedIndex.QueryResult>> results;

	/**
	 * inverted index used to perform an exact search or partial search on the queries and returns the
	 * results
	 */
	private final InvertedIndex index;

	/**
	 * intializes queryBuilder
	 *
	 * @param index inverted index
	 *
	 */
	public QueryBuilder(InvertedIndex index) {
		this.results = new TreeMap<>();
		this.index = index;
	}

	/**
	 * Inherits from queryBuilderInterface
	 *
	 * @param queryPath    the path of the query
	 * @param hasExactFlag takes in exact flag to see which search we are using
	 * @throws IOException throws exception
	 */
	@Override
	public void parseQueries(Path queryPath, boolean hasExactFlag) throws IOException {
		QueryBuilderInterface.super.parseQueries(queryPath, hasExactFlag);
	}

	/**
	 * parses queries line by line
	 *
	 * @param line         the path of the query
	 * @param hasExactFlag hasExactFlag takes in exact flag to see which search we are using
	 * @throws IOException throws exception
	 */
	@Override
	public void parseQueries(String line, boolean hasExactFlag) throws IOException {
		TreeSet<String> queryLines = TextFileStemmer.uniqueStems(line);
		String query = String.join(" ", queryLines);
		if (!query.isBlank() && !results.containsKey(query)) {
			results.put(query, index.chooseSearch(queryLines, hasExactFlag));
		}
	}

	/**
	 * Calls method that writes in nice JSON format
	 *
	 * @param resultsPath resultsPath
	 * @throws IOException IOException
	 *
	 */
	@Override
	public void toJson(Path resultsPath) throws IOException {
		SimpleJsonWriter.queryWriteOut(results, resultsPath);
	}

}
