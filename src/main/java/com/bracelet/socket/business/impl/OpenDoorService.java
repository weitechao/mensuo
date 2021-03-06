package com.bracelet.socket.business.impl;

import io.netty.channel.Channel;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bracelet.dto.FingerDto;
import com.bracelet.dto.OpenDoorDto;
import com.bracelet.dto.SocketBaseDto;
import com.bracelet.dto.SocketLoginDto;
import com.bracelet.entity.BindDevice;
import com.bracelet.entity.MemberInfo;
import com.bracelet.entity.NotRegisterInfo;
import com.bracelet.entity.NoticeInfo;
import com.bracelet.entity.UserInfo;
import com.bracelet.exception.BizException;
import com.bracelet.util.PushUtil;
import com.bracelet.util.RespCode;
import com.bracelet.util.SmsUtil;
import com.bracelet.service.IMemService;
import com.bracelet.service.IOpenDoorService;
import com.bracelet.service.IPushlogService;
import com.bracelet.service.ITokenInfoService;
import com.bracelet.service.IUserInfoService;
import com.bracelet.service.IVoltageService;
import com.bracelet.socket.business.IService;
import com.bracelet.util.ChannelMap;
import com.taobao.api.ApiException;

@Component("opendoorService")
public class OpenDoorService implements IService {
	@Autowired
	IOpenDoorService opendoorService;

	@Autowired
	IUserInfoService userInfoService;
	@Autowired
	IVoltageService voltageService;
	@Autowired
	ITokenInfoService tokenInfoService;

	@Autowired
	IMemService memService;

	@Autowired
	IPushlogService pushlogService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public SocketBaseDto process(JSONObject jsonObject, Channel channel) {
		logger.info("===开门记录：" + jsonObject.toJSONString());
		JSONObject jsonObject2 = (JSONObject) jsonObject.get("data");
		if (jsonObject2 == null) {
			throw new BizException(RespCode.NOTEXIST_PARAM);
		}
		Long userid = jsonObject2.getLong("userid");
		Integer way = jsonObject2.getInteger("way");
		Integer side = jsonObject2.getInteger("side");
		String no = jsonObject.getString("no");
		String imei = jsonObject.getString("imei");
		int isadmin = jsonObject2.getInteger("isadmin");
		
		  long timestamp = jsonObject.getLongValue("timestamp");

		Integer voltage = jsonObject2.getInteger("battery_percent");

		voltageService.insertDianLiang(imei, voltage);

		// String name = "";
		Integer register = jsonObject2.getInteger("register");// 0未注册 1注册
		UserInfo userinfo = userInfoService.getUserInfoById(userid);
		/*
		 * if (userinfo != null) { name = userinfo.getNickname(); }
		 */
	

		BindDevice bind = userInfoService.getBindInfoByImeiAndStatus(imei, 1);
		if (bind != null) {
			Long user_id = bind.getUser_id();
			// 开锁方式 0:门把开锁 1:APP开锁,2:指纹开锁,3:密码开锁
			String wayString = "门把";
			if (way == 1) {
				wayString = "APP";
			} else if (way == 2) {
				wayString = "指纹";
			} else if (way == 3) {
				wayString = "密码";
			}

			String sideString = side == 1 ? "门里" : "门外";

			String notifyContent = "门锁" + bind.getName();

		/*	if (isadmin == 1) {*/
				if (userinfo.getNickname() != null
						&& !"".equals(userinfo.getNickname())) {
					notifyContent = notifyContent + "被名字叫"
							+ userinfo.getNickname() + "的使用" + wayString + "在"
							+ sideString + "打开!";
					opendoorService.insert(1, userid, way, side, imei,
							userinfo.getNickname(),new Timestamp(timestamp * 1000));
				} else {
					notifyContent = notifyContent + "被手机号为"
							+ userinfo.getUsername() + "使用" + wayString + "在"
							+ sideString + "打开!";
					opendoorService.insert(1, userid, way, side, imei,
							userinfo.getUsername(),new Timestamp(timestamp * 1000));
				}
		/*	} else {
				MemberInfo member = memService.getMemberInfo(
						userinfo.getUsername(), imei);
				if (member.getName() != null && !"".equals(member.getName())) {
					notifyContent = notifyContent + "被名字叫" + member.getName()
							+ "的使用" + wayString + "在" + sideString + "打开!";
					opendoorService.insert(1, userid, way, side, imei,
							member.getName());
				} else {
					notifyContent = notifyContent + "被手机号为"
							+ userinfo.getUsername() + "使用" + wayString + "在"
							+ sideString + "打开!";
					opendoorService.insert(1, userid, way, side, imei,
							userinfo.getUsername());
				}
			}*/

			OpenDoorDto sosDto = new OpenDoorDto();
			sosDto.setName(userinfo.getNickname());
			sosDto.setImei(imei);
			sosDto.setTimestamp(new Date().getTime());
			sosDto.setSide(side);
			sosDto.setWay(way);
			sosDto.setContent(notifyContent);
			String target = tokenInfoService.getTokenByUserId(user_id);
			String title = "开锁";
			String content = JSON.toJSONString(sosDto);
			
			NoticeInfo vinfo = userInfoService.getNoticeSet(user_id);
			if (vinfo == null ||  vinfo.getMemberunlockswitch() == 1 ) {
			PushUtil.push(target, title, content, notifyContent);
			// save push log
			this.pushlogService
					.insert(user_id, imei, 0, target, title, content);
			}
		}

		SocketBaseDto dto = new SocketBaseDto();
		dto.setType(jsonObject.getIntValue("type"));
		dto.setNo(no);
		dto.setTimestamp(new Date().getTime());
		dto.setStatus(0);

		return dto;

	}
}
