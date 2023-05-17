package org.aksw.sparqlify.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringCast;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;

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


    public static final String HIVE = "apache hive";
    public static final String POSTGRES = "postgresql";
    public static final String MYSQL = "mysql";

    public static void init(Map<String, SqlBackendConfig> map) {
        map.put(HIVE, new SqlBackendConfig(new DatatypeToStringCast(), SqlCodecUtils.createSqlCodecForApacheSpark()));
        map.put(POSTGRES, new SqlBackendConfig(new DatatypeToStringPostgres(), SqlCodecUtils.createSqlCodecDefault()));
        map.put(MYSQL, new SqlBackendConfig(new DatatypeToStringCast(), SqlCodecUtils.createSqlCodecForApacheSpark()));
    }

    public Map<String, SqlBackendConfig> getMap() {
        return nameToConfig;
    }

    public void put(String name, SqlBackendConfig config) {
        nameToConfig.put(name.toLowerCase(), config);
    }


    @Override
    public SqlBackendConfig apply(String t) {
        SqlBackendConfig result = nameToConfig.get(t.toLowerCase());
        return result;
    }
}
