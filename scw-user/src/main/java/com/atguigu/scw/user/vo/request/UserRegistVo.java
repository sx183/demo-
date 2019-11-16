package com.atguigu.scw.user.vo.request;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserRegistVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String loginacct;// 手机号码
	private String userpswd;
	private String email;
	private String code;
	private String usertype;// 用户数据类型： 0 个人 1 代表企业

}
