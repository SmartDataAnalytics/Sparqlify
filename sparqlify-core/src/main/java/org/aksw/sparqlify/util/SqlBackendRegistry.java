package org.aksw.sparqlify.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aksw.sparqlify.backend.postgres.DatatypeToStringCast;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaperBacktick;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaperDoubleQuote;

public class SqlBackendRegistry
    implements Function<String, SqlBackendConfig>
{
    private static SqlBackendRegistry instance;
    
    private Map<String, SqlBackendConfig> nameToConfig = new HashMap<>();
    
    public static SqlBackendRegistry get() {
        if(instance == null) {
            instance = new SqlBackendRegistry();
            init(instance.nameToConfig);
        }
       
        return instance;
    }
    
    
    public static final String HIVE = "Apache Hive";
    public static final String POSTGRES = "Postgresql";
    
    public static void init(Map<String, SqlBackendConfig> map) {
        map.put(HIVE, new SqlBackendConfig(new DatatypeToStringCast(), new SqlEscaperBacktick()));
        map.put(POSTGRES, new SqlBackendConfig(new DatatypeToStringPostgres(), new SqlEscaperDoubleQuote()));        
    }
    
    public Map<String, SqlBackendConfig> getMap() {
        return nameToConfig;
    }
    
    public void put(String name, SqlBackendConfig config) {
        nameToConfig.put(name, config);        
    }


    @Override
    public SqlBackendConfig apply(String t) {
        SqlBackendConfig result = nameToConfig.get(t);
        return result;
    }
}
