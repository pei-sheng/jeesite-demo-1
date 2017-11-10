/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.sys.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Maps;
import com.thinkgem.jeesite.common.config.Global;
import com.thinkgem.jeesite.common.security.shiro.session.SessionDAO;
import com.thinkgem.jeesite.common.servlet.ValidateCodeServlet;
import com.thinkgem.jeesite.common.utils.CacheUtils;
import com.thinkgem.jeesite.common.utils.CookieUtils;
import com.thinkgem.jeesite.common.utils.IdGen;
import com.thinkgem.jeesite.common.utils.StringUtils;
import com.thinkgem.jeesite.common.web.BaseController;
import com.thinkgem.jeesite.modules.sys.security.FormAuthenticationFilter;
import com.thinkgem.jeesite.modules.sys.security.SystemAuthorizingRealm.Principal;
import com.thinkgem.jeesite.modules.sys.utils.UserUtils;

/**
 * 登录Controller
 * 
 * @author ThinkGem
 * @version 2013-5-31
 */
@Controller
public class LoginController extends BaseController {

	@Autowired
	private SessionDAO sessionDAO;

	/**
	 * 管理登录
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * 
	 * 		update 9/30
	 */
	@RequestMapping(value = "${adminPath}/login", method = RequestMethod.GET)
	public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
		// 1.获取当前用户对象
		Principal principal = UserUtils.getPrincipal();

		//  默认页签模式
//		String tabmode = CookieUtils.getCookie(request, "tabmode");
//		if (tabmode == null) {
//			CookieUtils.setCookie(response, "tabmode", "1");
//			logger.debug("ye'qai" + tabmode);
//		}
		
		//logger.isDebugEnabled() 预先判断，减少系统构造参数时间，提高系统性能。
		//官方的说法，执行一次logger.isDebugEnabled()这样的判断花费的时间大概是写日志时间的万分之一.虽然这个比例很小，但是，程序中的任何地方放到并发的环境下，我们就得重新考虑了。
		if (logger.isDebugEnabled()) {
			logger.debug("login, active session size: {}", sessionDAO.getActiveSessions(false).size());
		}

		//2.获取jeesite.properties 文件中“notAllowRefreshIndex” 的key值      如果已登录，再次访问主页，则退出原账号。  
		if (Global.TRUE.equals(Global.getConfig("notAllowRefreshIndex"))) {
			CookieUtils.setCookie(response, "LOGINED", "false");
		}

