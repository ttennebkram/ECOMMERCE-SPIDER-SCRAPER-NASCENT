package com.netsol.scraper.util;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

import java.io.IOException;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: gauhars
 * Date: Apr 12, 2010
 * Time: 10:40:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class SolrManager
{
    private static SolrServer solrServer = null;
    private CoreContainer core = null;

    public SolrServer getSolrServer(String unitId)
    {
        if (solrServer == null)
        {
            try
            {
                //System.setProperty("solr.solr.home", "d:/projects/apache-solr-1.4.1/example");
                //System.setProperty("solr.solr.home", "/shared/solr14/example");
                System.setProperty("solr.solr.home", "/home/smartoci/solr/example/multicore");
                CoreContainer.Initializer initializer = new CoreContainer.Initializer();
                CoreContainer coreContainer = initializer.initialize();
                core = coreContainer;
                solrServer = new EmbeddedSolrServer(coreContainer, "core" + unitId);
            }
            catch (Exception exp)
            {
                exp.printStackTrace();
            }
        }

        return solrServer;
    }

    public void addDocument(SolrServer solrServer, Object bean) throws IOException, SolrServerException
    {
        solrServer.addBean(bean);
    }

    public void commit(SolrServer solrServer) throws IOException, SolrServerException
    {
        solrServer.commit();
    }

    public void deleteByQuery(SolrServer solrServer, String id) throws IOException, SolrServerException
    {
        solrServer.deleteByQuery(id);
    }

    public CoreContainer getCore()
    {
        return core;
    }

    public void setCore(CoreContainer core)
    {
        this.core = core;
    }
}
