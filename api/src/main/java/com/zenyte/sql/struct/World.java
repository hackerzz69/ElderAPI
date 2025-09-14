package com.zenyte.sql.struct;


public enum World {
    
    ECO,
    PVP,
    DEV,
    ;
    
    public static final World[] VALUES = values();
    
    World() {
    }
    
    public static World getWorld(final int id) {
        return VALUES[id - 1];
    }
    
}
