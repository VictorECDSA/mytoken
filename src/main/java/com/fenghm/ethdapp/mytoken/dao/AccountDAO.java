package com.fenghm.ethdapp.mytoken.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<String> list() {
		String sql = "select * from account";
		List<Map<String, Object>> accountList = jdbcTemplate.queryForList(sql);

		List<String> list = new ArrayList<String>();
		for (Map<String, Object> map : accountList) {
			String accountId = (String) map.get("account_id");
			list.add(accountId);
		}
		return list;
	}

	// 插入记录
	public int insert(String accountId, int password) {
		String sql = "insert into account(account_id, password) values(?, ?)";
		return jdbcTemplate.update(sql, accountId, password);
	}

	// 查询账户密码
	public Integer queryPassword(String accountId) {
		String sql = "select * from account where account_id = ?";
		try {
			Map<String, Object> accountMap = jdbcTemplate.queryForMap(sql, accountId);
			return (Integer) accountMap.get("password");
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

}
