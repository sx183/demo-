package com.atguigu.scw.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


@Api(tags="测试Swagger的Controller")
@RestController   //处理请求返回json
public class HelloController {
		@RequestMapping("/hello")
		@ApiOperation(value="测试swagger方法")
		@ApiImplicitParams(value= {
				@ApiImplicitParam(required=true,name="username"),
				@ApiImplicitParam(dataType="Integer",required=false,name="password")
		})
		public String Hello(String username,String password) {
			
			return  "helloWorld";
		}

}
