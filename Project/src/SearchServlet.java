import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * @author Sophia
 *
 */
public class SearchServlet extends HttpServlet {

	/**
	 * Initializes stemmer to be used
	 */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;
	/** The title to use for this webpage. */
	private static final String TITLE = "SophiaSearch";

	/** Everything that will be output after a search is carried out. */
	private LinkedList<String> output;

	private MultiThreadedIndexBuilder builder;

	private InvertedIndex index;
	/**
	 * The number of searches
	 */
	private int searches = 0;

	/**
	 * The time it took
	 */
	private double seconds;

	private QueryBuilderInterface queryBuilder;

	/**
	 * @param queryBuilder
	 * @param index
	 */
	public SearchServlet(QueryBuilderInterface queryBuilder, InvertedIndex index, InvertedIndexBuilder builder) {
		super();
		output = new LinkedList<>();
		this.queryBuilder = queryBuilder;
		this.index = index;
		this.builder = (MultiThreadedIndexBuilder) builder;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		out.printf("<!DOCTYPE html>%n");
		out.printf("<html>");
		out.printf("<head>");
		out.printf("	<meta charset=\"utf-8\">");
		out.printf("	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
		out.printf("	<title>%s</title>%n", TITLE);
		out.printf(
				"	<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/bulmaswatch/0.8.1/superhero/bulmaswatch.min.css\">%n");
		out.printf("</head>%n");
		out.printf("%n");
		out.printf("<body>%n");
		out.printf("	  <div class=\"hero-body\">%n");
		out.printf("	    <div class=\"center container\" style=\"text-align:center\">%n");
		out.printf("<figure class=\"image is-96x96\">\n"
				+ "  <img  src=\"https://cdn.shopify.com/s/files/1/1506/0026/files/Double_S_logo_black_white_stroke_495x.png?v=1551562090\">\n"
				+ "</figure>");
		out.printf("	      <h1 class=\"title\">%n");
		out.printf(TITLE + "%n");
		out.printf("	      </h1>");
		out.printf("	    <div class=\"center container\" style=\"text-align:left\">%n");

		out.printf("<p1></p1>");
		out.printf("<p1></p1>");

		out.printf("<p1></p1>");
		out.printf("			<form method=\"%s\" action=\"%s\">%n", "POST", request.getServletPath());
		out.printf("				<div class=\"field\">%n");
		out.printf("					<div class=\"control has-icons-left\">%n");
		out.printf(
				"						<input class=\"input\" type=\"text\" name=\"%s\" placeholder=\"Search something....\">%n",
				"search");
		out.printf("						<span class=\"icon is-small is-left\">%n");
		out.printf("							<i class=\"fas fa-search\"></i>%n");
		out.printf("						</span>%n");
		out.printf("					</div>%n");
		out.printf("				</div>%n");
		out.printf("%n");
		out.printf("%n");

		out.printf("<div class=\"control\">\n" + "		  <label class=\"radio\">\n"
				+ "		    <input type=\"radio\" name=\"answer1\">\n" + "		    Partial\n" + "		  </label>\n"
				+ "		  <label class=\"radio\">\n" + "		    <input type=\"radio\" name=\"answer2\">\n"
				+ "		    Exact\n" + "		  </label>\n" + "		  <label class=\"radio\">\n"
				+ "		    <input type=\"radio\" name=\"answer3\">\n" + "		    New Crawl\n" + "		</div>");

		out.printf("%n");
		out.printf("%n");

		out.printf(" <div class=\"control\">%n");
		out.printf(
				" <button class=\"button is-primary\" onclick=\\\"clicked()\\\" name=\\\"enter\\\" type=\\\"submit\\\">%n%n");
		out.printf(" <i class=\"fas fa-glass-cheers\"></i>%n");
		out.printf(" &nbsp;%n");
		out.printf("Search" + "%n");
		out.printf(" </button>%n");

		out.printf(" <button class=\"button is-link\" onclick=\"clicked()\" name=\"lucky\" type=\"submit\">%n");
		out.printf(" <i class=\"fas fa-glass-cheers\"></i>%n");
		out.printf(" &nbsp;%n");
		out.printf("I'm feeling lucky!%n");
		out.printf(" </button>%n");
		out.printf(" </div>%n");
		out.printf(" </form>%n");
		out.printf(" </div>%n");
		out.printf("%n");
		out.printf(" <section class=\"section\">%n");
		out.printf(" <div class=\"container\">%n");

		out.printf("	<section class=\"section\">%n");
		out.printf("		<div class=\"container\">%n");
		if (!output.isEmpty()) {
			for (String message : output) {
				out.printf("				<div class=\"box\">%n");
				out.printf(message);
				out.printf("				</div>%n");
				out.printf("%n");
			}
		}
		out.printf("			</div>%n");
		out.printf("%n");
		out.printf("		</div>%n");
		out.printf("	</section>%n");
		out.printf("	<footer class=\"footer\">%n");
		out.printf("	  <div class=\"content has-text-centered\">%n");
		out.printf("	    <p>%n");
		out.printf("%n");
		out.printf("	    <p>%n");
		out.printf("<p1>Results Found: " + searches + "</p1>%n");
		out.printf("	      </p>%n");
		out.printf("	    <p>%n");
		out.printf("It took " + seconds + " ms.</p1>%n");
		out.printf("	    </p>%n");
		out.printf("%n");
		out.printf("					<i class=\"fas fa-calendar-alt fa-pulse\"></i>%n");
		out.printf("					&nbsp;Last Visited %s%n", getDate());
		out.printf("	    </p>%n");
		out.printf("<br />");
		out.printf("	  </div>");
		out.printf("	</footer>");
		out.printf("</body>");
		out.printf("</html>");

		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Instant start = Instant.now();

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		String message = request.getParameter("search");
		String answer1 = request.getParameter("answer1");
		String answer3 = request.getParameter("answer3");

		String formatString = null;

		// String message2 = request.getParameter("crawl");
		// URL url;
		boolean urlBoolean = false;

		if (message == null) {
			message = "";
		}

		// message = StringEscapeUtils.escapeHtml4(message);

		if (message != null) {
			response.getWriter();

			boolean search = true;

			if (answer1 != null && answer1.contains("on")) {
				search = false;
			} else if (answer3 != null && answer3.contains("on")) {
				output.clear();
				URL url = new URL(message);
				builder.addUrl(url);
				formatString = String.format(
						"					<i class=\"fas fa-quote-left has-text-grey-light\"></i> %s <i class=\"fas fa-quote-right has-text-grey-light\"></i>%n"
								+ "					<p class=\"has-text-grey is-size-7 has-text-right\"></p>%n",
						"The URL was crawled");
				urlBoolean = true;
				output.add(formatString);
			} else {
				search = true;
			}

			if (urlBoolean != true) {
				SnowballStemmer stemmer = new SnowballStemmer(DEFAULT);
				ArrayList<String> queryList = new ArrayList<String>();
				for (String part : message.split(" ")) {
					if (part == " ") {
						part = "";
					}
					queryList.add((stemmer.stem(part.toLowerCase())).toString());
				}

				Collection<InvertedIndex.QueryResult> results = this.builder.search(queryList, search);

				if (results == null || results.isEmpty()) {
					searches = 0;
					output.clear();

					formatString = String.format(
							"					<i class=\"fas fa-quote-left has-text-grey-light\"></i> %s <i class=\"fas fa-quote-right has-text-grey-light\"></i>%n"
									+ "					<p class=\"has-text-grey is-size-7 has-text-right\"></p>%n",
							"The String: " + request.getParameter("search") + " was not found", getDate());
					output.add(formatString);

				} else if (request.getParameter("lucky") != null) {
					ArrayList<InvertedIndex.QueryResult> newResult = new ArrayList<>(results);
					String location = newResult.get(0).getLocation();
					response.sendRedirect(location);
				} else {
					searches = 0;
					output.clear();

					for (InvertedIndex.QueryResult result : results) {
						String score = String.format("%.3f", result.getScore());
						formatString = String.format("<a href=\"%s\">%s</a>"
								+ "					<p class=\"has-text-grey is-size-7 has-text-right\">%s</p>%n"
								+ "					<p class=\"has-text-grey is-size-7 has-text-right\">%s</p>%n"
								+ "					<p class=\"has-text-grey is-size-7 has-text-right\">%s</p>%n",
								result.getLocation(), result.getLocation(), "score: " + score,
								"matches: " + result.getMatchCount(), getDate());
						searches++;
						output.add(formatString);
					}
				}

			}
		}

		Duration elapsed = Duration.between(start, Instant.now());
		seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();

		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(request.getServletPath());
		response.flushBuffer();
	}

	private static String getDate() {
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}
}