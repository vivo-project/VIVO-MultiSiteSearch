package testAlone.localhost;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;

import edu.cornell.mannlib.vivo.mss.discovery.DiscoverUrisUsingListrdf;
import edu.cornell.mannlib.vivo.mss.discovery.DiscoveryWorker;
import edu.cornell.mannlib.vivo.mss.discovery.DiscoveryWorkerException;
import edu.cornell.mannlib.vivo.mss.utils.Log4JHelper;
import edu.cornell.mannlib.vivo.mss.utils.http.BasicHttpWorker;
import edu.cornell.mannlib.vivo.mss.utils.http.HttpClientFactory;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/**
 * Run a livetest against localhost, using hard-coded class URIS and site URL, a
 * hardcoded implementation of DiscoverUrisForSite, but actual HTTP transfers.
 * 
 * Run outside of Hadoop.
 */
public class TestListrdfOnLocalhost {
	private static final List<String> CLASS_URIS = Arrays.asList(
			"http://xmlns.com/foaf/0.1/Person",
			"http://vivoweb.org/ontology/core#Continent");

	public static void main(String[] args) throws DiscoveryWorkerException {
		Log4JHelper.resetToConsole();
		Log4JHelper.setLoggingLevel(Level.WARN);
		Log4JHelper.setLoggingLevel("edu.cornell", Level.DEBUG);

		DiscoveryWorker worker = new DiscoverUrisUsingListrdf(CLASS_URIS,
				new BasicHttpWorker(HttpClientFactory.standardClient()));
		Iterable<String> uris = worker
				.getUrisForSite("http://localhost:8080/vivo");
		for (String uri : uris) {
			System.out.println(uri);
		}
	}
}
