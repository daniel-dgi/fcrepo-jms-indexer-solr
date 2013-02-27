package org.fcrepo.indexer.solr;

import org.apache.solr.common.SolrInputDocument;

public interface ScriptIndexer {
    public void indexObject(String pid, SolrInputDocument doc);
}
