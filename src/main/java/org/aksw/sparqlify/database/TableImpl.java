package org.aksw.sparqlify.database;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.time.StopWatch;



public class TableImpl<T>
	implements Table<T>
{
	private IndexMap<String, Column> columns;

	
	//private List<Index<T>> indexes = new ArrayList<Index<T>>();
	private IndexCollection<T> indexes = new IndexCollection<T>();

	public IndexCollection<T> getIndexes() {
		return indexes;
	}
	
	public TableImpl(IndexMap<String, Column> columns) {
		this.columns = columns;
	}
	
	@Override
	public void addIndex(Index<T> index) {
		if(index.getTable() != this) {
			throw new RuntimeException("Index has a different table set");
		}
		
		// TODO Sanity check on indexColumns
		
		indexes.add(index);
	}
	

    public static void main2(String[] args) {
        NavigableMap<String, String> m = new TreeMap<String, String>();
        m.put("m", "2");
        m.put("malta", "3");
        m.put("mali", "4");
        m.put("malibu", "5");
        m.put("macedonien", "6");

        boolean inclusive = true;
        
        System.out.println(StringUtils.longestPrefixLookup("malibuu", inclusive, m));
        System.out.println(StringUtils.longestPrefixLookup("malibu", inclusive, m));
        System.out.println(StringUtils.longestPrefixLookup("malib", inclusive, m));
        System.out.println(StringUtils.longestPrefixLookup("mali", inclusive, m));
        System.out.println(StringUtils.longestPrefixLookup("mal", inclusive, m));
        System.out.println(StringUtils.longestPrefixLookup("ma", inclusive, m));
        System.out.println(StringUtils.longestPrefixLookup("m", inclusive, m));
        System.out.println(StringUtils.longestPrefixLookup("", inclusive, m));
        
        System.out.println(StringUtils.getAllPrefixes("malibu", false, m));
    }

	
	public static void main(String[] args) {
		TableBuilder<String> builder = new TableBuilder<String>();
		builder.addColumn("g", String.class);
		builder.addColumn("s", String.class);
		builder.addColumn("p", String.class);
		builder.addColumn("o", String.class);
		
		Table<String> table = builder.create();
		
		
		TreeSet<String> set = new TreeSet<String>();
		
		set.add("Das");
		set.add("Das blah");
		set.add("Das ist");
		set.add("Das ist ein Satz");
		set.add("Das ist noch ein Satz");
		set.add("Das ist noch einer");
		
		System.out.println(set.tailSet("Da"));
		
		
		System.out.println(set.headSet("Das ist ", false));
		
		/*
		PrefixIndex<String> index = new PrefixIndex<String>();
		
		index.attach("g", "s", "p", "o");
		*/
		
		Transformer<Object, Set<String>> prefixExtractor = new Transformer<Object, Set<String>>() {

			@Override
			public Set<String> transform(Object input) {
				return Collections.singleton((String)input);
			}
			
		};
		
		MetaIndexFactory factory = new PrefixIndexMetaFactory(prefixExtractor);
		//MetaIndexFactory factory = new PatriciaAccessorFactory(prefixExtractor);
		
		IndexMetaNode root = IndexMetaNode.create(table, factory, "s");
		//IndexMetaNode s = IndexMetaNode.create(root, factory, "g");
		//IndexMetaNode o = IndexMetaNode.create(root, factory, "o");
		
		
		
		TreeIndex index = TreeIndex.attach(table, root);
		
		for(int i = 0; i < 1000; ++i) {
			table.add(Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		}
		
		
		table.add(Arrays.asList("aaa", "bbb", "ccc", "ddd"));
		table.add(Arrays.asList("bbb", "ccc", "ddd", "aaa"));
		table.add(Arrays.asList("ccc", "ddd", "aaa", "bbb"));
		table.add(Arrays.asList("ddd", "aaa", "bbb", "ccc"));
		
		table.add(Arrays.asList("Das", "ddd", "test", "ist"));
		
		//System.out.println(index.getStore());

		Map<String, Constraint> constraints = new HashMap<String, Constraint>();
		constraints.put("s", new StartsWithConstraint("dd"));
		constraints.put("g", new IsPrefixOfConstraint("Das"));

		Object rs = table.select(constraints);
		System.out.println(rs);
		

		StopWatch sw = new StopWatch();
		sw.start();
		
		for(int i = 0; i < 10000000; ++i) {
		
			Object x = table.select(constraints);
		}
		
		sw.stop();
		System.out.println(sw.getTime());
				
		
		//System.out.println(rs);
		/*
		
		PrefixIndex<String> idx = PrefixIndex.attach(prefixExtractor, table, "g", "s", "p", "o");
		PrefixIndex<String> idx2 = PrefixIndex.attach(prefixExtractor, table, "o", "s");
		
		
		table.add(Arrays.asList("aaa", "bbb", "ccc", "ddd"));
		table.add(Arrays.asList("bbb", "ccc", "ddd", "aaa"));
		table.add(Arrays.asList("ccc", "ddd", "aaa", "bbb"));
		table.add(Arrays.asList("ddd", "aaa", "bbb", "ccc"));
		
		table.add(Arrays.asList("aaa", "xxx", "yyy", "zzz"));

		
		printTable(idx.lookupSimple(Arrays.asList("aaa", "b")), System.out);
		printTable(idx.lookupSimpleLonger(Arrays.asList("a", "b")), System.out);
		
		
		// Now: Given a set of constraints, e.g. Prefix(?a, "foo"), determine the most appropriate indexes to do the lookup.
		
		
		Map<String, PrefixSet> prefixConstraints = new HashMap<String, PrefixSet>();
		
		
		prefixConstraints.put("s", new PrefixSet("xxxx"));
		prefixConstraints.put("o", new PrefixSet("zzzz"));

		prefixConstraints.put("g", new PrefixSet("aaaa"));
		prefixConstraints.put("p", new PrefixSet("yyyy"));

		{
			/*
			int constraintIndexes[] = new int[prefixConstraints.size()];
			for(String key : prefixConstraints.keySet()) {
				Column column = table.getColumns().get(key);
				
			}* /

			Index<String> index = table.getIndexes().get(prefixConstraints.keySet());
			
			System.out.println(index);
			
		}
		*/
		
		
	}
	
	public static <T> void printTable(Collection< ? extends List<T>> table, PrintStream out) {
		for(List<T> row : table) {
			for(int i = 0; i < row.size(); ++i) {
				if(i != 0) {
					out.print("\t");
				}
				
				Object o = row.get(i);
				String value = (o == null) ? "(null)" : o.toString();

				if(value.isEmpty()) {
					value = "(empty string)";
				}
				
				out.print(value);
			}
			out.println();
		}
	}


	@Override
	public IndexMap<String, Column> getColumns() {
		return columns;
	}




	@Override
	public void add(List<? extends T> row) {
		for(Index<T> index : indexes) {
			if(!index.preAdd(row)) {
				throw new RuntimeException("Row rejected");
			}
		}
		
		for(Index<T> index : indexes) {
			index.add(row);
			index.postAdd(row);
		}
	}

	@Override
	public Collection<List<Object>> select(Map<String, Constraint> constraints) {
		// TODO This is a hack passing the columns
		return indexes.get(constraints, this.getColumns());
	}

	@Override
	public int[] getIndexes(List<String> columnNames) {
		int[] result = new int[columnNames.size()];
		
		for(int i = 0; i < result.length; ++i) {
			String columnName = columnNames.get(i);
			result[i] = columns.getIndex(columnName);
		}
		
		return result;
	}
		
}
