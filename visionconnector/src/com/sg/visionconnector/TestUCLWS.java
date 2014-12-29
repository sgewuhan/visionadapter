package com.sg.visionconnector;

import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sg.ucl.wsclient.UCL;
import com.sg.ucl.wsclient.UCLService;

public class TestUCLWS {

	public static void main(String[] args) throws Exception {
		/**************************************************************
		 * 首先，您需要注册一个Vision授权连接的外部引用
		 */
		// 1. 获得UCL服务
		UCLService service = new UCLService(new URL(
				"http://127.0.0.1:81/visrv/uclportal?wsdl"));
		UCL ucl = service.getUCLPort();
		// 2. 注册一个外部应用
		String appid = "vis-ext01";
		String appname = "Vision 移动客户端";
		String version = "1.0";
		String appprovider = "SG Co., Ltd.";
		String email = "zhonghua@yaozheng.com.cn";
		String appAuthCode = ucl.registerApp(appid, appname, version,
				appprovider, email);
		// appAuthCode是该应用的授权码，在以后的使用中，都需要appid和授权码联合使用。
		// 该appid对应的授权码不会改变，除非调用重设授权码的web 服务

		/**************************************************************
		 * 在调用Vision服务时，分为两步第一步获得该调用的token
		 */
		// 1. 获得token
		String visionUserName = "zhonghua";
		String visionPassword = "1";
		// 注意，vision的用户验证支持使用域验证或非域验证，不同的用户可能设置不同。
		// 此处的用户密码遵循该用户在Vision中设置的验证方式。
		String applicationData = "example";
		String token = ucl.getToken(visionUserName, visionPassword, appid,
				appAuthCode, applicationData);
		System.out.println("您得到的token为:" + token);
		// 2. 该token将返回给您一个字符串，您可以调用以下web服务测试您的token
		String result = ucl.ping(appid, appAuthCode, token);
		System.out.println("token 内容包括：" + result);

		// 3. 使用以下的地址调用Vision的ucl链接
		System.out.println("您可以使用后面的地址进行访问: http://<vision的web地址>/direct?link="
				+ token);

		/**************************************************************
		 * 请记住您的app授权码，如果忘记该授权码，可调用以下的服务重设
		 */
		// ucl.sendResetAppAuthCodeEmail(appid);
		System.out.println("发动邮件需要Vision-CDF WEB服务启动,请在正式运行环境下解除以上的注释");

		/**************************************************************
		 * 传递复杂的appData 您可以使用JSON向ucl传递复杂的对象,如果您输入的字符串无法转换为JSON,那么他将按字符串生成token
		 */
		applicationData = "{api:\"getPartDocument\",partnumber:\"TX0001\",booleanTypeDemo:true,numberTypeDemo:100.09,arryTypeDemo:[1,\"2\"]}";
		token = ucl.getToken(visionUserName, visionPassword, appid,
				appAuthCode, applicationData);
		System.out.println("您得到的token为:" + token);
		result = ucl.ping(appid, appAuthCode, token);
		System.out.println("token 内容包括：" + result);

		/**************************************************************
		 * 以下的例子用于获得直访零部件关联文档的链接 applicationData 包括以下的字段：
		 * 
		 * 
		 * 目前可以用的类型包括以下： "技术方案" "客户资料" "立项资料" "产品设计" "工艺设计" "变更文档" "质量管理" "产品试制"
		 * "检验文档" "阶段评审" "售后服务" "文献资料" "变更通知单"
		 * 
		 * 您可以使用Json帮助转换字符串
		 */

		JSONObject query = new JSONObject();
		query.put("api", "plmRelativeQuery");
		// 零部件过滤
		query.put(IPLM_Object.F_PARTNUMBER, "TX_DEMO");
		// 多个可采用如下方式：
		// query.append(IPLM_Object.F_PARTNUMBER, new
		// JSONArray().put("TX_DEMO").put("TX_DEMO1")));
		query.put(
				"toType",
				new JSONArray().put(IPLM_Object.TYPE_CAD).put(
						IPLM_Object.TYPE_DOCUMENT));
		// 过滤类型
		query.put("filter", new JSONObject().accumulate(
				IPLM_Object.F_DOCUMENT_TYPE, new String[] { "产品设计", "工艺设计" }));
		applicationData = query.toString();
		token = ucl.getToken(visionUserName, visionPassword, appid,
				appAuthCode, applicationData);
		System.out.println("您得到的token为:" + token);
		// 2. 该token将返回给您一个字符串，您可以调用以下web服务测试您的token
		result = ucl.ping(appid, appAuthCode, token);
		System.out.println("token 内容包括：" + result);
		System.out.println("您可以使用后面的地址进行访问: http://<vision的web地址>/direct?link="
				+ token);

		/**************************************************************
		 * (并非必须的步骤)token在使用后自动失效，但是您还是可以手工移除该token。
		 */
		// ucl.removeToken(token, appid, appAuthCode);
		// System.out.println(token + "已被移除");

		/**************************************************************
		 * (并非必须的步骤)如果您无需再使用Vision的UCL连接，可注销您的应用，无论app是否注销，
		 * 您调用的token记录均不会清除。如果您先注销了应用， token将无法手工删除，当然您也无需清除token
		 */
		// ucl.unRegisterApp(appid, appAuthCode);
		// System.out.println(appid + "已被注销");

	}
}
