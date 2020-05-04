package com.fenghm.ethdapp.mytoken.dao;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MyTokenBalanceDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// 插入记录
	public int insert(String myTokenId, String accountId, BigInteger balance) {
		String sql = "insert into my_token_balance(my_token_id, account_id, balance) values(?, ?, ?)";
		return jdbcTemplate.update(sql, myTokenId, accountId, balance);
	}

	// 查询账户余额
	public BigInteger queryBalance(String myTokenId, String accountId) {
		String sql = "select * from my_token_balance where my_token_id = ? and account_id = ?";
		try {
			Map<String, Object> map = jdbcTemplate.queryForMap(sql, myTokenId, accountId);
			return (BigInteger) map.get("balance");
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	// 更新账户余额
	public int update(String myTokenId, String accountId, BigInteger balance) {
		String sql = "update my_token_balance set balance = ? where my_token_id = ? and account_id = ?";
		return jdbcTemplate.update(sql, balance, myTokenId, accountId);
	}

}
