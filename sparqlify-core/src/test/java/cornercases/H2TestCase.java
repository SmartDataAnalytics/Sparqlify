package cornercases;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.h2.jdbcx.JdbcDataSource;

public class H2TestCase {
	public static void main(String[] args)
		throws Exception
	{
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test_mem");
		ds.setUser("sa");
		ds.setPassword("sa");
		 
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS person;");
		
		conn.createStatement().executeUpdate("CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR, age INT)");

		conn.createStatement().executeUpdate("INSERT INTO person VALUES (1, 'Anne', 20)");
		conn.createStatement().executeUpdate("INSERT INTO person VALUES (2, 'Bob', 22)");
		
		ResultSet rs = conn.createStatement().executeQuery("SELECT name AS foo FROM person");
		
		ResultSetMetaData meta = rs.getMetaData();
		
		for(int i = 1; i <= meta.getColumnCount(); ++i) {
			String colName = meta.getColumnLabel(i);
			
			// Expected: Foo
			System.out.println("Column [" + i + "]: " + colName);
		}
	}
}
