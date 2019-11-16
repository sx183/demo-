package com.atguigu.scw.user.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsTemplate implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// @Value("${sms.host}")
	String host;
	// @Value("${sms.path}")
	String path;
	// @Value("${sms.method}")
	String method;
	// @Value("${sms.appcode}")
	String appcode;

	public boolean sendSms(String phoneNum, String code, String template) {
		System.out.println("host:" + host);
		Map<String, String> headers = new HashMap<String, String>();
		// 最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
		headers.put("Authorization", "APPCODE " + appcode);
		Map<String, String> querys = new HashMap<String, String>();
		querys.put("mobile", phoneNum);
		querys.put("param", "code:" + code);
		querys.put("tpl_id", template);
		Map<String, String> bodys = new HashMap<String, String>();

		try {
			/**
			 * 重要提示如下: HttpUtils请从
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
			 * 下载
			 *
			 * 相应的依赖请参照
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
			 */
			HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
			System.out.println(response.toString());

			// 获取response的body

			// System.out.println(EntityUtils.toString(response.getEntity()));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
