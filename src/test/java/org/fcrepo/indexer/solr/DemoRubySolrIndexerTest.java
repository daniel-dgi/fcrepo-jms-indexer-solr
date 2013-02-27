package org.fcrepo.indexer.solr;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.abdera.model.Text.Type.TEXT;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test/solr-indexer.xml"})
public class DemoRubySolrIndexerTest {

    final private Logger logger = LoggerFactory.getLogger(DemoRubySolrIndexerTest.class);


    private Connection connection;

    private javax.jms.Session session;

    @Inject
    private ActiveMQConnectionFactory connectionFactory;

    private static String TEST_PID = "changeme:1001";

    @Inject
    private ScriptingSolrIndexer indexer;

    private static Message getAddMessage(Session session) throws JMSException {
        return getTestMessage(session, "addDatastream");
    }

    private static Message getDeleteMessage(Session session) throws JMSException {
        return getTestMessage(session, "purgeDatastream");
    }

    private static Message getTestMessage(Session session, String operation) throws JMSException {
        Abdera abdera = new Abdera();

        Entry entry = abdera.newEntry();
        entry.setTitle(operation, TEXT).setBaseUri("http://localhost:8080/rest");
        entry.addCategory("xsd:string", TEST_PID, "fedora-types:pid");
        entry.setContent("contentds");
        StringWriter writer = new StringWriter();
        try {
            entry.writeTo(writer);
        } catch (IOException e) {
            // hush
        }

        String atomMessage = writer.toString();
        return session.createTextMessage(atomMessage);
    }

    @Test
    public void testSimpleScriptingSolrIndexer() throws JMSException, IOException, SolrServerException {
        Message m = getAddMessage(session);

        SolrServer s = mock(SolrServer.class);

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", TEST_PID);
        doc.addField("ruby-indexer", "was-here");
        indexer.setSolrServer(s);
        indexer.onMessage(m);
        verify(s).add(argThat(new MatchesSolrDocument(doc)));
    }


    @Before
    public void acquireConnection() throws JMSException {
        logger.debug(this.getClass().getName() + " acquiring JMS connection.");
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, AUTO_ACKNOWLEDGE);
    }

    @After
    public void releaseConnection() throws JMSException {
        logger.debug(this.getClass().getName() + " releasing JMS connection.");
        session.close();
        connection.close();
    }

    private class MatchesSolrDocument extends BaseMatcher<SolrInputDocument> {
        private final SolrInputDocument source_doc;

        public MatchesSolrDocument(SolrInputDocument doc) {
            this.source_doc = doc;
        }

        @Override
        public boolean matches(Object o) {
            final SolrInputDocument test_doc = (SolrInputDocument) o;

            final Iterator<SolrInputField> it = source_doc.iterator();

            while(it.hasNext()) {
                final SolrInputField source_field = it.next();
                final SolrInputField test_field = test_doc.getField(source_field.getName());

                Collection<String> source_values = new ArrayList<String>();
                Collection<String> test_values = new ArrayList<String>();


                final Iterator<Object> fieldIterator = source_field.getValues().iterator();

                while(fieldIterator.hasNext()) {
                    source_values.add((String)fieldIterator.next());
                }

                final Iterator<Object> testFieldIterator = test_field.getValues().iterator();

                while(testFieldIterator.hasNext()) {
                    test_values.add((String)testFieldIterator.next());
                }


                if(!test_values.containsAll(source_values)) {
                    System.out.println(source_field.getName() + " did not match!");
                    System.out.println("Source:" + source_values.toString());
                    System.out.println("Test:" + test_values.toString());
                    return false;
                }
            }

            return true;
        }


        @Override
        public void describeTo(Description description) {

            description.appendValue(source_doc);
        }
    }
}
