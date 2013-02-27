require 'java'
require 'loggable'
require 'rubydora'

java_package 'org.fcrepo.indexer.solr'

class DemoRubySolrIndexer
  include org.fcrepo.indexer.solr.ScriptIndexer

  def indexObject profile, pid, solr_document

    solr_document.addField("__org.fcrepo.indexer.solr.class__", self.class.name);

    repo = Rubydora.connect :url => profile.repository_url

      obj = repo.find(pid)

      return if obj.new?

      obj.profile.each do |key, values|
        Array(values).each do |v|
          solr_document.add_field(key, v)
        end
      end


  end
end