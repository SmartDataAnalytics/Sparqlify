package org.aksw.sparqlify.core.sql.expr.serialization;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.TypeSystem;

public class SqlFunctionSerializerCast
    extends SqlFunctionSerializerBase1
{
    private TypeSystem typeSystem;
    private TypeToken typeToken;
    
    
    public SqlFunctionSerializerCast(TypeToken typeToken) {
        this.typeToken = typeToken;
    }
    
    
    @Override
    public String serialize(String a) {
        //typeSystem.getTypeMapper().g
        
        String result = "(CAST " + a + " AS " + ")";
        return result;
    }
}
