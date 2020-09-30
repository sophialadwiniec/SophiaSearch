import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and returns HTML content if
 * possible.
 *
 * @see HttpsFetcher
 */
public class HtmlFetcher {

	/**
	 * Returns {@code true} if and only if there is a "Content-Type" header and the first value of that
	 * header starts with the value "text/html" (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {

		if (headers.containsKey("Content-Type")) {
			return (headers.get("Content-Type").get(0).startsWith("text/html"));
		}
		return false;
	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the status line is stored
	 * under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		if (headers.containsKey(null)) {
			String statusCode = headers.get(null).toString();
			String[] realStatusCode = statusCode.split(" ");
			return (Integer.parseInt(realStatusCode[1]));
		}
		return -1;
	}

	/**
	 * Returns {@code true} if and only if the HTTP status code is between 300 and 399 (inclusive) and
	 * there is a "Location" header with at least one value.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isRedirect(Map<String, List<String>> headers) {
		int code = getStatusCode(headers);
		return (code >= 300 && code < 400 && headers.containsKey("Location") && !headers.get("Location").isEmpty());
	}

	/**
	 * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status code is 200 and the
	 * content type is HTML, returns the HTML as a single string. If the status code is a valid
	 * redirect, will follow that redirect if the number of redirects is greater than 0. Otherwise,
	 * returns {@code null}.
	 *
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the resource is not html
	 *
	 * @see HttpsFetcher#openConnection(URL)
	 * @see HttpsFetcher#printGetRequest(PrintWriter, URL)
	 * @see HttpsFetcher#getHeaderFields(BufferedReader)
	 * @see HttpsFetcher#getContent(BufferedReader)
	 *
	 * @see String#join(CharSequence, CharSequence...)
	 *
	 * @see #isHtml(Map)
	 * @see #isRedirect(Map)
	 */
	public static String fetch(URL url, int redirects) {
		Map<String, List<String>> map = new HashMap<>();
		try {
			map = HttpsFetcher.fetchURL(url);
			if (isHtml(map)) {
				if (getStatusCode(map) > 399) {
					return null;
				} else if (isRedirect(map) && redirects > 0) {
					String regex = "<a[^>]*\\s*href\\s*=\\s*\"\\s*(.*?)\\s*\"\\s*";
					Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
					if (map.get("Content") != null) {
						try {
							Matcher matcher = pattern.matcher(String.join(" ", map.get("Content")));
							while (matcher.find()) {
								String string = matcher.group(1);
								if (!string.isEmpty() && string.length() > 0) {
									URL link = new URL(string);
									return fetch(link, redirects - 1);
								}
							}
						} catch (MalformedURLException e) {
							System.out.println("exception");
						}
					}
				} else if (isRedirect(map) && redirects <= 0) {
					return null;
				}
				return String.join("\n", map.get("Content"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)}.
	 *
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url, int redirects) {
		try {
			return fetch(new URL(url), redirects);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls {@link #fetch(URL, int)}
	 * with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url) {
		return fetch(url, 0);
	}

	/**
	 * Calls {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the resource is not html
	 */
	public static String fetch(URL url) {
		return fetch(url, 0);
	}
}
