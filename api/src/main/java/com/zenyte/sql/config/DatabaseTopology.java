package com.zenyte.sql.config;

public enum DatabaseTopology {
    
    LOCAL(DatabaseCredential.LOCAL),
    BETA(DatabaseCredential.BETA_DOCKER);
    
    private DatabaseCredential[] nodes;
    
    DatabaseTopology(DatabaseCredential... nodes) {
        this.nodes = nodes;
    }
    
    public DatabaseCredential[] getNodes() {
        return nodes;
    }
}
