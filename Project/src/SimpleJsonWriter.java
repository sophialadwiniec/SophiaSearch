import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines are used to
 * separate elements and nested elements are indented.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class concurrently,
 * access must be synchronized externally.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class SimpleJsonWriter {

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		level++;
		if (iterator.hasNext()) {
			writer.write("\n");
			indent(iterator.next(), writer, level);
		}
		while (iterator.hasNext()) {
			writer.write(",\n");
			indent(iterator.next(), writer, level);
		}
		writer.write("\n");
		indent("]", writer, level - 1);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		Iterator<Map.Entry<String, Integer>> entries = elements.entrySet().iterator();
		writer.write("{\n");
		level++;
		if (entries.hasNext()) {
			Map.Entry<String, Integer> entry = entries.next();
			quote(entry.getKey(), writer, level);
			writer.write(": " + entry.getValue());
		}
		while (entries.hasNext()) {
			writer.write(",\n");
			Map.Entry<String, Integer> entry = entries.next();
			quote(entry.getKey(), writer, level);
			writer.write(": " + entry.getValue());
		}
		writer.write("\n");
		indent("}", writer, level - 1);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param index index to write to
	 * @param path  path to write to
	 * @throws IOException throws IO if necessary
	 */
	public static void asDoubleNestedObject(Map<String, TreeMap<String, TreeSet<Integer>>> index, Path path)
			throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asDoubleNestedObject(index, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements elements the elements to use
	 * @param writer   writes to the path
	 * @param level    indentation on file
	 * @throws IOException throws exception
	 */
	public static void asDoubleNestedObject(Map<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer,
			int level) throws IOException {
		writer.write("{");
		Iterator<String> iter = elements.keySet().iterator();
		if (iter.hasNext()) {
			String key = iter.next();
			writer.write("\n");
			indent(writer, level + 1);
			quote(key, writer);
			writer.write(": ");
			asNestedObject(elements.get(key), writer, level + 1);
		}
		while (iter.hasNext()) {
			String key = iter.next();
			writer.write(",\n");
			indent(writer, level + 1);
			quote(key, writer);
			writer.write(": ");
			asNestedObject(elements.get(key), writer, level + 1);
		}
		indent("\n}", writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements elements the elements to use
	 * @param path     has a path to write to
	 * @throws IOException throws exception
	 */
	public static void asNestedObject(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements elements the elements to use
	 * @param writer   writes elements to path
	 * @param level    indentation level
	 * @throws IOException throws exception
	 */
	public static void asNestedObject(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {
		writer.write("{\n");
		Iterator<String> map = elements.keySet().iterator();
		if (map.hasNext()) {
			String next = map.next();
			indent(writer, level + 1);
			quote(next, writer);
			writer.write(": ");
			asArray(elements.get(next), writer, level + 1);
		}
		while (map.hasNext()) {
			String next = map.next();
			writer.write(",\n");
			indent(writer, level + 1);
			quote(next, writer);
			writer.write(": ");
			asArray(elements.get(next), writer, level + 1);
		}
		writer.write("\n}");
	}

	/**
	 * Writes the elements as a pretty JSON object with a nested array. The generic notation used allows
	 * this method to be used for any type of map with any type of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {
		Iterator<String> iterator = elements.keySet().iterator();
		writer.write("{\n");
		if (iterator.hasNext()) {
			String temp = iterator.next();
			quote(temp, writer, level + 1);
			writer.write(": ");
			asArray(elements.get(temp), writer, level + 1);
		}
		while (iterator.hasNext()) {
			writer.write(",\n");
			String temp = iterator.next();
			quote(temp, writer, level + 1);
			writer.write(": ");
			asArray(elements.get(temp), writer, level + 1);
		}
		writer.write("\n");
		indent("}", writer, --level);
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static String asNestedArray(Map<String, ? extends Collection<Integer>> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Indents using 2 spaces by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		for (int i = 0; i < times; i++) {
			writer.write(' ');
			writer.write(' ');
		}
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(String, Writer, int)
	 * @see #indent(Writer, int)
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(element.toString(), writer, times);
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		writer.write(element);
	}

	/**
	 * Writes the element surrounded by {@code " "} quotation marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @throws IOException if an IO error occurs
	 */
	public static void quote(String element, Writer writer) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Indents and then writes the element surrounded by {@code " "} quotation marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(Writer, int)
	 * @see #quote(String, Writer)
	 */
	public static void quote(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		quote(element, writer);
	}

	/**
	 * Writes the output in a nice array way for query arrays.
	 *
	 * @param query  each query
	 * @param writer the writer
	 * @throws IOException IO Exception
	 */
	public static void queryArrayWriteOut(Collection<InvertedIndex.QueryResult> query, Writer writer)
			throws IOException {
		if (!query.isEmpty()) {
			ArrayList<InvertedIndex.QueryResult> queries = new ArrayList<>(query);
			Iterator<InvertedIndex.QueryResult> iter = queries.iterator();

			if (iter.hasNext()) {
				indent("{\n", writer, 2);
				writeResult(iter.next(), writer);
			}

			while (iter.hasNext()) {
				indent("},\n", writer, 2);
				indent("{\n", writer, 2);
				writeResult(iter.next(), writer);
			}
			indent("}\n", writer, 2);
		}
	}

	/**
	 * Supports queries.
	 *
	 * @param queries each query
	 * @param path    the path specified
	 * @throws IOException IOException
	 */
	public static void queryWriteOut(TreeMap<String, Collection<InvertedIndex.QueryResult>> queries, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			Iterator<String> iter = queries.keySet().iterator();
			writer.append("{\n");

			if (iter.hasNext()) {
				String query = iter.next();
				quote(query, writer, 1);
				writer.write(": [\n");
				queryArrayWriteOut(queries.get(query), writer);
			}

			while (iter.hasNext()) {
				String query = iter.next();
				indent("],\n", writer, 1);
				quote(query, writer, 1);
				writer.write(": [\n");
				queryArrayWriteOut(queries.get(query), writer);
			}
			indent("]\n", writer, 1);
			writer.append("}\n");
		}
	}

	/**
	 * Method to write the results.
	 *
	 * @param result results as queries
	 * @param writer the writer
	 * @throws IOException IOException
	 */
	private static void writeResult(InvertedIndex.QueryResult result, Writer writer) throws IOException {
		quote("where", writer, 3);
		writer.write(": ");
		quote(result.getLocation(), writer);
		writer.write(",\n");
		quote("count", writer, 3);
		writer.write(": ");
		writer.write(result.getMatchCount().toString());
		writer.write(",\n");
		quote("score", writer, 3);
		writer.write(": ");
		writer.write(result.getScoreString());
		writer.write("\n");
	}

}
