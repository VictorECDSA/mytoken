package com.fenghm.ethdapp.mytoken.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fenghm.ethdapp.mytoken.common.Tool;
import com.fenghm.ethdapp.mytoken.dao.AccountDAO;

@Service
public class AccountService {

	@Autowired
	AccountDAO accountDAO;

	public List<String> list() {
		List<String> accountIds = accountDAO.list();
		return accountIds;
	}

	// 注册账户
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String register(String password) {
		// 生成以“0x”为前缀的40个16进制字符的随机账户ID
		String accountId = Tool.randomHexString(40);

		// 插入账户记录
		int result = accountDAO.insert(accountId, password.hashCode());
		if (result != 1) {
			throw new RuntimeException("insert account error");
		}

		return accountId;
	}

}
