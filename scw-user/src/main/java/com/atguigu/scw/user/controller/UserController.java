package com.atguigu.scw.user.controller;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.scw.common.bean.ResponseVo;
import com.atguigu.scw.common.utils.ScwAppUtils;
import com.atguigu.scw.common.vo.response.UserResponseVo;
import com.atguigu.scw.user.bean.TMemberAddress;
import com.atguigu.scw.user.service.UserService;
import com.atguigu.scw.user.utils.SmsTemplate;
import com.atguigu.scw.user.vo.request.UserRegistVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

//处理用户登录注册 发送短信验证码等请求
@Api(tags = "处理用户登录注册 发送短信验证码等请求 ")
@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	SmsTemplate smsTemplate;
	@Autowired 
	StringRedisTemplate  stringRedisTemplate;
	@Autowired
	UserService  userService;
	
	
	// 处理发送短信验证码的请求
	
	
	@ApiOperation(value="注册时获取验证码的方法")
	@ApiImplicitParams(value= {
			
			@ApiImplicitParam(name="phoneNum",value="手机号码 ")
			
	})
	@GetMapping("/sendSms")
	public String sendSms(String phoneNum) {
		// 判断手机号码是否正确
		boolean flag =true ;// ScwUserAppUtils.isphone(phoneNum);
		if (!flag) {
			return "手机号码格式错误";
		}

		// 2判断手机号码24小时内申请的次数是否超过三次[使用redis保存 ，第一次访问 没有 ]
		//2.1拼接 手机号码 保存对应次数 存在redis中的键
		 String phoneCountKey="phone:code:"+phoneNum+":count"; 
		 flag = stringRedisTemplate.hasKey(phoneCountKey);
		 int count =0;
		 if(flag) {
			 //检查之前获取过验证码
			String str = stringRedisTemplate.opsForValue().get(phoneCountKey);
			count = Integer.parseInt(str);
			if(count>3) {			
				return "该手机号码获取验证码过多";
			}
		 }
		//3、判断手机号码是否存在未使用的验证码
		 //拼接该手机号码存储验证码的key
		 String phoneCodeKey="phone:code"+phoneNum+":code";
		 flag =stringRedisTemplate.hasKey(phoneCodeKey);
		 if(flag) {
			 //存在未使用的验证码
			 return "获取的验证码过于频繁";
			 
			 
		 }
		 
		// 4，生成6位验证码
		 String code=UUID.randomUUID().toString().replace("-", "").substring(0, 6);
		// 5发送
		 flag=smsTemplate.sendSms(phoneNum,code,"TP1711063");
		 if(!flag) {
			 //短信发送失败
			 return "短信发送失败，请稍后再试";
		 }
		// 6发送成功 需要将手机号码 和对应的验证码 保存十分钟
		stringRedisTemplate.opsForValue().set(phoneCodeKey, code, 15, TimeUnit.MINUTES);
		// 7更新 当前手机号码 24小时内获取验证码的次数
		if (count==0) {
			//第一次记录次数
			stringRedisTemplate.opsForValue().set(phoneCountKey,"1",24,TimeUnit.HOURS);
			
		}else {
			count++;
			stringRedisTemplate.opsForValue().increment(phoneCountKey);//在之前的基础之上自增不会覆盖
		}
				
				
		// 给出返回结果
		return "发送 验证码成功";

	}
	
	//登录方法  
	@ApiOperation(value="登录方法")
	@PostMapping("/doLogin")
	public ResponseVo <UserResponseVo> doLogin(@RequestParam("loginacct")String loginacct,@RequestParam("userpswd")String userpswd){
		
		//调用业务层处理登录业务
		UserResponseVo vo;
		  try {
			vo =userService.dologin(loginacct,userpswd);
			  //响应
			   return ResponseVo.ok(vo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ResponseVo.fail(e.getMessage());
		}
		
		  
		
	}
	
	
	//处理注册请求的方法
	@ApiOperation(value="注册方法")
	@PostMapping("/doRegist")
	public ResponseVo<Object> doRegist(UserRegistVo vo) {
		//1接收参数  
		
		//调用业务层处理业务 
		try {
			userService.saveUser(vo);
			//3给调用者响应
			//{code:200,message:"success",data:Object}
			//{code:10001,message:"失败原因"}
			//封装响应类：responseVo
			
			return ResponseVo.ok("注册成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return  ResponseVo.fail(e.getMessage());
		}
		//给调用者响应 
		
	}
	
	
	//查询用户的的地址集合
	
	@ApiOperation("查询用户地址列表")
	@GetMapping("/getAddress")
	public ResponseVo<List< TMemberAddress>> getAddresses( @RequestParam("accessToken")String accessToken){
		//根据token去redis中获取用户信息
		 UserResponseVo vo = ScwAppUtils.getObjFromRedis(stringRedisTemplate, UserResponseVo.class, accessToken);
		if (vo==null) {
			return  ResponseVo.fail("登录超时");
		}
		Integer id = vo.getId();
		 List<TMemberAddress> list =userService.getAddress(id);
		 
		 return ResponseVo.ok(list);
		
	}
	
	
}