		//3.授权用户登录，且不为手机端登陆， 重定向到adminPath
		if (principal != null && !principal.isMobileLogin()) {
			return "redirect:" + adminPath;
		}
		//4.返回sysLogin登陆页面
		return "modules/sys/sysLogin";
	}

	/**
	 * 登录失败，真正登录的POST请求由Filter完成
	 */
	@RequestMapping(value = "${adminPath}/login", method = RequestMethod.POST)
	public String loginFail(HttpServletRequest request, HttpServletResponse response, Model model) {
		// 1.获取当前登陆者信息
		Principal principal = UserUtils.getPrincipal();
		// 2.如果已经登录，则跳转到管理首页
		if (principal != null) {
			return "redirect:" + adminPath;
		}

		String username = WebUtils.getCleanParam(request, FormAuthenticationFilter.DEFAULT_USERNAME_PARAM);
		boolean rememberMe = WebUtils.isTrue(request, FormAuthenticationFilter.DEFAULT_REMEMBER_ME_PARAM);
		boolean mobile = WebUtils.isTrue(request, FormAuthenticationFilter.DEFAULT_MOBILE_PARAM);
		String exception = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
		String message = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_MESSAGE_PARAM);
		
		//3.isBlank 为空，空白字符    是为true || message ="null" 提示用户名或密码错误
		if (StringUtils.isBlank(message) || StringUtils.equals(message, "null")) {
			message = "用户或密码错误, 请重试.";
		}
		
		model.addAttribute(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM, username);
		model.addAttribute(FormAuthenticationFilter.DEFAULT_REMEMBER_ME_PARAM, rememberMe);
		model.addAttribute(FormAuthenticationFilter.DEFAULT_MOBILE_PARAM, mobile);
		model.addAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME, exception);
		model.addAttribute(FormAuthenticationFilter.DEFAULT_MESSAGE_PARAM, message);

		if (logger.isDebugEnabled()) {
			logger.debug("login fail, active session size: {}, message: {}, exception: {}",
					sessionDAO.getActiveSessions(false).size(), message, exception);
		}

		// 4.非授权异常，登录失败，验证码加1。
		if (!UnauthorizedException.class.getName().equals(exception)) {
			model.addAttribute("isValidateCodeLogin", isValidateCodeLogin(username, true, false));
		}

		// 验证失败清空验证码
		request.getSession().setAttribute(ValidateCodeServlet.VALIDATE_CODE, IdGen.uuid());

		// 如果是手机登录，则返回JSON字符串
		if (mobile) {
			return renderString(response, model);
		}

		return "modules/sys/sysLogin";
	}

	/**
	 * 登录成功，进入管理首页
	 */
	@RequiresPermissions("user") // 要求具有user权限的才可以执行
	@RequestMapping(value = "${adminPath}")
	public String index(HttpServletRequest request, HttpServletResponse response) {
		//1.获取当前登录者信息
		Principal principal = UserUtils.getPrincipal();
		//2.登录成功后，验证码计算器清零
		isValidateCodeLogin(principal.getLoginName(), false, true);
		//3.日志输出
		if (logger.isDebugEnabled()) {
			logger.debug("show index, active session size: {}", sessionDAO.getActiveSessions(false).size());
		}
		//3.如果已登录，再次访问主页，则退出原账号。需要设置
		if (Global.TRUE.equals(Global.getConfig("notAllowRefreshIndex"))) {
			String logined = CookieUtils.getCookie(request, "LOGINED");
			//2.1 首次登陆时计入内存
			if (StringUtils.isBlank(logined) || "false".equals(logined)) {
				CookieUtils.setCookie(response, "LOGINED", "true");
			} else if (StringUtils.equals(logined, "true")) {
			//2.2已经登陆退出前者账号到登陆界面
				UserUtils.getSubject().logout();
				return "redirect:" + adminPath + "/login";
			}
		}

		// 如果是手机登录，则返回JSON字符串
		if (principal.isMobileLogin()) {
			if (request.getParameter("login") != null) {
				return renderString(response, principal);
			}
			if (request.getParameter("index") != null) {
				return "modules/sys/sysIndex";
			}
			return "redirect:" + adminPath + "/login";
		}

		// 登录成功后，获取上次登录的当前站点ID
//			UserUtils.putCache("siteId",
//			StringUtils.toLong(CookieUtils.getCookie(request, "siteId")));
//
//			System.out.println("==========================a");
//			try {
//		 byte[] bytes =
//		 com.thinkgem.jeesite.common.utils.FileUtils.readFileToByteArray(
//		 com.thinkgem.jeesite.common.utils.FileUtils.getFile("c:\\sxt.dmp"));
//		 UserUtils.getSession().setAttribute("kkk", bytes);
//		 UserUtils.getSession().setAttribute("kkk2", bytes);
//		 } catch (Exception e) {
//		 e.printStackTrace();
//		 }
//		 for (int i=0; i<1000000; i++){
//		UserUtils.getSession().setAttribute("a", "a");
//		 request.getSession().setAttribute("aaa", "aa");
//	 }
//		 System.out.println("==========================b");
		return "modules/sys/sysIndex";
	}

	/**
	 * 主题设置
	 * 
	 * {id}在这个请求的URL里就是个变量，可以使用@PathVariable来获取
	 * @PathVariable和@RequestParam的区别就在于： @RequestParam用来获得静态的URL请求参数； @PathVariable用来获得动态的URL请求入参
	 *  获取主题方案
	 */
	@RequestMapping(value = "/theme/{theme}")
	public String getThemeInCookie(@PathVariable String theme, HttpServletRequest request,
			HttpServletResponse response) {
		if (StringUtils.isNotBlank(theme)) {
			CookieUtils.setCookie(response, "theme", theme);
		} else {
			theme = CookieUtils.getCookie(request, "theme");
		}
		return "redirect:" + request.getParameter("url");
	}

	/**
	 * 是否是验证码登录
	 * 条件：三次登陆fail时会添加验证码
	 * 
	 * @param useruame      用户名
	 * @param isFail    计数加1
	 * @param clean      计数清零
	 * @return True/false
	 */
	@SuppressWarnings("unchecked")
	public static boolean isValidateCodeLogin(String useruame, boolean isFail, boolean clean) {
		//1.登陆fail Map
		Map<String, Integer> loginFailMap = (Map<String,Integer>)CacheUtils.get("loginFailMap");
		if(loginFailMap == null ){
			loginFailMap = Maps.newHashMap();
			CacheUtils.put("loginFailMap", loginFailMap);
		}
		Integer loginFailNum = loginFailMap.get(useruame);
		//(1).为 null
		if(loginFailNum == null){
			loginFailNum=0;
		}
		//(2).fail Num++
		if(isFail){
			loginFailNum ++;
			loginFailMap.put(useruame, loginFailNum);
		}
		//(3).大于三次fail，执行验证码登陆。
		if(clean){
			loginFailMap.remove(useruame);
		}
		return loginFailNum >=3;
		
	
	}
}
