package com.fenghm.ethdapp.mytoken.service;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fenghm.ethdapp.mytoken.common.Tool;
import com.fenghm.ethdapp.mytoken.dao.AccountDAO;
import com.fenghm.ethdapp.mytoken.dao.MyTokenBalanceDAO;
import com.fenghm.ethdapp.mytoken.dao.MyTokenDAO;

@Service
public class MyTokenService {

	@Autowired
	AccountDAO accountDAO;

	@Autowired
	MyTokenDAO myTokenDAO;

	@Autowired
	MyTokenBalanceDAO myTokenBalanceDAO;

	// 发布代币
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String deploy(String accountId, String password, BigInteger balance) {
		// 校验账户和密码
		Integer pw = accountDAO.queryPassword(accountId);
		if (pw == null) {
			throw new RuntimeException("account not found");
		}
		if (pw != password.hashCode()) {
			throw new RuntimeException("incorrect password");
		}

		// 生成以“0x”为前缀的40个16进制字符的随机代币ID
		String myTokenId = Tool.randomHexString(40);

		// 插入代币记录
		int result0 = myTokenDAO.insert(myTokenId);
		if (result0 != 1) {
			throw new RuntimeException("insert myToken error");
		}

		// 插入代币余额记录
		int result1 = myTokenBalanceDAO.insert(myTokenId, accountId, balance);
		if (result1 != 1) {
			throw new RuntimeException("insert myTokenBalance error");
		}

		return myTokenId;
	}

	// 转账代币
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public BigInteger transfer(String accountId, String password, String myTokenId, String to, BigInteger value) {
		// 校验账户和密码
		Integer pw = accountDAO.queryPassword(accountId);
		if (pw == null) {
			throw new RuntimeException("account not found");
		}
		if (pw != password.hashCode()) {
			throw new RuntimeException("incorrect password");
		}

		// 校验代币是否存在
		String result0 = myTokenDAO.query(myTokenId);
		if (result0 == null) {
			throw new RuntimeException("myToken not found");
		}

		// 校验账户余额是否充足
		BigInteger fromBalance = myTokenBalanceDAO.queryBalance(myTokenId, accountId);
		if (fromBalance == null) {
			throw new RuntimeException("account ID not found");
		}
		if (fromBalance.compareTo(value) < 0) {
			throw new RuntimeException("balance not sufficient");
		}
		// 账户转出转账额
		fromBalance = fromBalance.subtract(value);
		int result1 = myTokenBalanceDAO.update(myTokenId, accountId, fromBalance);
		if (result1 != 1) {
			throw new RuntimeException("update fromBalance error");
		}

		// 对手方转入转账额
		BigInteger toBalance = myTokenBalanceDAO.queryBalance(myTokenId, to);
		if (toBalance == null) {
			toBalance = value;
			int result2 = myTokenBalanceDAO.insert(myTokenId, to, toBalance);
			if (result2 != 1) {
				throw new RuntimeException("insert toBalance error");
			}
		} else {
			toBalance = toBalance.add(value);
			int result2 = myTokenBalanceDAO.update(myTokenId, to, toBalance);
			if (result2 != 1) {
				throw new RuntimeException("update toBalance error");
			}
		}

		return fromBalance;
	}

	// 查询代币
	public BigInteger balanceOf(String myTokenId, String accountId) {
		// 校验代币是否存在
		String result0 = myTokenDAO.query(myTokenId);
		if (result0 == null) {
			throw new RuntimeException("myToken not found");
		}

		// 查询账户余额
		BigInteger balance = myTokenBalanceDAO.queryBalance(myTokenId, accountId);
		if (balance == null) {
			balance = BigInteger.ZERO;
		}
		return balance;
	}

}
