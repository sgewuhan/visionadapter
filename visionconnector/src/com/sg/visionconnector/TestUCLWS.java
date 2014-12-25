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
			// ע��Ӧ��
			String appid = "vis-ext01";
			String appAuthCode = ucl.registerApp(appid, "Vision �ƶ��ͻ���",
					"1.0", "SG Co., Ltd.", "ghuazh@gmail.com");

			// ���token
			String token1 = ucl.getToken("zhonghua", "1", appid, appAuthCode,"example");
			System.out.println(token1);
			// ����token
			String result = ucl.ping(appid, appAuthCode, token1);
			System.out.println(result);

		} catch (Exception_Exception e) {
			e.printStackTrace();
		}
	}
}
