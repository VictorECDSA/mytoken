package com.fenghm.ethdapp.mytoken.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import com.fenghm.ethdapp.mytoken.common.Constant;
import com.fenghm.ethdapp.mytoken.contract.MyToken_sol_MyToken;
import com.fenghm.ethdapp.mytoken.contract.MyToken_sol_MyToken.RemainEventResponse;

@Service
public class MyTokenService4BC {

	// 发布代币
	public String deploy(String accountId, String password, BigInteger balance) {
		// 从账户文件加载以太坊账户
		Credentials credentials = null;
		try {
			credentials = WalletUtils.loadCredentials(password, Constant.accountFolder.resolve(accountId).toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// 发布代币
		MyToken_sol_MyToken myToken = null;
		try {
			myToken = MyToken_sol_MyToken.deploy(Constant.admin, credentials, Constant.gasProvider, balance).send();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return myToken.getContractAddress();
	}

	// 转账代币
	public BigInteger transfer(String accountId, String password, String myTokenId, String to, BigInteger value) {
		// 从账户文件加载以太坊账户
		Credentials credentials = null;
		try {
			credentials = WalletUtils.loadCredentials(password, Constant.accountFolder.resolve(accountId).toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// 根据合约地址定位以太坊中的智能合约
		MyToken_sol_MyToken myToken = MyToken_sol_MyToken.load(myTokenId, Constant.admin, credentials,
				Constant.gasProvider);

		// 转账代币
		TransactionReceipt receipt = null;
		try {
			receipt = myToken.transfer(to, value).send();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (!receipt.isStatusOK()) {
			throw new RuntimeException("transfer error");
		}

		// 利用智能合约中的事件，查询账户转账后的余额
		List<RemainEventResponse> remainEventList = myToken.getRemainEvents(receipt);
		RemainEventResponse remainEvent = remainEventList.get(0);
		BigInteger balance = remainEvent.amount;

		return balance;
	}

	// 查询代币
	public BigInteger balanceOf(String myTokenId, String accountId) {
		// 根据合约地址定位以太坊中的智能合约
		MyToken_sol_MyToken myToken = MyToken_sol_MyToken.load(myTokenId, Constant.admin, Constant.systemCredentials,
				Constant.gasProvider);

		// 查询账户余额
		BigInteger balance = null;
		try {
			balance = myToken.balanceOf(accountId).send();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return balance;
	}

}
