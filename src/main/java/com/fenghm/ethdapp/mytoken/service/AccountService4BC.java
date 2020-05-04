package com.fenghm.ethdapp.mytoken.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.web3j.crypto.WalletUtils;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import com.fenghm.ethdapp.mytoken.common.Constant;

@Service
public class AccountService4BC {

	public List<String> list() {
		List<String> list = new ArrayList<String>();
		for (File file : Constant.accountFolder.toFile().listFiles()) {
			if (file.isFile()) {
				list.add(file.getName());
			}

		}
		return list;
	}

	// 注册账户
	public String register(String password) {
		// 创建以太坊账户文件
		String walletFileName = null;
		try {
			walletFileName = WalletUtils.generateNewWalletFile(password, Constant.accountFolder.toFile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// 把以太坊账户文件改为以账户地址命名
		String accountId = "0x"
				+ walletFileName.substring(walletFileName.lastIndexOf("--") + 2, walletFileName.lastIndexOf("."));
		File walletFile = Constant.accountFolder.resolve(walletFileName).toFile();
		File newWalletFile = Constant.accountFolder.resolve(accountId).toFile();
		walletFile.renameTo(newWalletFile);

		// 用系统账户（即geth的挖矿账户）转一笔以太币给新创建的账户，保证该新创建的账户能够顺利进行交易
		try {
			Transfer.sendFunds(Constant.admin, Constant.systemCredentials, accountId, BigDecimal.valueOf(1L),
					Convert.Unit.ETHER).send();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return accountId;
	}

}
