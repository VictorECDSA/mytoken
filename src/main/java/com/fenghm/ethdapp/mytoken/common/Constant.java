package com.fenghm.ethdapp.mytoken.common;

import java.math.BigInteger;
import java.nio.file.Path;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;
import org.web3j.tx.gas.ContractGasProvider;

public class Constant {
	static public String gethUrl;
	static public Admin admin;
	static public BigInteger gasPrice;
	static public BigInteger gasLimit;
	static public ContractGasProvider gasProvider;

	static public Path accountFolder;
	static public String systemAccountId;
	static public String systemAccountPassword;
	static public Credentials systemCredentials;
}
