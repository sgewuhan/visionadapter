package com.sg.visionconnector;

import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sg.ucl.wsclient.UCL;
import com.sg.ucl.wsclient.UCLService;

public class TestUCLWS {

	public static void main(String[] args) throws Exception {
		/**************************************************************
		 * ���ȣ�����Ҫע��һ��Vision��Ȩ���ӵ��ⲿ����
		 */
		// 1. ���UCL����
		UCLService service = new UCLService(new URL(
				"http://127.0.0.1:81/visrv/uclportal?wsdl"));
		UCL ucl = service.getUCLPort();
		// 2. ע��һ���ⲿӦ��
		String appid = "vis-ext01";
		String appname = "Vision �ƶ��ͻ���";
		String version = "1.0";
		String appprovider = "SG Co., Ltd.";
		String email = "zhonghua@yaozheng.com.cn";
		String appAuthCode = ucl.registerApp(appid, appname, version,
				appprovider, email);
		// appAuthCode�Ǹ�Ӧ�õ���Ȩ�룬���Ժ��ʹ���У�����Ҫappid����Ȩ������ʹ�á�
		// ��appid��Ӧ����Ȩ�벻��ı䣬���ǵ���������Ȩ���web ����

		/**************************************************************
		 * �ڵ���Vision����ʱ����Ϊ������һ����øõ��õ�token
		 */
		// 1. ���token
		String visionUserName = "zhonghua";
		String visionPassword = "1";
		// ע�⣬vision���û���֤֧��ʹ������֤�������֤����ͬ���û��������ò�ͬ��
		// �˴����û�������ѭ���û���Vision�����õ���֤��ʽ��
		String applicationData = "example";
		String token = ucl.getToken(visionUserName, visionPassword, appid,
				appAuthCode, applicationData);
		System.out.println("���õ���tokenΪ:" + token);
		// 2. ��token�����ظ���һ���ַ����������Ե�������web�����������token
		String result = ucl.ping(appid, appAuthCode, token);
		System.out.println("token ���ݰ�����" + result);

		// 3. ʹ�����µĵ�ַ����Vision��ucl����
		System.out.println("������ʹ�ú���ĵ�ַ���з���: http://<vision��web��ַ>/direct?link="
				+ token);

		/**************************************************************
		 * ���ס����app��Ȩ�룬������Ǹ���Ȩ�룬�ɵ������µķ�������
		 */
		// ucl.sendResetAppAuthCodeEmail(appid);
		System.out.println("�����ʼ���ҪVision-CDF WEB��������,������ʽ���л����½�����ϵ�ע��");

		/**************************************************************
		 * ���ݸ��ӵ�appData ������ʹ��JSON��ucl���ݸ��ӵĶ���,�����������ַ����޷�ת��ΪJSON,��ô�������ַ�������token
		 */
		applicationData = "{api:\"getPartDocument\",partnumber:\"TX0001\",booleanTypeDemo:true,numberTypeDemo:100.09,arryTypeDemo:[1,\"2\"]}";
		token = ucl.getToken(visionUserName, visionPassword, appid,
				appAuthCode, applicationData);
		System.out.println("���õ���tokenΪ:" + token);
		result = ucl.ping(appid, appAuthCode, token);
		System.out.println("token ���ݰ�����" + result);

		/**************************************************************
		 * ���µ��������ڻ��ֱ���㲿�������ĵ������� applicationData �������µ��ֶΣ�
		 * 
		 * 
		 * Ŀǰ�����õ����Ͱ������£� "��������" "�ͻ�����" "��������" "��Ʒ���" "�������" "����ĵ�" "��������" "��Ʒ����"
		 * "�����ĵ�" "�׶�����" "�ۺ����" "��������" "���֪ͨ��"
		 * 
		 * ������ʹ��Json����ת���ַ���
		 */

		JSONObject query = new JSONObject();
		query.put("api", "plmRelativeQuery");
		// �㲿������
		query.put(IPLM_Object.F_PARTNUMBER, "TX_DEMO");
		// ����ɲ������·�ʽ��
		// query.append(IPLM_Object.F_PARTNUMBER, new
		// JSONArray().put("TX_DEMO").put("TX_DEMO1")));
		query.put(
				"toType",
				new JSONArray().put(IPLM_Object.TYPE_CAD).put(
						IPLM_Object.TYPE_DOCUMENT));
		// ��������
		query.put("filter", new JSONObject().accumulate(
				IPLM_Object.F_DOCUMENT_TYPE, new String[] { "��Ʒ���", "�������" }));
		applicationData = query.toString();
		token = ucl.getToken(visionUserName, visionPassword, appid,
				appAuthCode, applicationData);
		System.out.println("���õ���tokenΪ:" + token);
		// 2. ��token�����ظ���һ���ַ����������Ե�������web�����������token
		result = ucl.ping(appid, appAuthCode, token);
		System.out.println("token ���ݰ�����" + result);
		System.out.println("������ʹ�ú���ĵ�ַ���з���: http://<vision��web��ַ>/direct?link="
				+ token);

		/**************************************************************
		 * (���Ǳ���Ĳ���)token��ʹ�ú��Զ�ʧЧ�����������ǿ����ֹ��Ƴ���token��
		 */
		// ucl.removeToken(token, appid, appAuthCode);
		// System.out.println(token + "�ѱ��Ƴ�");

		/**************************************************************
		 * (���Ǳ���Ĳ���)�����������ʹ��Vision��UCL���ӣ���ע������Ӧ�ã�����app�Ƿ�ע����
		 * �����õ�token��¼������������������ע����Ӧ�ã� token���޷��ֹ�ɾ������Ȼ��Ҳ�������token
		 */
		// ucl.unRegisterApp(appid, appAuthCode);
		// System.out.println(appid + "�ѱ�ע��");

	}
}
