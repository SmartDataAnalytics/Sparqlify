package sparql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.aksw.commons.util.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;

import com.google.common.base.Joiner;

public class ViewTable
{
    private DataSource dataSource;
    private String tableName;
    private List<String> varNames;
    private boolean isCreated = false;

    private Connection conn;

    /**
     * Allow lazy creation of the table.
     *
     * @throws SQLException
     */
    private void prepare()
        throws SQLException
    {
        if(!isCreated) {
            createTable(dataSource, tableName, varNames);
            isCreated = true;

            conn = dataSource.getConnection();
        }
    }

    public static List<String> toStringSet(Set<Var> varNames)
    {
        List<String> result = new ArrayList<String>();
        for(Var varName : varNames) {
            result.add(varName.getVarName());
        }

        return result;
    }

    public ViewTable(DataSource dataSource, String tableName, Set<Var> varNames)
    {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.varNames = toStringSet(varNames);
    }


    public static Pair<List<String>, List<String>> getValues(Binding binding)
    {
        List<String> keys = new ArrayList<String>();
        List<String> values = new ArrayList<String>();

        Iterator<Var> it = binding.vars();

        while(it.hasNext()) {
            Var key = it.next();

            keys.add(key.getName());
            values.add(binding.get(key).toString());
        }

        return Pair.create(keys, values);
    }

    public static Pair<String, String> toParts(Binding binding)
    {
        Pair<List<String>, List<String>> pair = getValues(binding);

        return Pair.create(
                "(" + Joiner.on(", ").join(pair.getKey()) + ")",
                "('" + Joiner.on("', '").join(pair.getValue()) + "')");
    }

    public static String toSqlPredicate(Binding binding)
    {
        Pair<String, String> pair = toParts(binding);

        return pair.getKey() + " = " + pair.getValue();
    }

    private ResultSet executeQuery(String query)
        throws SQLException
    {
        // TODO Retry on connection error
        return conn.createStatement().executeQuery(query);
    }

    /**
     *
     *
     * Returns all bindings
     *
     * @param binding
     * @return
     * @throws SQLException
     */
    public Iterable<Binding> lookup(Binding binding)
        throws SQLException
    {
        prepare();

        String sql = "SELECT * FROM " + tableName + " WHERE TRUE AND " + toSqlPredicate(binding);

        ResultSet rs = executeQuery(sql);
        ResultSetMetaData meta = rs.getMetaData();

        List<Binding> result = new ArrayList<Binding>();

        while(rs.next()) {

            BindingMap item = new BindingHashMap();
            for(int i = 1; i <= meta.getColumnCount(); ++i) {

                Object o = rs.getObject(i);
                // TODO HACK! Deal properly with the types here
                Node node = NodeFactory.createURI(o.toString());

                item.add(Var.alloc(meta.getColumnName(i)), node);
            }
        }

        return result;
    }

    public void insert(Binding binding)
        throws SQLException
    {
        prepare();


        Pair<String, String> pair = toParts(binding);

        String sql = "INSERT INTO " + tableName + pair.getKey() + " VALUES " + pair.getValue() + ";";
        //" SET " + toSqlPredicate(binding) + ";";
        //conn.createStatement().execute(sql);

        System.out.println(sql);
    }


    public void remove(Binding binding)
        throws SQLException
    {
        prepare();

        String sql = "DELETE FROM " + tableName + " WHERE " + toSqlPredicate(binding) + ";";
        conn.createStatement().execute(sql);
    }

    public void clear()
        throws SQLException
    {
        prepare();

        String sql = "DELETE FROM " + tableName;
        conn.createStatement().execute(sql);
    }

    public void createIndex(String var) {
        // Create Index ...
    }

    public static void createTable(DataSource dataSource, String tableName, Iterable<String> varNames)
        throws SQLException
    {
        List<String> columns = new ArrayList<String>();
        for(String varName : varNames)  {
            columns.add("\t" + varName + " text");
        }

        String sql = "CREATE TABLE " + tableName + "(\n" + Joiner.on(",\n").join(columns) + ");";

        Connection conn = dataSource.getConnection();

        try {
            conn.createStatement().execute(sql);
        } catch(Exception e) {
            // TODO HACK Check properly if table already exists
            System.err.println("Exception type: " + e.getClass());
            e.printStackTrace();
        }

        //conn.close();
    }
}
