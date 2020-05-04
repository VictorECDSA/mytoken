package com.fenghm.ethdapp.mytoken.dao;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MyTokenDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// 插入记录
	public int insert(String myTokenId) {
		String sql = "insert into my_token(my_token_id) values(?)";
		return jdbcTemplate.update(sql, myTokenId);
	}

	// 查询代码是否存在
	public String query(String myTokenId) {
		String sql = "select * from my_token where my_token_id = ?";
		try {
			Map<String, Object> myTokenMap = jdbcTemplate.queryForMap(sql, myTokenId);
			return (String) myTokenMap.get("my_token_id");
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

}
