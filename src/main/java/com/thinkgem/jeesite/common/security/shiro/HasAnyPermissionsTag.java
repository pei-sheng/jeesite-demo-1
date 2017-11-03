/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.common.security.shiro;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.tags.PermissionTag;

/**
 * Shiro HasAnyPermissions Tag.
 *扩展标签，具有列出权限中的任意一个
 * 
 * @author calvin
 */
/**
 * 一个菜单下面有3个子菜单 
|-菜单栏目一 
|——-|–子栏目一 
|——-|–子栏目二

像这样，这时，假设子栏目一显示的条件是hasPermission p1, 子栏目二是p2,那么我们可以认为父级
（菜单栏一，只需要在有p1或者p2的情况下就显示），这时就需要一个 类似hasAnyRole的标签-hasAnyPermssion 
( 当然，实现这种需要还有其他的方法，我暂时没有想到，用shiro 不是很久。。。。)
具体实现时候，实现是一个HasAnyPermissionTag 类，然后把shiro-web.jar 里面的shiro.tld 里面加入自己定义的标签就行
 * @author Json
 *
 */

public class HasAnyPermissionsTag extends PermissionTag {

	private static final long serialVersionUID = 1L;
	private static final String PERMISSION_NAMES_DELIMETER = ",";

	@Override
	protected boolean showTagBody(String permissionNames) {
		boolean hasAnyPermission = false;

		Subject subject = getSubject();

		if (subject != null) {
			// Iterate through permissions and check to see if the user has one of the permissions
			for (String permission : permissionNames.split(PERMISSION_NAMES_DELIMETER)) {

				if (subject.isPermitted(permission.trim())) {
					hasAnyPermission = true;
					break;
				}

			}
		}

		return hasAnyPermission;
	}

}
