package com.fenghm.ethdapp.mytoken.controller;

import java.math.BigInteger;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fenghm.ethdapp.mytoken.service.MyTokenService;
import com.fenghm.ethdapp.mytoken.service.MyTokenService4BC;

@Controller
@RequestMapping("/mytoken")
@Validated
public class MyTokenController {

	// 使用传统中心化数据库的Service
	@Autowired
	MyTokenService myTokenService;

	// 使用基于区块链的Service
	@Autowired
	MyTokenService4BC myTokenService4BC;

	// 发布代币
	@RequestMapping("/deploy")
	public ModelAndView deploy(@NotBlank String accountId, @NotBlank String password, BigInteger balance) {
		System.out.println("call on mytoken deploy, accountId=" + accountId + ", balance=" + balance);
		String myTokenId = myTokenService4BC.deploy(accountId, password, balance);
		ModelAndView mode = new ModelAndView("mytoken/deploy");
		mode.addObject("myTokenId", myTokenId);
		return mode;
	}

	// 转账代币
	@RequestMapping("/transfer")
	public ModelAndView transfer(@NotBlank String accountId, @NotBlank String password, @NotBlank String myTokenId,
			@NotBlank String to, BigInteger value) {
		System.out.println("call on mytoken transfer, myTokenId=" + myTokenId + ", accountId=" + accountId + ", to="
				+ to + ", value=" + value);
		BigInteger balance = myTokenService4BC.transfer(accountId, password, myTokenId, to, value);
		ModelAndView mode = new ModelAndView("mytoken/transfer");
		mode.addObject("myTokenId", myTokenId);
		mode.addObject("accountId", accountId);
		mode.addObject("balance", balance);
		return mode;
	}

	// 查询代币
	@RequestMapping("/balanceof")
	public ModelAndView balanceOf(@NotBlank String accountId, @NotBlank String myTokenId) {
		System.out.println("call on mytoken balance, accountId=" + accountId + ", myTokenId=" + myTokenId);
		BigInteger balance = myTokenService4BC.balanceOf(myTokenId, accountId);
		ModelAndView mode = new ModelAndView("mytoken/balanceof");
		mode.addObject("myTokenId", myTokenId);
		mode.addObject("accountId", accountId);
		mode.addObject("balance", balance);
		return mode;
	}
}
