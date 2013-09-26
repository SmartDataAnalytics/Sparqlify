package org.aksw.sparqlify.admin.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class JdbcDataSource
	extends ResourceBase
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	private String jdbcUrl;
	private String username;
	
	// This is a plain text password storage!!!
	// TODO Support something more advanced
	private char[] password;

	public JdbcDataSource() {
		
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}
}
