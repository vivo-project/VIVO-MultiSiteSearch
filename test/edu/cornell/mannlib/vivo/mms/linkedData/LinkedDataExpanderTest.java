package edu.cornell.mannlib.vivo.mms.linkedData;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class LinkedDataExpanderTest {

    @Test
    public void test() {
        InputStream in = 
            this.getClass().getClassLoader().getResourceAsStream("resources/bdc34.n3");

        assertNotNull( in );

        Model m = ModelFactory.createDefaultModel();
        m.read( in, null , "N3" );
        assertTrue("The testing model created from bdc34.n3 should have some statements" , m.size() > 0 );
        
    }

}
