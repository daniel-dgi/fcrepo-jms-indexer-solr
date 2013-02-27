package org.fcrepo.indexer.solr;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
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
        verify(s).add(eq(doc));
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

}
