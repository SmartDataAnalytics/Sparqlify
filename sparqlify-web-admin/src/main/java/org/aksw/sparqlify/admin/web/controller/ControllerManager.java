package org.aksw.sparqlify.admin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@RequestMapping("/welcome")
@Controller
public class ControllerManager {

		
	// @RequestMapping(method = RequestMethod.GET)
	@RequestMapping(value = "/index-sparqlify-admin.do", method = RequestMethod.GET)
	public String showIndexPage(ModelMap model) {

		return "index-sparqlify-web-manager";
	}
}