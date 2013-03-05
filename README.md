This is an example fcrepo 4.x <=> solr indexer that listens to the Fedora JMS topic and indexes things into Solr accordingly. It is built relying heavily on Spring machinery, including:

* spring-lang
* spring-jms
* activemq-spring (?)


## Running the indexer

Beats me. I assume we should have a stand-alone mode that implements a main() method, but probably should also do something so it runs inside e.g. an OSGi container or Tomcat itself.

## Configuring the indexer
There is an example Spring configuration [in the tests](https://github.com/futures/fcrepo-jms-indexer-solr/blob/master/src/test/resources/spring-test/solr-indexer.xml), but it goes something like this:

```xml
 <bean class="org.apache.solr.client.solrj.impl.HttpSolrServer">
    <constructor-arg value="http://localhost:8983/solr" />

  </bean>

  <bean class="org.fcrepo.indexer.RepositoryProfile">
    <property name="repositoryURL" value="http://localhost:${test.port:8080}/rest" />
  </bean>

  <lang:jruby id="rubyScriptClass" script-interfaces="org.fcrepo.indexer.solr.ScriptIndexer" script-source="classpath:demo_ruby_solr_indexer.rb" />

  <!-- this is the Message Driven POJO (MDP) -->
  <bean id="solrIndexer" class="org.fcrepo.indexer.solr.ScriptingSolrIndexer" />

  <bean id="destination" class="org.apache.activemq.command.ActiveMQTopic">
    <constructor-arg value="fedora" />
  </bean>

  <!-- and this is the message listener container -->
  <bean id="jmsContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">
    <property name="connectionFactory" ref="connectionFactory"/>
    <property name="destination" ref="destination"/>
    <property name="messageListener" ref="solrIndexer" />
    <property name="sessionTransacted" value="true"/>
  </bean>
```

The magic is in the ```jmsContainer``` bean. It will listen to the ```destination``` for messages, and pass them onto our ```messageListener```. In this case, the ```messageListener``` is a Ruby script that is loaded in the ```lang:jruby``` construct.

The ```messageListener``` class implements the ```ScriptingSolrIndexer``` interface (which implements the ```javax.jms.MessageListener``` interface). It should respond to an ```onMessage``` call by taking the message and indexing the object appropriately.

