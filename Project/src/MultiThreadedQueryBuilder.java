import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * Multi-threaded version of QueryBuilder
 *
 * @author Sophia
 *
 */
public class MultiThreadedQueryBuilder implements QueryBuilderInterface {

	/**
	 * workQueue for multithreading
	 */
	private final WorkQueue queue;

	/**
	 * Stores the query string and the results in an collection
	 */
	private final TreeMap<String, Collection<InvertedIndex.QueryResult>> results;

	/**
	 * inverted index used to perform an exact search or partial search on the queries and returns the
	 * results
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * Constructor for multiThreaded query builder
	 *
	 * @param index takes in an invertedIndex
	 * @param queue queue for multithreading
	 */
	public MultiThreadedQueryBuilder(ThreadSafeInvertedIndex index, WorkQueue queue) {
		this.results = new TreeMap<>();
		this.index = index;
		this.queue = queue;

	}

	@Override
	public void parseQueries(Path queryPath, boolean hasExactFlag) throws IOException {
		QueryBuilderInterface.super.parseQueries(queryPath, hasExactFlag);
		queue.finish();
	}

	@Override
	public void parseQueries(String line, boolean hasExactFlag) throws IOException {
		queue.execute(new Task(line, hasExactFlag));
	}

	/**
	 * @author Sophia task that is required by the workQueue to perform
	 */
	private class Task implements Runnable {
		/**
		 * query line
		 */
		private String line;
		/**
		 * flag which determines which search we will use exact or partial
		 */
		private boolean hasExactFlag;

		/**
		 * @param line         parses line by line
		 * @param hasExactFlag determines if it is an exactSearch or partialSearch
		 */
		public Task(String line, boolean hasExactFlag) {
			this.line = line;
			this.hasExactFlag = hasExactFlag;
		}

		@Override
		public void run() {
			TreeSet<String> queryLines = TextFileStemmer.uniqueStems(line);
			String query = String.join(" ", queryLines);

			synchronized (results) {
				if (query.isBlank() || results.containsKey(query)) {
					return;
				}
			}
			var chooseSearch = index.chooseSearch(queryLines, hasExactFlag);
			synchronized (results) {
				results.put(query, chooseSearch);
			}

		}

	}

	@Override
	public void toJson(Path resultsPath) throws IOException {
		SimpleJsonWriter.queryWriteOut(results, resultsPath);
	}

}
