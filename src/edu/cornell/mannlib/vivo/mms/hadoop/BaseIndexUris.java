package edu.cornell.mannlib.vivo.mms.hadoop;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vivo.mms.linkedData.ExpandingLinkedDataService;
import edu.cornell.mannlib.vivo.mms.solr.DocumentMaker;
import edu.cornell.mannlib.vivo.mms.solr.DocumentMakerImpl;

/**
 * This class accepts a list of URIs to index and then gets the linked data for the
 * URI, builds a solr document and adds that to a solr index.
 * 
 *  Input: list of lineNum -> URIs to index:
 *  ex.
 *  [ 1 : "http://vivo.cornell.edu/individual4344" ... ]
 *  
 *  Output: TBD. Maybe success or error for each URI?
 *  
 */
public abstract class BaseIndexUris extends Mapper<LongWritable , Text, Text, Text>{
	Log log = LogFactory.getLog(BaseIndexUris.class);

	DocumentMaker docMaker;	
	SolrServer solrServer;
	ExpandingLinkedDataService dataSource;

    /**
     * implementations must provide a linked data source. 
     */
    protected abstract void setupLinkedDataSource(Context context);

    /**
     * Implementations may override this method to prvoide a specialized Document Maker.
     */
    protected void setupDocMaker(Context context) {
        docMaker = new DocumentMakerImpl();        
    }

	
	@Override
    protected void setup(Context context) throws IOException,
            InterruptedException {
	    setupDocMaker(context);
	    setupSolrServer(context);
	    setupLinkedDataSource(context);
    }

    @Override
	protected void map(LongWritable lineNum, Text value, Context context)
			throws IOException, InterruptedException {

        String uri = value.toString();
        Model data = null;
        SolrInputDocument doc = null;

        try {
            data = getLinkedData(uri);
        } catch (Throwable ex) {
            log.error(ex, ex);
            context.write(value,
                    new Text("ERROR\tGettingLinkedData\t" + ex.getMessage()));
            return;
        }

        try {
            doc = makeDocument(uri, data);
        } catch (Throwable ex) {
            log.error(ex, ex);
            context.write(value,
                    new Text("ERROR\tMakingSolrDoc\t" + ex.getMessage()));
            return;
        }
        
        try {
            indexToSolr(doc);
        } catch (Throwable ex) {
            log.error(ex, ex);
            context.write(value,
                    new Text("ERROR\tIndexingSolrDoc\t" + ex.getMessage()));
            return;
        }

        context.write(value, new  Text("SUCCESS"));	    
	}

    protected void setupSolrServer(Context context){
        String solrUrl = context.getConfiguration().get(BuildIndexUtils.solrUrl);
        solrServer = new HttpSolrServer(solrUrl);
    }

    protected void indexToSolr(SolrInputDocument doc) throws SolrServerException, IOException {
        solrServer.add(doc);
    }

    protected SolrInputDocument makeDocument(String uri, Model data) {
        return docMaker.makeDocument(uri, data);
    }

    protected Model getLinkedData(String uri
           ) throws Exception {
    	Model m = ModelFactory.createDefaultModel();
    	dataSource.getLinkedData( uri, m);
        return m; 
    }

	
}
