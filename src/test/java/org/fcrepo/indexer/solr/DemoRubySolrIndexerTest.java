package org.fcrepo.indexer.solr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test/solr-indexer.xml"})
public class DemoRubySolrIndexerTest {

    @Test
    public void testSimpleScriptingSolrIndexer() {
        assert(true);
    }
}
