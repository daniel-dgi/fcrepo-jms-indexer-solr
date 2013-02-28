require 'java'
require 'rubygems'
require 'active_fedora'

java_package 'org.fcrepo.indexer.solr'
class DemoRubySolrIndexer
  include org.fcrepo.indexer.solr.ScriptIndexer
  
  class TestConfigurator
    attr_reader :fedora_config, :solr_config, :predicate_config
    
    def init(options = {})
      @fedora_config = options[:fedora_config]
      @solr_config = options[:solr_config]
      @predicate_config = options[:predicate_config]
    end
  end
  
  def indexObject profile, pid, solr_document

    solr_document.addField("__org.fcrepo.indexer.solr.class__", self.class.name);

    @config_params = {
      :fedora_config => { :url => profile.repository_url },
      :solr_config => { :url => profile.repository_url },
      :predicate_config => { 
        :default_namespace => 'info:fedora/fedora-system:def/relations-external#',
        :predicate_mapping => {
          'info:fedora/fedora-system:def/relations-external#' => { :has_part => 'hasPart' } 
        }
      }
    }

    ActiveFedora.configurator = DemoRubySolrIndexer::TestConfigurator.new
    ActiveFedora.init @config_params

    begin
        obj = ActiveFedora::Base.find(pid)
    rescue ActiveFedora::ObjectNotFoundError
      return
    end

    obj.to_solr.each do |key, values|
        next if key == :id
        Array(values).each do |v|
          solr_document.add_field(key.to_s, v.to_s)
        end
    end

  end




  
     
end
