package org.aksw.sparqlify.admin.web.main;

import org.springframework.context.annotation.Configuration;

//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration
//	extends WebSecurityConfigurerAdapter
//{
//
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		// @formatter:off
//		http
//			.authorizeUrls()
//				.antMatchers("/mvc/blog/**").hasRole("ADMIN")
//				.antMatchers("/mvc/blog").permitAll()
//				.antMatchers("/mvc/rest/*").permitAll()
//				.antMatchers("/mvc/status", "/mvc/status.txt").permitAll()
//			.and()
//				.formLogin()
//				.loginUrl("/mvc/auth/login")
//			 	.defaultSuccessUrl("/mvc/blog/posts")
//			 	.failureUrl("/mvc/auth/login")
//			 	.usernameParameter("user")
//			 	.passwordParameter("pwd")
//			 	.permitAll()
//			 .and()
//			 	.logout()
//			 	.logoutUrl("/mvc/auth/logout")
//			 	.logoutSuccessUrl("/mvc/blog")
//			 	.permitAll()
//			 ;
//		// @formatter:on
//	}
//
//	@Override
//	protected void registerAuthentication(AuthenticationManagerBuilder registry)
//			throws Exception {
//		registry.inMemoryAuthentication().withUser("admin").password("admin")
//				.roles("ADMIN");
//	}
//
//	@Override
//	public void configure(WebSecurity builder) throws Exception {
//		builder.ignoring().antMatchers("/mvc/static/**");
//	}
//
//}
