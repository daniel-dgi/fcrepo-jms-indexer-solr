package org.fcrepo.indexer.solr;

import org.apache.solr.common.SolrDocument;

public interface ScriptIndexer {
    public void indexObject(String pid, SolrDocument doc);
}
