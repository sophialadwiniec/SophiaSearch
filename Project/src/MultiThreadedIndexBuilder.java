import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * @author Sophia
 *
 *         multi-threaded indexBuilder
 */
public class MultiThreadedIndexBuilder extends InvertedIndexBuilder {
	/**
	 * logger to log errors and debug
	 */
	private final static Logger log = LogManager.getLogger(MultiThreadedIndexBuilder.class);

	/**
	 * Thread safe inverted index
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * Initializes stemmer to be used
	 */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * workQueue for multi-threading
	 */
	private final WorkQueue queue;

	/**
	 * Constructor for multi-threaded indexBuilder
	 *
	 * @param index takes in an invertedIndex
	 * @param queue queue for multi-threading
	 */
	public MultiThreadedIndexBuilder(ThreadSafeInvertedIndex index, WorkQueue queue) {
		super(index);
		this.index = index;
		this.queue = queue;
	}

	@Override
	public void parseDirectories(Path directory) throws IOException {
		super.parseDirectories(directory);
		queue.finish();

	}

	public Collection<InvertedIndex.QueryResult> search(Collection<String> queries, boolean exact) {
		return this.index.chooseSearch(queries, exact);
	}

	/**
	 * @param index index
	 * @param url   url
	 * @param line  line
	 */
	public static void urlHelper(InvertedIndex index, URL url, String line) {
		Integer counter = 1;
		Stemmer snowball = new SnowballStemmer(DEFAULT);
		for (String word : TextParser.parse(line)) {
			word = snowball.stem(word).toString();
			index.add(word, url.toString(), counter);
			counter++;
		}
	}

	/**
	 * @param url
	 */
	public void addUrl(URL url) {
		parseUrls(url, 50);
	}

	/**
	 * @param url   url
	 * @param limit limit
	 */
	public void parseUrls(URL url, int limit) {

		ArrayList<URL> urls = new ArrayList<URL>();
		HashSet<String> lookup = new HashSet<String>();
		// add one given
		int i;
		urls.add(url);
		// don't relook up same one
		lookup.add(url.toString());
		for (i = 0; i < urls.size() && i < limit; i++) {
			URL current = urls.get(i);
			String html = HtmlCleaner.stripComments(HtmlFetcher.fetch(current, 3));
			URLTask task = new URLTask(current, html);
			queue.execute(task);
			ArrayList<URL> temp = LinkParser.listLinks(current, html);
			for (URL found : temp) {
				if (!lookup.contains(found.toString())) {
					urls.add(found);
					lookup.add(found.toString());
				}
			}
		}
		queue.finish();
	}

	@Override
	public void parseTextFile(Path fileName) throws IOException {
		Task task = new Task(fileName);
		queue.execute(task);
	}

	/**
	 * Task class which allows code to create a task for each text file that is needed to be parsed
	 *
	 * @author Sophia
	 *
	 */
	private class Task implements Runnable {

		/**
		 * path to the file
		 */
		private final Path path;

		/**
		 * @param path path to the file
		 */
		public Task(Path path) {

			this.path = path;
		}

		@Override
		public void run() {
			try {

				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.parseTextFile(path, local);
				index.addAll(local);

			} catch (IOException e) {
				log.error("Could not add the following file " + path.toString());
			}

		}

	}

	/**
	 * Task class which allows code to create a task for each text file that is needed to be parsed
	 *
	 * @author Sophia
	 *
	 */
	private class URLTask implements Runnable {

		/**
		 * path to the file
		 */
		private final URL url;

		/**
		 *
		 */
		private final String html;

		/**
		 * @param url  url
		 * @param html html
		 *
		 */
		public URLTask(URL url, String html) {

			this.url = url;
			this.html = html;
		}

		@Override
		public void run() {

			InvertedIndex local = new InvertedIndex();
			String line = HtmlCleaner.stripHtml(html);
			MultiThreadedIndexBuilder.urlHelper(local, url, line);
			index.addAll(local);

		}

	}

}
