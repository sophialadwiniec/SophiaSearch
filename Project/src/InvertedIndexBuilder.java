import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Parses directories and files to find only .txt files and .text files then goes through each word
 * in each file stems the word and adds them to an inverted index
 *
 * @author Sophia
 *
 */
public class InvertedIndexBuilder {

	/**
	 * stores the words parsed through the parseTextFile method
	 */
	private final InvertedIndex index;

	/**
	 * initializes InvertedIndex
	 *
	 * @param index inverted index
	 */
	public InvertedIndexBuilder(InvertedIndex index) {
		this.index = index;
	}

	/**
	 * Initializes stemmer to be used
	 */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Parses all valid text files then stems words and adds them to an inverted index
	 *
	 * @param fileName goes through the fileName using a buffered reader reads the file and gets the
	 *                 words at each line stems them and then adds the word, fileName, and word
	 *                 occurrence to the index counter represents the place the word takes place in the
	 *                 text file
	 * @param index    takes in invertedIndex to build to it and add
	 * @throws IOException IO Exception
	 */
	public static void parseTextFile(Path fileName, InvertedIndex index) throws IOException {
		String currLine;
		Integer counter = 1;
		Stemmer snowball = new SnowballStemmer(DEFAULT);
		String location = fileName.toString();
		try (BufferedReader read = Files.newBufferedReader(fileName, StandardCharsets.UTF_8)) {
			while ((currLine = read.readLine()) != null) {
				for (String word : TextParser.parse(currLine)) {
					word = snowball.stem(word).toString();
					index.add(word, location, counter);
					counter++;
				}
			}
		}
	}

	/**
	 * calls static version of parseTextFiles
	 *
	 * @param fileName takes in a fileName
	 * @throws IOException throws IO Exception
	 */
	public void parseTextFile(Path fileName) throws IOException {
		parseTextFile(fileName, this.index);
	}

	/**
	 * calls parseHelper
	 *
	 * @param directory parses the directory to find files if a file is found checks to make sure it is
	 *                  a valid file and therefore parses the text file otherwise there will be an error
	 * @throws IOException IO Exception
	 */
	public void parseDirectories(Path directory) throws IOException {
		parseHelper(directory);

	}

	/**
	 * Helper method which parses through directories and extracts only valid text files (those ending
	 * in .txt and .text) and passes them to parseTextFile
	 *
	 * @param directory parses the directory to find files if a file is found checks to make sure it is
	 *                  a valid file and therefore parses the text file otherwise there will be an error
	 * @throws IOException IO Exception
	 */
	private void parseHelper(Path directory) throws IOException {
		if (isValidFile(directory)) {
			parseTextFile(directory);
		} else if (Files.isDirectory(directory)) {
			try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
				for (Path file : listing) {
					parseHelper(file);
				}
			}
		}
	}

	/**
	 * checks to make sure the given path is a valid text file. Meaning it ends with .txt and .text
	 *
	 * @param file the file passed in to check only valid if it ends in text or txt or is a regular file
	 * @return returns a boolean
	 */
	public static boolean isValidFile(Path file) {
		String fileName = file.getFileName().toString().toLowerCase();
		return (Files.isRegularFile(file) && (fileName.endsWith(".txt") || fileName.endsWith(".text")));
	}

}
