package com.thinkgem.jeesite.modules.sys.listener;

import javax.servlet.ServletContext;
//import javax.servlet.annotation.WebListener;

//import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import com.thinkgem.jeesite.modules.sys.service.SystemService;
/**
 * 监听器的类
 * @WebListener Servlet3.0以上可一用注解，不需要配置xml
 * 
 * @author Json
 *
 */
//@WebListener
public class WebContextListener extends org.springframework.web.context.ContextLoaderListener {
	
	 @Override
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
		if (!SystemService.printKeyLoadMessage()){
			return null;
		}
		return super.initWebApplicationContext(servletContext);
		
	}

	
}
