package com.atguigu.scw.user.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.atguigu.scw.common.consts.UserAppConsts;
import com.atguigu.scw.common.exception.MyException;
import com.atguigu.scw.common.vo.response.UserResponseVo;
import com.atguigu.scw.user.bean.TMember;
import com.atguigu.scw.user.bean.TMemberAddress;
import com.atguigu.scw.user.bean.TMemberAddressExample;
import com.atguigu.scw.user.bean.TMemberExample;
import com.atguigu.scw.user.mapper.TMemberAddressMapper;
import com.atguigu.scw.user.mapper.TMemberMapper;
import com.atguigu.scw.user.service.UserService;
import com.atguigu.scw.user.utils.ScwUserAppUtils;
import com.atguigu.scw.user.vo.request.UserRegistVo;


import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class UserServiceImpl implements UserService {
	@Autowired
	BCryptPasswordEncoder encoder;
	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Autowired
	TMemberMapper memberMapper;
	
	@Autowired
	TMemberAddressMapper memberAddressMapper;

	@Override
	public void saveUser(UserRegistVo vo) {
		// 1判断验证码是否失效，是否正确
		String phoneCodeKey = UserAppConsts.PHONE_CODE_PREFIX + vo.getLoginacct() + UserAppConsts.PHONE_CODE_SUFFIX;
		String redisPhoneCode = stringRedisTemplate.opsForValue().get(phoneCodeKey);
		if (StringUtils.isEmpty(vo.getCode())) {
			throw new MyException("请输出验证码 ");

		}
		Boolean flag = stringRedisTemplate.hasKey(phoneCodeKey);
		if (!flag) {
			// redis中的验证码已经失效
			throw new MyException("验证码过期");

		}
		if (StringUtils.isEmpty(vo.getCode()) || !vo.getCode().equalsIgnoreCase(redisPhoneCode)) {
			throw new MyException("验证码错误");
		}
		// 2将UserRegistVo转为TMember对象，并初始化默认值
		TMember member = new TMember();
		// 参数1：数据源，参数2：目标对象 ，方法作用：按照vo的属性名和类型去目标对象中完全匹配，如果找到对应的属性，则调用其set方法设置属性值
		BeanUtils.copyProperties(vo, member);
		// 数据库表中 member的 username authtype需要默认值，需要初始化
		member.setUsername(member.getLoginacct());
		// 实名认证状态 0 - 未实名认证， 1 - 实名认证申请中， 2 - 已实名认证
		member.setAuthstatus("0");
		// 3调用mapper将数据存到数据库中
		TMemberExample example = new TMemberExample();
		example.createCriteria().andLoginacctEqualTo(member.getLoginacct());
		long l = memberMapper.countByExample(example);
		if (l > 0) {
			// 用户被占用
			throw new MyException("用户名被占用");
		}
		example.clear();
		example.createCriteria().andEmailEqualTo(member.getEmail());
		l = memberMapper.countByExample(example);
		if (l > 0) {
			throw new MyException("邮箱已被占用");

		}

		// 3.1验证账号和邮箱的唯一性
		// 3.2保存
		member.setUserpswd(encoder.encode(member.getUserpswd()));
		memberMapper.insertSelective(member);
		// 删除redis中该手机号码使用过的验证码
		stringRedisTemplate.delete(phoneCodeKey);

	}

	@Override
	public UserResponseVo dologin(String loginacct, String userpswd) {
		TMemberExample example = new TMemberExample();
		example.createCriteria().andLoginacctEqualTo(loginacct);
		List<TMember> list = memberMapper.selectByExample(example);
		if (CollectionUtils.isEmpty(list)|| list.size() > 1) {
			throw new MyException("账号不存在");
		}
		TMember member = list.get(0);
		//账号存在
		//验证密码 
		//参数1：明文密码，参数2：数据库查询的加密过的密码
		boolean flag = encoder.matches(userpswd, member.getUserpswd());
		if (!flag) {
			//两次密码不一样 
			throw new MyException("密码错误");
		}
		//将member转为vo
		UserResponseVo vo = new UserResponseVo();
		BeanUtils.copyProperties(member, vo);
		//创建唯一token
		String token =UUID.randomUUID().toString().replace("-", "");
		token= UserAppConsts.USER_LOGIN_TOKEN_PREFIX+token;
		vo.setAccesstoken(token);
		//将登录成功的信息封存到redis中
		stringRedisTemplate.opsForValue().set(token, ScwUserAppUtils.objToJsonStr(vo),7,TimeUnit.DAYS);   
		log.debug("登录查询到的vo对象：{}",vo);
		return vo;
	}

	@Override
	public List<TMemberAddress> getAddress(Integer id) {
		
		TMemberAddressExample example= new TMemberAddressExample();
		example.createCriteria().andMemberidEqualTo(id);
		return  memberAddressMapper.selectByExample(example);
	}

}
