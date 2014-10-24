package ext.tmt.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.infoengine.util.Base64;

import ext.tmt.utils.Debug;

import wt.auth.AuthenticationServer;
import wt.httpgw.HTTPGatewayServlet;
import com.ptc.mvc.components.support.ComponentXmlWebApplicationContext;
import com.ptc.mvc.components.support.ComponentBeanDefinitionDocumentReader;
import com.ptc.mvc.components.support.DefaultComponentBuilderResolver;
import com.ptc.mvc.components.support.ComponentBuilderBeanNameGenerator;
import com.ptc.mvc.components.support.ComponentBuilderBeanPostProcessor;

public class MyAuthHttpServlet extends HTTPGatewayServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void serviceWithoutFilters(HttpServletRequest httpservletrequest,
			HttpServletResponse httpservletresponse) throws ServletException,IOException {
		System.out.println("-----------------------------I'm in MyHttpAuthGatewayServlet--------------------");
		String userName="";
		String passWord="";
		Debug.P(httpservletrequest.getSession().getAttribute(""));
		Debug.P(httpservletrequest.getSession().getAttribute(""));
		userName=httpservletrequest.getParameter("userName");
		passWord=httpservletrequest.getParameter("passWord");
		
		Debug.P("userName--------------->"+userName);
		Debug.P("passWord---------------->"+passWord);
		httpservletresponse.setHeader("Authorization", "Basic " + Base64.encode(userName+":"+passWord));
		AuthenticationServer.setUserName(userName);

		super.serviceWithoutFilters(httpservletrequest, httpservletresponse);
	}
}
