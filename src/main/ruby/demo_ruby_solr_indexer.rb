require 'java'

java_package 'org.fcrepo.indexer.solr'

class DemoRubySolrIndexer
  include org.fcrepo.indexer.solr.ScriptIndexer

  def indexObject profile, pid, solr_document
    solr_document.addField("__org.fcrepo.indexer.solr.class__", self.class.name);
  end
end