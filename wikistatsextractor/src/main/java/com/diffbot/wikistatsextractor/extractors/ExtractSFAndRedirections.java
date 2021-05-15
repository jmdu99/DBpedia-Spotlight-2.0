package com.diffbot.wikistatsextractor.extractors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.diffbot.wikistatsextractor.dumpparser.DumpParser;
import com.diffbot.wikistatsextractor.util.Tokenizer;
import com.diffbot.wikistatsextractor.util.Triplet;
import com.diffbot.wikistatsextractor.util.Util;
import com.diffbot.wikistatsextractor.util.Util.PairUriSF;

import org.apache.commons.lang.WordUtils;
import org.dbpedia.spotlight.db.model.*;
import org.dbpedia.spotlight.db.model.StringTokenizer;
import org.dbpedia.spotlight.db.tokenize.TextTokenizerFactory;

/**
 * Extract the Surface form as well as the uri they link to in the text.
 * Output the files uriCount and pairCounts in the output folder, 
 * as well as a few temporary files.
 * @author sam
 *
 */
public class ExtractSFAndRedirections {
	public static int MAX_LENGTH_SF = 80;
	public static int MIN_LENGTH_SF = 2;
	public static int NB_WORKERS=6;
	public static int MAX_NB_TOKEN_SF = 6;
	public static int MIN_OCCURENCE_COUPLE = 2;
	public static String LANGUAGE = "en";
	
	public static Map<String, String> pairs;
	public static Set<String >dbpediaURIs;

	public static class RedirAndSFWorker extends DumpParser.Worker {
		ConcurrentHashMap<Util.PairUriSF, Integer> surface_form_index;
		ConcurrentHashMap<String, String> redirection;
		// will receive the name of pages that actually exist
		ConcurrentHashMap<String, Integer> page_titles;

		protected StringTokenizer spotlightTokenizer;

		public RedirAndSFWorker(ConcurrentHashMap<Util.PairUriSF, Integer> surface_form_index, ConcurrentHashMap<String, String> redirection,
				ConcurrentHashMap<String, Integer> page_titles, StringTokenizer spotlightTokenizer) {
			this.surface_form_index = surface_form_index;
			this.redirection = redirection;
			this.page_titles = page_titles;
			this.spotlightTokenizer = spotlightTokenizer;
		}

		@Override
		public void doSomethingWithPage(String page) {
			/**
			 * obtain the list of paragraph from the page. Keep the reference,
			 * the lists and the links but get read of {{ }} and {| |}/ See the
			 * description of this function for more infos
			 */
			List<String> paragraphs = Util.getCleanTextFromPage(page, false, false, false,true);

			if (paragraphs != null) {

				HashMap<Util.PairUriSF, Integer> linksInArticle = new HashMap<>();

				/**
				 * obtain couples Uri - Surface pairs in each paragraph, and
				 * store it to a HashMap
				 */
				for (String paragraph : paragraphs) {
					List<Util.PairUriSF> pairsUriSF = Util.getAllSurfaceFormsInString(paragraph, MAX_LENGTH_SF, MIN_LENGTH_SF, MAX_NB_TOKEN_SF, LANGUAGE);
					for (Util.PairUriSF pusf : pairsUriSF) {
						Integer count = linksInArticle.get(pusf);
						if (count == null)
							linksInArticle.put(pusf, 1);
						else
							linksInArticle.put(pusf, 1 + count);
					}
				}

				/**
				 * Second run: duplicates per article
				 */

				HashMap<String, PairUriSF> bestLinkForSF = new HashMap<>();
				for (PairUriSF uriSF : linksInArticle.keySet()) {
					String sf = uriSF.surface_form;
					if (bestLinkForSF.containsKey(sf)) {
						if (linksInArticle.get(uriSF) >= linksInArticle.get(bestLinkForSF.get(sf))) {
							//If this is URI occurred more often with the SF, we use this one as the main link
							bestLinkForSF.put(sf, uriSF);
						}
					} else {
						bestLinkForSF.put(sf, uriSF);
					}
				}

				//Get all unlinked article links that occur after the first occurrence
				List<String> knownSurfaceFormsInString = Util.getKnownSurfaceFormsInParagraphs(paragraphs, bestLinkForSF.keySet(), MAX_NB_TOKEN_SF, spotlightTokenizer);

				//Reset the counts of the pairs that are seen both as normal links _and_ as SFs
				for (String sf : knownSurfaceFormsInString) {
					if (bestLinkForSF.containsKey(sf)) {
						PairUriSF pairUriSF = bestLinkForSF.get(sf);
						linksInArticle.put(pairUriSF, 0); //Reset the count of spotted SFs to 0 so we don't count them twice
					}
				}

				//Update the counts for the pairs that occur as link and SF
				for (String sf : knownSurfaceFormsInString) {
					if (bestLinkForSF.containsKey(sf)) {
						PairUriSF pairUriSF = bestLinkForSF.get(sf);
						linksInArticle.put(pairUriSF, linksInArticle.get(pairUriSF) + 1);
					}
				}

				//Update the global counts with the counts from this article:
				for (Map.Entry<PairUriSF, Integer> entry : linksInArticle.entrySet()) {
					Integer count = surface_form_index.get(entry.getKey());
					if (count == null)
						surface_form_index.put(entry.getKey(), entry.getValue());
					else
						surface_form_index.put(entry.getKey(), entry.getValue() + count);
				}
			}


			/** look for the title of the page */
			String title = Util.getTitle(page);
			if (Util.isWikiText(page) && title != null) {
				page_titles.put(title, 0);
			}

			/** now look for all the redirections */
			if (page.contains("<redirect title=")) {
				int start_redirect = page.indexOf("<redirect title=");
				int end_redirect = page.indexOf("/>", start_redirect);
				String redirect = page.substring(start_redirect + 16, end_redirect);
				redirect = redirect.trim();
				redirect = redirect.replaceAll("\"", "");
				redirect = Util.unescapeXML(redirect);
				if (title != null && redirect != null)
					redirection.put(title, redirect);
			}

		}

	}

