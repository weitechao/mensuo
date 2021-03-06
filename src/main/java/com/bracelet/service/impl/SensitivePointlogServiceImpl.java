package com.bracelet.service.impl;

import com.bracelet.entity.SensitivePointLog;
import com.bracelet.service.ISensitivePointlogService;
import com.bracelet.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Service
public class SensitivePointlogServiceImpl implements ISensitivePointlogService {
	@Autowired
	JdbcTemplate jdbcTemplate;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean insert(Long user_id, Long sp_id, String imei, String lat, String lng, Integer radius, String lat1,
			String lng1, Integer status, String content, Timestamp upload_time) {
		Timestamp now = Utils.getCurrentTimestamp();
		int i = jdbcTemplate.update(
				"insert into sensitive_point_log (user_id, sp_id, imei, lat, lng, radius, lat1, lng1, status, content, upload_time, createtime) values (?,?,?,?,?,?,?,?,?,?,?,?)",
				new Object[] { user_id, sp_id, imei, lat, lng, radius, lat1, lng1, status, content, upload_time, now },
				new int[] { Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
						Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.TIMESTAMP, Types.TIMESTAMP });
		return i == 1;
	}

	public List<SensitivePointLog> findAllByCount(Long user_id, Integer status, Integer limit) {
		String whereStatus = "";
		if (status == 1) {
			whereStatus = " and status = 1";
		} else if (status == 2) {
			whereStatus = " and status = 2";
		}
		String sql = "select * from sensitive_point_log where user_id=? " + whereStatus
				+ " order by upload_time desc limit ?";
		List<SensitivePointLog> list = jdbcTemplate.query(sql, new Object[] { user_id, limit },
				new BeanPropertyRowMapper<SensitivePointLog>(SensitivePointLog.class));
		return list;
	}

	public List<SensitivePointLog> findByCount(Long user_id, Long sp_id, Integer status, Integer limit) {
		String whereStatus = "";
		if (status == 1) {
			whereStatus = " and status = 1";
		} else if (status == 2) {
			whereStatus = " and status = 2";
		}
		String sql = "select * from sensitive_point_log where user_id=? and sp_id=? " + whereStatus
				+ " order by upload_time desc limit ?";
		List<SensitivePointLog> list = jdbcTemplate.query(sql, new Object[] { user_id, sp_id, limit },
				new BeanPropertyRowMapper<SensitivePointLog>(SensitivePointLog.class));
		return list;
	}

}
