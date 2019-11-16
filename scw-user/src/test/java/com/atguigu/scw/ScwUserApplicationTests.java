package com.atguigu.scw;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.atguigu.scw.user.utils.SmsTemplate;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBootTest
public class ScwUserApplicationTests {
	// 测试druid连接池 自动装配+redis 模板类对象

	@Autowired
	DataSource dataSource;
	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Autowired
	RedisTemplate<Object, Object> redisTemplate;

	Logger logger = LoggerFactory.getLogger(getClass());
	@Test
	public void contextLoads() {
		logger.debug("stringRedisTemplate：{}",stringRedisTemplate);
		logger.debug("数据库连接池：{}",dataSource);
		logger.debug("redisTemplate：{}",redisTemplate);
		
		Boolean key = stringRedisTemplate.hasKey("k1");
		logger.debug("redids中是否包含k1:{}",key);
		
		stringRedisTemplate.opsForValue().set("k1", "vale",1000, TimeUnit.SECONDS);
		
		Long expire = stringRedisTemplate.getExpire("k1",TimeUnit.MINUTES);
		String val = stringRedisTemplate.opsForValue().get("k1");
		
		logger.debug("redis中k1的过期时间：{}",expire);
		logger.debug("redis中k1的值：{}",val);
		
		stringRedisTemplate.delete("k1");
		
		
		
		
		
	}
	
	/*@Test
	public void testSms() {
		 String host = "http://dingxin.market.alicloudapi.com";
		    String path = "/dx/sendSms";
		    String method = "POST";
		    String appcode = "967fc3ed49bd46bdb15d3d6ea5049fb5";
		    Map<String, String> headers = new HashMap<String, String>();
		    //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
		    headers.put("Authorization", "APPCODE " + appcode);
		    Map<String, String> querys = new HashMap<String, String>();
		    querys.put("mobile", "18373942717");
		    querys.put("param", "code:longqingfengshizhu");
		    querys.put("tpl_id", "TP1711063");
		    Map<String, String> bodys = new HashMap<String, String>();


		    try {
		    	*//**
		    	* 重要提示如下:
		    	* HttpUtils请从
		    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
		    	* 下载
		    	*
		    	* 相应的依赖请参照
		    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
		    	*//*
		    	HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
		    	System.out.println(response.toString());
		    	 HttpEntity entity = response.getEntity();
		    	 InputStream content = entity.getContent();
		    	 byte [] b=new byte[content.available()];
		    	 content.read(b);
		    	 String str = new String(b);
		    	 System.out.println(str);
		    	//获取response的body
		    	System.out.println(EntityUtils.toString(response.getEntity()));
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		
	}*/
	
	@Autowired
	SmsTemplate smsTemplate;
	@Test
	public void testSms() {
		String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
		boolean b = smsTemplate.sendSms("18373942717", uuid, "TP1711063");
		log.debug("发送短信验证码：{},成功:{}",uuid,b);
	}

}
