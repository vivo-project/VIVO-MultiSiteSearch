package edu.cornell.mannlib.vivo.mss.hadoop;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import edu.cornell.mannlib.vivo.mss.MssMapperFailedException;
import edu.cornell.mannlib.vivo.mss.discovery.DiscoveryWorker;
import edu.cornell.mannlib.vivo.mss.utils.Log4JHelper;

/**
 * Get all the URIs for a given site.
 * 
 * INPUT: This mapper expects Text inputs that are URLs of sites. No keys are
 * expected. ex. [ "http://vivo.cornell.edu" ]
 * 
 * OUTPUT: URIs of individuals from the sites that should be added to the index.
 * The key should be the site URL and the value should be the URI of an
 * individual from that site. ex. [ "http://vivo.cornell.edu" :
 * "http://vivo.cornell.edu/indiviudal134" ... ]
 */
public abstract class BaseUriDiscovery extends
		Mapper<LongWritable, Text, Text, Text> {
	Log log = LogFactory.getLog(BaseUriDiscovery.class);

	protected final DiscoveryWorker uriSource;

	public BaseUriDiscovery(DiscoveryWorker uriSource) {
		this.uriSource = uriSource;
	}

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		Log4JHelper.addConfigfile("log4j.job.properties");
		super.setup(context);
	}

	@Override
	protected void map(LongWritable lineNum, Text urlOfSite, Context context)
			throws IOException, InterruptedException {
		try {
			String siteUrl = urlOfSite.toString();
			for (String uri : uriSource.getUrisForSite(siteUrl)) {
				context.getCounter(BuildIndexUtils.Counters.URIS_DISCOVERED)
						.increment(1);
				context.write(urlOfSite, new Text(uri));
			}
		} catch (Exception e) {
			throw new MssMapperFailedException(
					"Failed to retrieve URIs for site '" + urlOfSite + "'", e);
		}
	}
}
