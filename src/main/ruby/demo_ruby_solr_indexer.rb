require 'java'

java_package 'org.fcrepo.indexer.solr'

class DemoRubySolrIndexer
  include org.fcrepo.indexer.solr.ScriptIndexer

  def indexObject pid, solr_document
    solr_document.addField("ruby-indexer", "was-here");
  end
end