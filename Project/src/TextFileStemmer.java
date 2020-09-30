import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing and stemming text and text files into collections of stemmed words.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 *
 * @see TextParser
 */
// sophia
public class TextFileStemmer {

	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a list of cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static ArrayList<String> listStems(String line, Stemmer stemmer) {
		ArrayList<String> stemmed = new ArrayList<>();

		for (String word : TextParser.parse(line)) {
			stemmed.add(stemmer.stem(word).toString());
		}

		return stemmed;
	}

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return a list of cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #DEFAULT
	 * @see #listStems(String, Stemmer)
	 */
	public static ArrayList<String> listStems(String line) {
		// THIS IS PROVIDED FOR YOU; NO NEED TO MODIFY
		return listStems(line, new SnowballStemmer(DEFAULT));
	}

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {

		TreeSet<String> stemmed = new TreeSet<>();

		for (String word : TextParser.parse(line)) {
			stemmed.add(stemmer.stem(word).toString());
		}
		return stemmed;

	}

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #DEFAULT
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static TreeSet<String> uniqueStems(String line) {
		// THIS IS PROVIDED FOR YOU; NO NEED TO MODIFY
		return uniqueStems(line, new SnowballStemmer(DEFAULT));
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words, and then adds those
	 * words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #uniqueStems(String)
	 * @see TextParser#parse(String)
	 */
	public static TreeSet<String> uniqueStems(Path inputFile) throws IOException {
		TreeSet<String> stemmed = new TreeSet<>();

		if (!Files.exists(inputFile)) {
			return null;
		}

		try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
			String line = null;

			while ((line = reader.readLine()) != null) {
				stemmed.addAll(uniqueStems(line));
			}
		}

		return stemmed;
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words, and then adds those
	 * words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #uniqueStems(String)
	 * @see TextParser#parse(String)
	 */
	public static ArrayList<String> listStems(Path inputFile) throws IOException {
		ArrayList<String> stemmed = new ArrayList<>();

		if (!Files.exists(inputFile)) {
			return null;
		}

		try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
			String line = null;

			while ((line = reader.readLine()) != null) {
				stemmed.addAll(listStems(line));
			}
		}

		return stemmed;
	}

	/**
	 * A simple main method that demonstrates this class.
	 *
	 * @param args unused
	 * @throws IOException if an I/O error occurs
	 */
	public static void main(String[] args) throws IOException {
		String text = "PRACTIC *****, practic practical practice practiced practicer practices "
				+ "practicing practis practisants practise practised practiser "
				+ "practisers practises practising practitioner practitioners";

		// System.out.println(uniqueStems(text));
		System.out.println(listStems(text));

		Path inputPath = Path.of("test", "animals.text");
		Set<String> actual = TextFileStemmer.uniqueStems(inputPath);
		System.out.println(actual);
	}
}
