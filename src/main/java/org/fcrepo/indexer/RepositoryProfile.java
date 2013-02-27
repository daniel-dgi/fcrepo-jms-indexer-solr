package org.fcrepo.indexer;

import javax.inject.Inject;
import java.net.URL;

public class RepositoryProfile {

    private String repositoryURL;


    public void setRepositoryURL(String repositoryURL) {
        this.repositoryURL = repositoryURL;
    }

    public String getRepositoryURL() {
        return repositoryURL;
    }
}
