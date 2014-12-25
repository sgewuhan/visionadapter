package com.sg.visionconnector;

import java.net.MalformedURLException;
import java.net.URL;

import com.sg.ucl.wsclient.Exception_Exception;
import com.sg.ucl.wsclient.UCL;
import com.sg.ucl.wsclient.UCLService;

public class TestUCLWS {

	public static void main(String[] args) throws MalformedURLException {
		UCLService service = new UCLService(new URL(
				"http://127.0.0.1:81/visrv/uclportal?wsdl"));
		UCL ucl = service.getUCLPort();
		try {
			// 注册应用
			String appid = "vis-ext01";
			String appAuthCode = ucl.registerApp(appid, "Vision 移动客户端",
					"1.0", "SG Co., Ltd.", "ghuazh@gmail.com");

			// 获得token
			String token1 = ucl.getToken("zhonghua", "1", appid, appAuthCode,"example");
			System.out.println(token1);
			// 测试token
			String result = ucl.ping(appid, appAuthCode, token1);
			System.out.println(result);

		} catch (Exception_Exception e) {
			e.printStackTrace();
		}
	}
}
