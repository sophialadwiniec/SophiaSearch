import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data Structure that stores a word where it was found and the positions within the location
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class InvertedIndex {

	/**
	 * Stores a word, the locations that word was found, and the positions within that location the word
	 * was found.
	 *
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * countsIndex Stores the word count for each location in our index
	 */
	private final TreeMap<String, Integer> countsIndex;

	/**
	 * initializes Inverted index
	 */
	public InvertedIndex() {
		this.index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		this.countsIndex = new TreeMap<String, Integer>();
	}

	/**
	 * Adds a word, location, and position to the inverted index.
	 *
	 * @param word           adding the word to the inverted index
	 * @param file           fileName is the fileName change it to a string to fit my data structure
	 *                       adding the file name to the inverted index
	 * @param wordOccurrence adding the word occurrence (meaning what line the word occurred on
	 */
	public void add(String word, String file, Integer wordOccurrence) {
		index.putIfAbsent(word, new TreeMap<String, TreeSet<Integer>>());
		index.get(word).putIfAbsent(file, new TreeSet<Integer>());
		if (index.get(word).get(file).add(wordOccurrence)) {
			countsIndex.put(file, countsIndex.getOrDefault(file, 0) + 1);
		}
	}

	/**
	 * Merges other inverted index to current inverted index
	 *
	 * @param other Inverted Index
	 */
	public void addAll(InvertedIndex other) {
		// merge index
		for (String word : other.index.keySet()) {
			if (this.index.containsKey(word)) {
				for (String file : other.index.get(word).keySet()) {
					if (this.index.get(word).containsKey(file)) {
						this.index.get(word).get(file).addAll(other.index.get(word).get(file));
					} else {
						this.index.get(word).put(file, other.index.get(word).get(file));
					}
				}
			} else {
				this.index.put(word, other.index.get(word));
			}
		}
		// merge countsIndex
		for (String location : other.countsIndex.keySet()) {
			int count = other.countsIndex.get(location) + this.countsIndex.getOrDefault(location, 0);
			this.countsIndex.put(location, count);

		}

	}

	/**
	 * writes the inverted index back to the given path in pretty Json format
	 *
	 * @param path writes back to path
	 * @throws IOException throws IO Exception
	 */
	public void toJson(Path path) throws IOException {
		SimpleJsonWriter.asDoubleNestedObject(index, path);
	}

	/**
	 * writes the count back to the given path in pretty json format
	 *
	 * @param path path
	 * @throws IOException throws IOException
	 */
	public void countToJson(Path path) throws IOException {
		SimpleJsonWriter.asObject(countsIndex, path);
	}

	/**
	 * Number of stems
	 *
	 * @return size of index
	 */
	public int size() {
		return index.size();
	}

	/**
	 * Overriding the toString method
	 *
	 * @return the inverted index as a String
	 */
	@Override
	public String toString() {
		return index.toString();
	}

	/**
	 * Helper method to see if the index contains the word
	 *
	 * @param word the word passed
	 * @return a boolean
	 */
	public boolean contains(String word) {
		return index.containsKey(word);
	}

	/**
	 * Helper method to get unmodifiable words
	 *
	 * @return words as a Set, unmodifiable.
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(this.index.keySet());
	}

	/**
	 * Helper method to get locations given a word
	 *
	 * @param word the word passed
	 * @return a Set of Strings
	 */
	public Set<String> getLocations(String word) {
		if (contains(word)) {
			return Collections.unmodifiableSet(this.index.get(word).keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Helper method to get the positions given a word and a location
	 *
	 * @param word     the word passed
	 * @param location the location passed
	 * @return a Set of Integers
	 */
	public Set<Integer> getPositions(String word, String location) {
		if (contains(word, location)) {
			return Collections.unmodifiableSet(this.index.get(word).get(location));
		}
		return Collections.emptySet();
	}

	/**
	 * Helper method to see if the index contains the word given word, location, and position
	 *
	 * @param word     the word passed
	 * @param location the location passed
	 * @param position the position passed
	 * @return a boolean
	 */
	public boolean contains(String word, String location, int position) {
		if (contains(word, location)) {
			return index.get(word).get(location).contains(position);
		}
		return false;
	}

	/**
	 * Helper method to see if the index contains the word given word and location
	 *
	 * @param word     the word passed
	 * @param location the location passed
	 * @return a boolean
	 */
	public boolean contains(String word, String location) {
		if (contains(word)) {
			return index.get(word).keySet().contains(location);
		}
		return false;
	}

	/**
	 * chooses the search based on the flag given
	 *
	 * @param queries      takes in the queries
	 * @param hasExactFlag checks which flag to see if it is an exact search or partial search
	 * @return returns a collection<QueryResult> based off the search
	 */
	public Collection<QueryResult> chooseSearch(Collection<String> queries, boolean hasExactFlag) {
		if (hasExactFlag) {
			return exactSearch(queries);
		}
		return partialSearch(queries);
	}

	/**
	 * Helper method which breaks down repeated code
	 *
	 * @param totalResults all the results
	 * @param word         each word
	 * @param resultsList  the list of results
	 */
	private void searchHelper(HashMap<String, QueryResult> totalResults, String word,
			Collection<QueryResult> resultsList) {
		for (String location : index.get(word).keySet()) {
			if (!totalResults.containsKey(location)) {
				QueryResult queryResult = new QueryResult(location);
				totalResults.put(location, queryResult);
				resultsList.add(queryResult);
			}
			totalResults.get(location).update(word);
		}
	}

	/**
	 * exact search for queries
	 *
	 * @param queries to search with
	 * @return return search results in a collection
	 */
	public Collection<QueryResult> exactSearch(Collection<String> queries) {
		HashMap<String, QueryResult> totalResults = new HashMap<>();
		ArrayList<QueryResult> collectionResults = new ArrayList<>();

		for (String query : queries) {
			if (index.containsKey(query)) {
				searchHelper(totalResults, query, collectionResults);
			}
		}

		Collections.sort(collectionResults);
		return collectionResults;

	}

	/**
	 * partial search for queries
	 *
	 * @param queries to search with
	 * @return return search results in a collection
	 */
	public Collection<QueryResult> partialSearch(Collection<String> queries) {

		HashMap<String, QueryResult> totalResults = new HashMap<>();
		ArrayList<QueryResult> collectionResults = new ArrayList<>();

		for (String query : queries) {
			for (String word : index.tailMap(query).keySet()) {
				if (word.startsWith(query)) {
					searchHelper(totalResults, word, collectionResults);
				} else {
					break;
				}
			}
		}

		Collections.sort(collectionResults);
		return collectionResults;

	}

	/**
	 * Stores query variables, helper methods.
	 *
	 * @author CS 212 Software Development
	 * @author Sophia
	 * @version Spring 2020
	 */
	public class QueryResult implements Comparable<QueryResult> {

		/** Proper formatting for the queries. */
		private final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000000");

		/** Location */
		private final String location;

		/** Matches */
		private int matches;

		/** Score */
		private double score;

		/**
		 * initializes QueryResult
		 *
		 * @param location the file location
		 *
		 */
		public QueryResult(String location) {
			this.location = location;
			this.matches = 0;
		}

		/**
		 * Helper method, gets match count.
		 *
		 * @return an Integer
		 */
		public Integer getMatchCount() {
			return matches;
		}

		/**
		 * Helper method, gets the score
		 *
		 * @return double - the score
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * Helper method, gets location.
		 *
		 * @return a String, the location
		 */
		public String getLocation() {
			return this.location;
		}

		/**
		 * Helper method, figures out from the index directly how many more matches to add calculates the
		 * score
		 *
		 * @param word takes in the word to update the matches and score for that word
		 *
		 */
		private void update(String word) {
			this.matches += index.get(word).get(location).size();
			this.score = (double) this.matches / countsIndex.get(location);
		}

		/**
		 * Helper method, gets the score as a string specifically for json writer
		 *
		 * @return a string, the output / score.
		 */
		public String getScoreString() {
			return SCORE_FORMAT.format(this.score);
		}

		/**
		 * Compares each result and returns accordingly
		 *
		 * @param result queryResults that are compared
		 * @return an integer
		 */
		@Override
		public int compareTo(QueryResult result) {
			if (this.score == result.score && result.matches == this.matches) {
				return this.location.compareTo(result.location);
			} else if (this.score == result.score) {
				return Integer.compare(result.matches, this.matches);
			} else {
				return Double.compare(result.score, this.score);
			}
		}

	}

}
