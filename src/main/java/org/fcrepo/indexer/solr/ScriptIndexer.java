package org.fcrepo.indexer.solr;

import org.apache.solr.common.SolrInputDocument;
import org.fcrepo.indexer.RepositoryProfile;

public interface ScriptIndexer {
    public void indexObject(RepositoryProfile profile, String pid, SolrInputDocument doc);
}
