package org.fcrepo.indexer.solr;

import org.apache.solr.common.SolrDocument;


public class ScriptIndexerShim implements ScriptIndexer {

    private ScriptIndexer scriptIndexer;

    @Override
    public void indexObject(String pid, SolrDocument doc) {
        scriptIndexer.indexObject(pid, doc);
    }

    public void setProxy(Object scriptIndexer) {
        this.scriptIndexer = (ScriptIndexer) scriptIndexer;
    }
}
