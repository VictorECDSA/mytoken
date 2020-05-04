package com.fenghm.ethdapp.mytoken.init;

import java.io.File;
import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

import com.fenghm.ethdapp.mytoken.common.Constant;

@Component
public class Initializer implements CommandLineRunner {

	@Autowired
	private Environment env;

	@Override
	public void run(String... arg0) throws Exception {
		Constant.gethUrl = env.getProperty("geth.url");
		Constant.admin = Admin.build(new HttpService(Constant.gethUrl));
		Constant.gasPrice = new BigInteger(env.getProperty("gas.price"), 10);
		Constant.gasLimit = new BigInteger(env.getProperty("gas.limit"), 10);
		Constant.gasProvider = new StaticGasProvider(Constant.gasPrice, Constant.gasLimit);

		Constant.accountFolder = new File(env.getProperty("account.folder")).toPath();
		Constant.systemAccountId = env.getProperty("system.account.id");
		Constant.systemAccountPassword = env.getProperty("system.account.password");
		Constant.systemCredentials = WalletUtils.loadCredentials(Constant.systemAccountPassword,
				Constant.accountFolder.resolve(Constant.systemAccountId).toString());

		System.out.println("gethUrl=" + Constant.gethUrl);
		System.out.println("gasPrice=" + Constant.gasPrice);
		System.out.println("gasLimit=" + Constant.gasLimit);
		System.out.println("accountFolder=" + Constant.accountFolder);
		System.out.println("systemAccountId=" + Constant.systemAccountId);
		System.out.println("systemAccountPassword=" + Constant.systemAccountPassword);
	}

}