	/** extract all surface forms and all redirection that it can get */
	public static void extractAllSurfaceFormsAndRedirection(final String path_to_wiki_articles, String path_to_output_surface_form,
			String path_to_output_redirections, String path_to_ouput_uri_counts, String path_to_output_sf_counts,
			TextTokenizerFactory spotlightTokenizerFactory) {

		/** container for the output */
		ConcurrentHashMap<Util.PairUriSF, Integer> surface_form_index = new ConcurrentHashMap<Util.PairUriSF, Integer>(10000000, 0.5f, 8);
		ConcurrentHashMap<String, String> redirection = new ConcurrentHashMap<String, String>(10000000, 0.5f, 8);
		ConcurrentHashMap<String, Integer> page_titles = new ConcurrentHashMap<String, Integer>();
		
		/** launch the dump Parsing */
		DumpParser dp = new DumpParser();
		for (int i = 0; i < NB_WORKERS; i++)
			dp.addWorker(new RedirAndSFWorker(surface_form_index, redirection, page_titles, spotlightTokenizerFactory.createTokenizer().getStringTokenizer()));
		dp.extract(path_to_wiki_articles);

		/** exploit the dump parsing (single threaded but should be fast */
		/**
		 * For each element of the HashMap, we follow the list of redirection
		 * that we got.
		 */
		
		dbpediaURIs = new HashSet<String>();
		
		
		ArrayList<Triplet> list_all_triplets = new ArrayList<Triplet>();
		for (PairUriSF key : surface_form_index.keySet()) {
			int count = surface_form_index.get(key);
			if (count < MIN_OCCURENCE_COUPLE)
				continue;
			if (key.surface_form.endsWith(","))
				continue;
			String uri = key.uri;
			String surface_form = key.surface_form;
			Triplet t = new Triplet(uri, surface_form, count);
			boolean has_redirection = true;
			int counter = 0;
			while (has_redirection) {
				counter++;
				if (counter > 15) {
					System.out.println("too many redirections: " + key + "  " + t.toString());
					break;
				}

				String uperified = Util.upperifyFirstChar(t.uri);
				if (redirection.containsKey(t.uri)) {
					String redirect_uri = redirection.get(t.uri);
					if (redirect_uri == null)
						has_redirection = false;
					else
						t.uri = redirect_uri;
				} else if (redirection.containsKey(uperified)) {
					String redirect_uri = redirection.get(uperified);
					if (redirect_uri == null)
						has_redirection = false;
					else
						t.uri = redirect_uri;
				} else {
					has_redirection = false;
				}
			}
			t.uri = Util.upperifyFirstChar(t.uri);

			/**
			 * we add the triplet in the list only if an actual page with this
			 * title exists
			 */
			Integer count_for_uri = page_titles.get(t.uri);
			if (count_for_uri != null) {
				list_all_triplets.add(t);
				dbpediaURIs.add(t.uri);
				page_titles.put(t.uri, t.count + count_for_uri);
			}
		}
		
		pairs = Util.loadWikidataResources(LANGUAGE);
		
		Collections.sort(list_all_triplets, new Triplet.SortByUri());
		
		int new_resources = 0;
		
		/** we don't need the concurrent HashMap anymore, let's free it */
		surface_form_index = null;

		/**
		 * we will now output the result, and we aggregate the count of the same
		 * couples (uri, surface_form)
		 */
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path_to_output_surface_form)), "UTF8"));
			
			for (Entry<String, String> uri: pairs.entrySet()) {
				String wikidataURI = uri.getValue();
				String sf = uri.getKey();
				if(!dbpediaURIs.contains(sf)) {
					bw.write(sf + "\t" + wikidataURI + "\t" + 25 +"\n");
					bw.write(sf.toLowerCase() + "\t" + wikidataURI + "\t" + 25 +"\n");
					if(!sf.toLowerCase().equals(sf.substring(0, 1).toLowerCase() + sf.substring(1))) {
						bw.write(sf.substring(0, 1).toLowerCase() + sf.substring(1) +"\t" + wikidataURI + "\t" + 25 +"\n");
					}
					if(sf.substring(1).matches(".*[A-Z].*") && !(sf.substring(0, 1) + sf.substring(1).toLowerCase()).equals(sf)){
						bw.write(sf.substring(0, 1) + sf.substring(1).toLowerCase() + "\t" + wikidataURI + "\t" + 25 +"\n");
					}
					if(!sf.equals((WordUtils.capitalizeFully(sf)))){
						bw.write(WordUtils.capitalizeFully(sf) + "\t" + wikidataURI + "\t" + 25 +"\n");
					}
					 new_resources++;
				}
			}
			
			System.out.println(new_resources + " new Wikidata resources");
			
			for (int i = 0; i < list_all_triplets.size(); i++) {
				Triplet t = list_all_triplets.get(i);
				int count=t.count;
				while (i + 1 < list_all_triplets.size() && Triplet.same_couple_uri_sf(t, list_all_triplets.get(i + 1))) {
					count += list_all_triplets.get(i + 1).count;
					i++;
				}
				bw.write(t.surface_form + "\t" + Util.uriStandardization(t.uri, null, LANGUAGE) + "\t" + count + "\n");
			}
			bw.close();
		} catch (IOException ioe) {
		}

		/** output the redirection map for future uses */
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path_to_output_redirections)), "UTF8"));
			for (String key : redirection.keySet()) {
				bw.write(key + ",," + redirection.get(key) + "\n");
			}
			bw.close();
		} catch (IOException ioe) {
		}

		/** output the counts per uri */
		try {
			
			String last_one = "";
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path_to_ouput_uri_counts)), "UTF8"));
			
			for (Entry<String, String> uri: pairs.entrySet()) {
				String wikidataURI = uri.getValue();
				String sf = uri.getKey();
				if(!dbpediaURIs.contains(sf)) {
					bw.write(wikidataURI + "\t" + 25 +"\n");
				}
			}
			
			for (Triplet t : list_all_triplets) {
				if (last_one.equals(t.uri))
					continue;
				last_one = t.uri;
				bw.write(Util.uriStandardization(t.uri, null, LANGUAGE) + "\t" + page_titles.get(t.uri) + "\n");
			}
			bw.close();
		} catch (IOException ioe) {
		}

		/** output the count per surface form */
		LinkedHashMap<String, Integer> counts_per_sf = new LinkedHashMap<String, Integer>();
		for (Triplet t : list_all_triplets) {

			Integer count = counts_per_sf.get(t.surface_form);
			if (count == null)
				counts_per_sf.put(t.surface_form, t.count);
			else
				counts_per_sf.put(t.surface_form, t.count + count);

		}
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path_to_output_sf_counts)), "UTF8"));
			for (String sf : counts_per_sf.keySet()) {
				bw.write(sf + ",," + counts_per_sf.get(sf) + "\n");
			}
			bw.close();
		} catch (IOException ioe) {
		}

	}

	public static void main(String[] args) {
	}

}
