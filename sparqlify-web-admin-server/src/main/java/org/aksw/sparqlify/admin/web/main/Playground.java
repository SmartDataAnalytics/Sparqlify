package org.aksw.sparqlify.admin.web.main;

import org.aksw.sparqlify.admin.model.JdbcDataSource;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.admin.model.TextResource;

import com.google.gson.Gson;

public class Playground {
	public static void main(String[] args) throws Exception {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setJdbcUrl("http://foo");
		ds.setUsername("user");
		ds.setPassword("bar");//.toCharArray());
		
		TextResource textResource = new TextResource();
		textResource.setData("foobar");
		textResource.setType("baz");
		
		Rdb2RdfConfig conf = new Rdb2RdfConfig();
		conf.setJdbcDataSource(ds);
		conf.setTextResource(textResource);
		
		Gson gson = new Gson();
		String str = gson.toJson(conf);
		System.out.println(str);
	}

}
