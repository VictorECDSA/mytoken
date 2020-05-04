# mytoken  
  
在以太坊DApp中，智能合约只是其中一部份，还需要通过前端应用接入用户的请求。本章将讲解如何开发一个简单而完整的以太坊DApp，麻雀虽小，五脏俱全！  
同时为了让读者更好地了解DApp与传统的中心化应用在开发过程中的不同，本章的讲解会先根据需求实现成一个中心化系统，再把该系统改造成一个基于以太坊区块链的去中心化应用。阅读本章内容需要读者有过一定的基于Java的SpringBoot和Maven的项目开发经验。  
## （一）要做什么  
本章讲解如何开发一个发布和转账代币的系统，该系统主要有以下功能。  
(1) 注册账户：用户可以在系统注册一个或多个账户，注册时只需要输入一次密码，该密码作为系统对该账户的认证，系统会随机生成一个账户ID返回给用户，用户需要记下该账户ID以便后续进行代币相关的操作。  
(2) 发布代币：用户可以根据已注册的账户和对应的密码，在系统发布一个或多个代币，需要输入初始金额，该初始金额会在发布后归属到该账户名下。  
(3) 转账代币：用户可以把属于自己的某个账户名下的某种代币，转账到另外一个账户，接收方账户可以是自己的，也可以是别人的，需要输入具体转账额。  
(4) 查询代币：用户可以查询任意账户的任意代币的数量。  
首先，我们会把这些需求放到一个传统的中心化应用系统中实现，用户用传统的方式使用这个系统的。  
所有用户通过浏览器向Web应用发起请求，经过Web应用的处理后，数据会被存储到数据库中。  
但是这样会有什么问题呢？所有用户的请求都用同一个Web应用来处理，并存储在同一个数据库中，会存在两方面的问题：  
(1) 单点问题：容易出现机器故障导致请求无法处理或者数据丢失的情况；  
(2) 篡改问题：在利益的驱使下，数据可能会被恶意用户所篡改。  
而这些问题，都是因为数据是中心化处理和存储的，所以我们要把这个系统改造成一个去中去化的系统。  
我们会把系统进一步改造成基于区块链的的分布式系统，这里的区块链指我们使用以太坊来搭建一个私有链网络，然后开发的Web应用通过Web3j来对以太坊私有链中的节点进行RPC调用，不同的用户通过访问不同的Web应用（当然也可以访问同一个Web应用，只要用户信任这个Web应用就可以）来操作这整个系统。  
这样的系统避免了单点问题和篡改问题：一是区块链中的每个节点都存有整个区块链数据账本的副本，即使某个节点的数据丢失，也可以在其他节点上找回相同的数据；二是区块链的数据是经过密码学保护的，是天然就防篡改的。  
现在，相信读者已经了解我们接下来要做什么，并且为什么要这么做了，那么就让我们马上来动手实施吧。  
## （二）环境准备  
1、本章讲解开发的系统主要依赖以下基础环境准备。  
(1) JDK：笔者这里使用的版本为1.8.0_131。  
(2) Eclipse：作为传统中心化应用的IDE。  
(3) Maven：负责引入和管理SpringBoot和Web3j等必要的包。  
(4) MySQL：中心化系统的数据存储。  
以上为一般开发传统JAVA应用系统所需要的环境依赖，本章不详细赘述安装步骤。  
2、由于引入了Solidity智能合约，还需要安装Solidity语言的编译环境，这里我们选择使用solc工具。  
执行以下命令安装solc工具（需要预先安装npm工具）。  
```  
$ npm install -g solc-cli  
```  
3、除了编译Solidity环境，还需要web3j命令行工具在稍后把智能合约转换成Java bean。  
读者可以自行下载web3j工具，下载解压后把其中的bin目录加入到系统PATH环境变量即可，下载路径为https://github.com/web3j/web3j/releases/tag/v4.0.1。  
## （三）创建项目  
1、在Eclipse上以“New -> Project -> Maven Project”新建maven项目，项目名为“mytoken”。  
2、编辑pom.xml引入相关依赖，具体代码如下：  
```  
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">  
	<modelVersion>4.0.0</modelVersion>  
  
	<groupId>com.fenghm.ethDApp</groupId>  
	<artifactId>mytoken</artifactId>  
	<version>0.0.1-SNAPSHOT</version>  
	<packaging>jar</packaging>  
  
	<name>mytoken</name>  
	<url>http://maven.apache.org</url>  
  
	<properties>  
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>  
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>  
		<java.version>1.8</java.version>  
	</properties>  
  
	<!-- 继承 spring boot默认的parent -->  
	<parent>  
		<groupId>org.springframework.boot</groupId>  
		<artifactId>spring-boot-starter-parent</artifactId>  
		<version>1.5.15.RELEASE</version>  
	</parent>  
  
	<dependencies>  
		<!-- web前端  -->  
		<dependency>  
			<groupId>org.springframework.boot</groupId>  
			<artifactId>spring-boot-starter-web</artifactId>  
		</dependency>  
		<dependency>  
			<groupId>javax.servlet</groupId>  
			<artifactId>javax.servlet-api</artifactId>  
		</dependency>  
		<dependency>  
			<groupId>javax.servlet</groupId>  
			<artifactId>jstl</artifactId>  
		</dependency>  
		<dependency>  
			<groupId>org.apache.tomcat.embed</groupId>  
			<artifactId>tomcat-embed-jasper</artifactId>  
		</dependency>  
		<dependency>  
			<groupId>org.apache.tomcat</groupId>  
			<artifactId>tomcat-jsp-api</artifactId>  
		</dependency>  
  
		<!-- 数据库连接  -->  
		<dependency>  
			<groupId>mysql</groupId>  
			<artifactId>mysql-connector-java</artifactId>  
		</dependency>  
		<dependency>  
			<groupId>org.springframework.boot</groupId>  
			<artifactId>spring-boot-starter-jdbc</artifactId>  
		</dependency>  
  
		<!-- 单元测试 -->  
		<dependency>  
			<groupId>junit</groupId>  
			<artifactId>junit</artifactId>  
			<scope>test</scope>  
		</dependency>  
	</dependencies>  
  
	<build>  
		<plugins>  
			<!-- spring-boot-maven-plugin插件  -->  
			<plugin>  
				<groupId>org.springframework.boot</groupId>  
				<artifactId>spring-boot-maven-plugin</artifactId>  
			</plugin>  
		</plugins>  
	</build>  
  
</project>  
```  
3、编辑应用配置mytoken/src/main/resources/application.properties，具体代码如下：  
```  
#端口及上下文根  
server.port=8088  
server.context-path=/mytoken  
  
#前端页面  
spring.mvc.view.prefix=/  
spring.mvc.view.suffix=.jsp  
  
#mysql连接参数  
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/mytoken?useSSL=true  
spring.datasource.username=mytoken  
spring.datasource.password=mytoken  
spring.datasource.driver-class-name=com.mysql.jdbc.Driver  
spring.datasource.max-idle=10  
spring.datasource.max-wait=10000  
spring.datasource.min-idle=5  
spring.datasource.initial-size=5  
```  
4、编辑启动类mytoken/src/main/java/com/fenghm/ethDApp/mytoken/App.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken;  
  
import org.springframework.boot.SpringApplication;  
import org.springframework.boot.autoconfigure.SpringBootApplication;  
import org.springframework.boot.web.support.SpringBootServletInitializer;  
import org.springframework.scheduling.annotation.EnableScheduling;  
  
@SpringBootApplication  
@EnableScheduling  
public class App extends SpringBootServletInitializer {  
  
	public static void main(String[] args) {  
		SpringApplication.run(App.class, args);  
	}  
  
}  
```  
## （四）初始化数据库  
1、在MySQL创建本系统的数据库实例，数据库名和密码均使用“mytoken”。  
2、执行如下DDL创建相关表结构：  
```  
#账户表  
CREATE TABLE IF NOT EXISTS `account`(  
   `account_id` VARCHAR(42) NOT NULL COMMENT '账户ID',  
   `password` INT NOT NULL COMMENT '账户密码',  
   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',  
   PRIMARY KEY ( `account_id` )  
)ENGINE=InnoDB DEFAULT CHARSET=utf8;  
  
#代币表  
CREATE TABLE IF NOT EXISTS `my_token`(  
   `my_token_id` VARCHAR(42) NOT NULL COMMENT '代币ID',  
   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',  
   PRIMARY KEY ( `my_token_id` )  
)ENGINE=InnoDB DEFAULT CHARSET=utf8;  
  
#代币余额表  
CREATE TABLE IF NOT EXISTS `my_token_balance`(  
   `my_token_id`VARCHAR(42) NOT NULL COMMENT '代币ID',  
   `account_id` VARCHAR(42) NOT NULL COMMENT '账户ID',  
   `balance` BIGINT UNSIGNED NOT NULL COMMENT '代币余额',  
   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',  
   PRIMARY KEY ( `my_token_id`, `account_id` )  
)ENGINE=InnoDB DEFAULT CHARSET=utf8;  
```  
## （五）编写DAO  
1、编辑账户数据存储mytoken/src/main/java/com/fenghm/ethDApp/mytoken/dao/AccountDAO.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken.dao;  
  
import java.util.Map;  
  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.dao.EmptyResultDataAccessException;  
import org.springframework.jdbc.core.JdbcTemplate;  
import org.springframework.stereotype.Component;  
  
@Component  
public class AccountDAO {  
  
	@Autowired  
	private JdbcTemplate jdbcTemplate;  
  
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
```  
2、编辑代币数据存储mytoken/src/main/java/com/fenghm/ethDApp/mytoken/dao/MyTokenDAO.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken.dao;  
  
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
```  
3、编辑代币余额数据存储mytoken/src/main/java/com/fenghm/ethDApp/mytoken/dao/MyTokenBalanceDAO.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken.dao;  
  
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
```  
## （六）编写Service  
1、编辑工具类mytoken/src/main/java/com/fenghm/ethDApp/mytoken/common/Tool.java，具体代码如下：  
```  
package com.fenghm.ethdapp.mytoken.common;  
  
import java.util.Random;  
  
public class Tool {  
  
	// 产生以“0x”为前缀的随机16进制字符串  
	public static String randomHexString(int len) {  
		StringBuffer result = new StringBuffer("0x");  
		for (int i = 0; i < len; i++) {  
			result.append(Integer.toHexString(new Random().nextInt(16)));  
		}  
		return result.toString().toLowerCase();  
	}  
  
}  
```  
2、编辑账户服务mytoken/src/main/java/com/fenghm/ethDApp/mytoken/service/AccountService.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken.service;  
  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Service;  
import org.springframework.transaction.annotation.Propagation;  
import org.springframework.transaction.annotation.Transactional;  
  
import com.fenghm.ethDApp.mytoken.common.Tool;  
import com.fenghm.ethDApp.mytoken.dao.AccountDAO;  
  
@Service  
public class AccountService {  
  
	@Autowired  
	AccountDAO accountDAO;  
  
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
```  
3、编辑代币服务mytoken/src/main/java/com/fenghm/ethDApp/mytoken/service/MyTokenService.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken.service;  
  
import java.math.BigInteger;  
  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Service;  
import org.springframework.transaction.annotation.Propagation;  
import org.springframework.transaction.annotation.Transactional;  
  
import com.fenghm.ethDApp.mytoken.common.Tool;  
import com.fenghm.ethDApp.mytoken.dao.AccountDAO;  
import com.fenghm.ethDApp.mytoken.dao.MyTokenBalanceDAO;  
import com.fenghm.ethDApp.mytoken.dao.MyTokenDAO;  
  
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
```  
## （七）编写Controller  
1、编辑导航主页控制器mytoken/src/main/java/com/fenghm/ethDApp/mytoken/controller/HomeController.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken.controller;  
  
import org.springframework.stereotype.Controller;  
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.servlet.ModelAndView;  
  
@Controller  
public class HomeController {  
  
	@RequestMapping({ "/", "/home" })  
	public ModelAndView home() {  
		System.out.println("call on home");  
		ModelAndView mode = new ModelAndView("home");  
		return mode;  
	}  
  
}  
```  
2、编辑账户控制器mytoken/src/main/java/com/fenghm/ethDApp/mytoken/controller/AccountController.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken.controller;  
  
import org.hibernate.validator.constraints.NotBlank;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Controller;  
import org.springframework.validation.annotation.Validated;  
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.servlet.ModelAndView;  
  
import com.fenghm.ethDApp.mytoken.service.AccountService;  
  
@Controller  
@RequestMapping("/account")  
@Validated  
public class AccountController {  
  
	@Autowired  
	AccountService accountService;  
  
	// 注册账户  
	@RequestMapping("/register")  
	public ModelAndView register(@NotBlank String password) {  
		System.out.println("call on account register");  
		String accountId = accountService.register(password);  
		ModelAndView mode = new ModelAndView("account/register");  
		mode.addObject("accountId", accountId);  
		return mode;  
	}  
  
}  
```  
3、编辑代币控制器mytoken/src/main/java/com/fenghm/ethDApp/mytoken/controller/MyTokenController.java，具体代码如下：  
```  
package com.fenghm.ethDApp.mytoken.controller;  
  
import java.math.BigInteger;  
  
import org.hibernate.validator.constraints.NotBlank;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Controller;  
import org.springframework.validation.annotation.Validated;  
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.servlet.ModelAndView;  
  
import com.fenghm.ethDApp.mytoken.service.MyTokenService;  
  
@Controller  
@RequestMapping("/mytoken")  
@Validated  
public class MyTokenController {  
  
	@Autowired  
	MyTokenService myTokenService;  
  
	// 发布代币  
	@RequestMapping("/deploy")  
	public ModelAndView deploy(@NotBlank String accountId, @NotBlank String password, BigInteger balance) {  
		System.out.println("call on mytoken deploy, accountId=" + accountId + ", balance=" + balance);  
		String myTokenId = myTokenService.deploy(accountId, password, balance);  
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
		BigInteger balance = myTokenService.transfer(accountId, password, myTokenId, to, value);  
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
		BigInteger balance = myTokenService.balanceOf(myTokenId, accountId);  
		ModelAndView mode = new ModelAndView("mytoken/balanceof");  
		mode.addObject("myTokenId", myTokenId);  
		mode.addObject("accountId", accountId);  
		mode.addObject("balance", balance);  
		return mode;  
	}  
}  
```  
## （八）编写前端页面  
1、编辑导航主页mytoken/src/main/webapp/home.jsp，具体代码如下：  
```  
<%@ page language="java" contentType="text/html; charset=UTF-8"  
	pageEncoding="UTF-8"%>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">  
<html>  
<head>  
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">  
<title>Home</title>  
</head>  
<body>  
	<b>Home</b>  
	<!-- 注册账户请求连接 -->  
	<p><a href="static/account/register.html">register account</a></p>  
	<!-- 发布代币请求连接 -->  
	<p><a href="static/mytoken/deploy.html">deploy token</a></p>  
	<!-- 查询代币请求连接 -->  
	<p><a href="static/mytoken/balanceof.html">query token</a></p>  
	<!-- 转账代币请求连接 -->  
	<p><a href="static/mytoken/transfer.html">transfer token</a></p>  
</body>  
</html>  
```  
2、编辑注册账户请求页面mytoken/src/main/webapp/static/account/register.html，具体代码如下：  
```  
<!doctype html>  
<html>  
<head>  
<meta charset="utf-8">  
<title>Register account</title>  
</head>  
<body>  
	<b>Register account</b>  
	<form action="../../account/register" method="post">  
		<!-- 输入账户密码 -->  
		<p>Password: <input type="password" name="password" /></p>  
		<!-- 提交按钮 -->  
		<p><input type="submit" value="Submit" /></p>  
		<!-- 后退到上一页按钮 -->  
		<p><input type="button" onclick="window.history.back()" value="back" /></p>  
		<!-- 返回到导航主面按钮 -->  
		<p><input type="button" onclick="location.href='../../home'" value="home" /></p>  
	</form>  
</body>  
</html>  
```  
3、编辑注册账户结果页面mytoken/src/main/webapp/account/register.jsp，具体代码如下：  
```  
<%@ page language="java" contentType="text/html; charset=UTF-8"  
	pageEncoding="UTF-8"%>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">  
<html>  
<head>  
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">  
<title>Register result</title>  
</head>  
<body>  
	<b>Register result</b>  
	<!-- 显示账户ID -->  
	<p>Register succeed, your accountId is ${accountId}</p>  
	<!-- 后退到上一页按钮 -->  
	<p><input type="button" onclick="window.history.back()" value="back" /></p>  
	<!-- 返回到导航主面按钮 -->  
	<p><input type="button" onclick="location.href='../home'" value="home" /></p>  
</body>  
</html>  
```  
4、编辑发布代币请求页面mytoken/src/main/webapp/static/mytoken/deploy.html，具体代码如下：  
```  
<!doctype html>  
<html>  
<head>  
<meta charset="utf-8">  
<title>Deploy token</title>  
</head>  
<body>  
	<b>Deploy token</b>  
	<form action="../../mytoken/deploy" method="post">  
		<!-- 输入账户ID -->  
		<p>Account ID: <input type="text" name="accountId" /></p>  
		<!-- 输入账户密码 -->  
		<p>Password: <input type="password" name="password" /></p>  
		<!-- 输入初始金额 -->  
		<p>Initial amount: <input type="text" name="balance" /></p>  
		<!-- 提交按钮 -->  
		<p><input type="submit" value="Submit" /></p>  
		<!-- 后退到上一页按钮 -->  
		<p><input type="button" onclick="window.history.back()" value="back" /></p>  
		<!-- 返回到导航主面按钮 -->  
		<p><input type="button" onclick="location.href='../../home'" value="home" /></p>  
	</form>  
</body>  
</html>  
```  
5、编辑发布代币结果页面mytoken/src/main/webapp/mytoken/deploy.jsp，具体代码如下：  
```  
<%@ page language="java" contentType="text/html; charset=UTF-8"  
	pageEncoding="UTF-8"%>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">  
<html>  
<head>  
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">  
<title>Deploy result</title>  
</head>  
<body>  
	<b>Deploy result</b>  
	<!-- 显示代币ID -->  
	<p>Deploy succeed, your tokenId is ${myTokenId}</p>  
	<!-- 后退到上一页按钮 -->  
	<p><input type="button" onclick="window.history.back()" value="back" /></p>  
	<!-- 返回到导航主面按钮 -->  
	<p><input type="button" onclick="location.href='../home'" value="home" /></p>  
</body>  
</html>  
```  
6、编辑查询代币请求页面mytoken/src/main/webapp/static/mytoken/balanceof.html，具体代码如下：  
```  
<!doctype html>  
<html>  
<head>  
<meta charset="utf-8">  
<title>Query token</title>  
</head>  
<body>  
	<b>Query token</b>  
	<form action="../../mytoken/balanceof" method="post">  
		<!-- 输入账户ID -->  
		<p>Account ID: <input type="text" name="accountId" /></p>  
		<!-- 输入代币ID -->  
		<p>My token ID: <input type="text" name="myTokenId" /></p>  
		<!-- 提交按钮 -->  
		<p><input type="submit" value="Submit" /></p>  
		<!-- 后退到上一页按钮 -->  
		<p><input type="button" onclick="window.history.back()" value="back" /></p>  
		<!-- 返回到导航主面按钮 -->  
		<p><input type="button" onclick="location.href='../../home'" value="home" />	</p>  
	</form>  
</body>  
</html>  
```  
7、编辑查询代币结果页面mytoken/src/main/webapp/mytoken/balanceof.jsp，具体代码如下：  
```  
<%@ page language="java" contentType="text/html; charset=UTF-8"  
	pageEncoding="UTF-8"%>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">  
<html>  
<head>  
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">  
<title>Query result</title>  
</head>  
<body>  
	<b>Query result</b>  
	<!-- 显示余额 -->  
	<p>Query succeed, your balance is ${balance}</p>  
	<!-- 后退到上一页按钮 -->  
	<p><input type="button" onclick="window.history.back()" value="back" /></p>  
	<!-- 返回到导航主面按钮 -->  
	<p><input type="button" onclick="location.href='../home'" value="home" /></p>  
</body>  
</html>  
```  
8、编辑转账代币请求页面mytoken/src/main/webapp/static/mytoken/transfer.html，具体代码如下：  
```  
<!doctype html>  
<html>  
<head>  
<meta charset="utf-8">  
<title>Transfer token</title>  
</head>  
<body>  
	<b>Transfer token</b>  
	<form action="../../mytoken/transfer" method="post">  
		<!-- 输入账户ID -->  
		<p>Account ID: <input type="text" name="accountId" /></p>  
		<!-- 输入账户密码 -->  
		<p>Password: <input type="password" name="password" /></p>  
		<!-- 输入代币ID -->  
		<p>My token ID: <input type="text" name="myTokenId" /></p>  
		<!-- 输入对手方账户ID -->  
		<p>Receiver account: <input type="text" name="to" /></p>  
		<!-- 输入转账金额 -->  
		<p>Transfer amount: <input type="text" name="value" /></p>  
		<!-- 提交按钮 -->  
		<p><input type="submit" value="Submit" /></p>  
		<!-- 后退到上一页按钮 -->  
		<p><input type="button" onclick="window.history.back()" value="back" /></p>  
		<!-- 返回到导航主面按钮 -->  
		<p><input type="button" onclick="location.href='../../home'" value="home" />	</p>  
	</form>  
</body>  
</html>  
```  
9、编辑转账代币结果页面mytoken/src/main/webapp/mytoken/transfer.jsp，具体代码如下：  
```  
<%@ page language="java" contentType="text/html; charset=UTF-8"  
	pageEncoding="UTF-8"%>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">  
<html>  
<head>  
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">  
<title>Transfer result</title>  
</head>  
<body>  
	<b>Transfer result</b>  
	<!-- 显示余额 -->  
	<p>Transfer succeed, now your balance is ${balance}</p>  
	<!-- 后退到上一页按钮 -->  
	<p><input type="button" onclick="window.history.back()" value="back" /></p>  
	<!-- 返回到导航主面按钮 -->  
	<p><input type="button" onclick="location.href='../home'" value="home" /></p>  
</body>  
</html>  
```  
## （九）先运行看看  
至此，一个传统的中心化应用系统已经基本成形，我们可以试着运行了。  
1、在浏览器输入“http://127.0.0.1:8088/mytoken/”登陆导航主页。  
导航主页有注册账户、发布代币、查询代币、转账代币4个功能。  
2、点击导航主页上的链接“register account”进入注册账户请求页面。  
在注册账户请求页面上，输入一次密码，这个密码需要读者记下来，后续一系列代币操作需要使用到该密码，点击“Submit”按钮提交注册账户请求。  
系统返回注册账户结果，其中账户ID（这里为“0xfe8fba859afd72615b91e6de106d0fe68a5b76a9”）需要读者记下来，后续一系列代币操作需要使用到该账户ID。  
3、点击导航主页上的链接“deploy token”进入发布代币请求页面。  
在发布代币请求页面上，填写刚刚注册的账户和密码，填写初始金额，点击“Submit”按钮提交发布代币请求。  
系统返回发布代币结果，其中代币ID（这里为“0x4899fe5232178d8c93af4d5aa256873c35ae03a1”）需要读者记下来，后续一系列代币操作需要使用到该代币ID。  
4、点击导航主页上的链接“query token”进入查询代币请求页面。  
在查询代币请求页面上，填写新注册账户的账户ID和新发布代币的代币ID，点击“Submit”按钮提交查询代币请求。  
系统返回查询代币结果，可以查询到在新发布代币下新注册账户的代币余额。  
5、接下来要进行转账代币的操作，在此之前，我们参考上述注册账户的步骤，再创建一个对手方账户，记下对手方账户的账户ID（这里为“0x752d45d84d73d2c30e7ef88b9fe9f09ea0525465”）。  
点击导航主页上的链接“transfer token”进入转账代币请求页面。  
在转账代币请求页面上，依次填写最初创建账户的账户ID及对应的密码、新发布代币的代币ID、对手方账户的账户ID和转账金额，点击“Submit”按钮提交转账代币请求。  
系统返回转账代币结果，显示转账后的账户代币余额。之后读者可以按上述查询代币余额的步骤查询对手方账户的代币余额，会发现对手方账户得到了之前转账接收的代币。  
## （十）如何改造成DApp  
到目前为止，我们开发的这个系统好像跟区块链还没什么关系对吧，不用急，我们接下来就把这个系统改造成一个基于以太坊区块链的DApp系统。  
这个中心化应用系统的架构可以分为5层，自底向上分别为：数据库层、DAO层、Service层、Controller层、JSP层，是比较典型的Web应用。如何改造成基于区块链的Dapp，笔者提出一种方案，把区块链类比成一个多方共享的、数据不可篡改的数据库，替换原来的数据库即可。  
把本系统改造成基于区块链的DApp，可以保持前端页面和Controller逻辑基本不变，只需要从Service层开始，替换成基于区块链和智能合约的新Service。  
## （十一）增加区块链配置参数  
1、编辑pom.xml，增加web3j相关依赖，具体代码如下：  
```  
……  
	<properties>  
		……  
		<geth.version>4.0.1</geth.version>  
	</properties>  
……  
	<dependencies>  
……  
		<!-- geth连接 -->  
		<dependency>  
			<groupId>org.web3j</groupId>  
			<artifactId>core</artifactId>  
			<version>${geth.version}</version>  
		</dependency>  
		<dependency>  
			<groupId>org.web3j</groupId>  
			<artifactId>geth</artifactId>  
			<version>${geth.version}</version>  
		</dependency>  
		<dependency>  
			<groupId>org.web3j</groupId>  
			<artifactId>parity</artifactId>  
			<version>${geth.version}</version>  
		</dependency>  
	</dependencies>  
……  
```  
2、到本地所搭建的以太坊私有链网络（参考初级实践上的《搭建以太坊私有链网络》）的其中一个节点的geth控制台，执行如下命令：  
```  
> eth.coinbase  
"0xc93a95297d7d51e923ef04e108d88431adaddba1"  
```  
可以看到笔者这里的geth挖矿账户为“0xc93a95297d7d51e923ef04e108d88431adaddba1”，读者看到的结果可能不一样，后续操作请读者按实际情况替换。  
到geth的执行目录下的keystore目录下找到“UTC--2018-06-19T13-43-47.289391600Z--c93a95297d7d51e923ef04e108d88431adaddba1”的文件，复制到“F:/credentials/credentials0”（该目录为改造后的账户文件存储目录，下面第2步会用到），并重命名为“0xc93a95297d7d51e923ef04e108d88431adaddba1”。  
同样地，到该私有链网络的另一个节点的geth控制台下，找到这个节点的挖矿账户的账户文件——笔者这里找到名为“UTC--2018-06-18T13-29-19.218969396Z--090ae71ba33d7cb7210dd8cf52e61d6a0ab7b04f”的文件，复制到“F:/credentials/credentials1”（等下查看运行效果时会用到），并重命名为“0x090ae71ba33d7cb7210dd8cf52e61d6a0ab7b04f”。  
3、编辑应用配置mytoken/src/main/resources/application.properties，增加区块链配置，具体代码如下：  
```  
……  
#geth连接参数  
geth.url=http://127.0.0.1:8546  
gas.price=18000000000  
gas.limit=4700000  
account.folder=F:/credentials/credentials0  
system.account.id=0xc93a95297d7d51e923ef04e108d88431adaddba1  
system.account.password=123  
```  
其中各项参数解释如下：  
(1) geth.url：具体启动的Geth客户端的ip和端口  
(2) gas.price：向区块链发送每笔交易的gas价格  
(3) gas.limit：向区块链发送每笔交易的gas消耗上限  
(4) account.folder：改造后的账户文件存储目录  
(5) system.account.id：指定系统账户的ID，同时也是系统账户文件的文件名，该系统账户用来为用户新增的账户分配以太币，以保证这些账户能顺利进行区块链上的交易，这个系统账户可以选择所连接Geth客户端的挖矿账户  
(6) system.account.password：  
指定系统账户的密码  
4、增加区块链配置参数mytoken/src/main/java/com/fenghm/ethdapp/mytoken/common/Constant.java，具体代码如下：  
```  
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
```  
5、增加区块链配置初始化程序mytoken/src/main/java/com/fenghm/ethdapp/mytoken/init/Initializer.java，具体代码如下：  
```  
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
```  
## （十二）生成智能合约Java bean  
1、使用Solidity语言编写智能合约MyToken.sol，具体代码如下：  
```  
pragma solidity >=0.4.22 <0.6.0;  
  
//引入安全计算函数库  
import "./SafeMath.sol";  
  
contract MyToken {  
  
    //把安全计算应用于uint256类型  
    using SafeMath for uint256;  
  
    //记录每个账户地址的代币余额  
    mapping (address => uint256) public balanceOf;  
  
    //构造函数  
    constructor(uint256 _supply) public {  
        if (_supply == 0) _supply = 10000;  
        balanceOf[msg.sender] = _supply;  
    }  
  
    //转账函数  
    function transfer(address _to, uint256 _value) public {  
        balanceOf[msg.sender] = balanceOf[msg.sender].sub(_value);  
        balanceOf[_to] = balanceOf[_to].add(_value);  
        //触发事件  
        emit remain(msg.sender, balanceOf[msg.sender]);  
    }  
  
    //定义事件，用来记录转账之后还有多少代币余额  
    event remain(address account, uint256 amount);  
  
}  
```  
2、由于该合约引用了安全计算函数库，继续编写智能合约SafeMath.sol，具体代码如下：  
```  
pragma solidity >=0.4.22 <0.6.0;  
   
/**  
 * @title SafeMath  
 * @dev Unsigned math operations with safety checks that revert on error  
 */  
   
library SafeMath {  
    /**  
     * @dev Multiplies two unsigned integers, reverts on overflow.  
     */  
    function mul(uint256 a, uint256 b) internal pure returns (uint256) {  
        // Gas optimization: this is cheaper than requiring 'a' not being zero, but the  
        // benefit is lost if 'b' is also tested.  
        // See: https://github.com/OpenZeppelin/openzeppelin-solidity/pull/522  
        if (a == 0) {  
            return 0;  
        }  
   
        uint256 c = a * b;  
        require(c / a == b);  
   
        return c;  
    }  
   
    /**  
     * @dev Integer division of two unsigned integers truncating the quotient, reverts on division by zero.  
     */  
    function div(uint256 a, uint256 b) internal pure returns (uint256) {  
        // Solidity only automatically asserts when dividing by 0  
        require(b > 0);  
        uint256 c = a / b;  
        // assert(a == b * c + a % b); // There is no case in which this doesn't hold  
   
        return c;  
    }  
   
    /**  
     * @dev Subtracts two unsigned integers, reverts on overflow (i.e. if subtrahend is greater than minuend).  
     */  
    function sub(uint256 a, uint256 b) internal pure returns (uint256) {  
        require(b <= a);  
        uint256 c = a - b;  
   
        return c;  
    }  
   
    /**  
     * @dev Adds two unsigned integers, reverts on overflow.  
     */  
    function add(uint256 a, uint256 b) internal pure returns (uint256) {  
        uint256 c = a + b;  
        require(c >= a);  
   
        return c;  
    }  
   
    /**  
     * @dev Divides two unsigned integers and returns the remainder (unsigned integer modulo),  
     * reverts when dividing by zero.  
     */  
    function mod(uint256 a, uint256 b) internal pure returns (uint256) {  
        require(b != 0);  
        return a % b;  
    }  
}  
```  
3、在MyToken.sol和SafeMath.sol的目录下，执行如下命令。  
```  
$ solcjs MyToken.sol SafeMath.sol --abi --bin -o ./  
```  
执行完成后，会发现该目录生成了MyToken合约的字节码文件MyToken_sol_MyToken.bin和ABI文件MyToken_sol_MyToken.abi。  
4、继续执行如下命令。  
```  
$ web3j solidity generate -b MyToken_sol_MyToken.bin -a MyToken_sol_MyToken.abi -o . -p com.fenghm.ethdapp.mytoken.contract  
```  
执行完成后，会发现在目录com/fenghm/ethdapp/mytoken/contract下生成了Java bean文件MyToken_sol_MyToken.java，具体代码如下：  
```  
package com.fenghm.ethdapp.mytoken.contract;  
  
import io.reactivex.Flowable;  
import java.math.BigInteger;  
import java.util.ArrayList;  
import java.util.Arrays;  
import java.util.Collections;  
import java.util.List;  
import org.web3j.abi.EventEncoder;  
import org.web3j.abi.FunctionEncoder;  
import org.web3j.abi.TypeReference;  
import org.web3j.abi.datatypes.Address;  
import org.web3j.abi.datatypes.Event;  
import org.web3j.abi.datatypes.Function;  
import org.web3j.abi.datatypes.Type;  
import org.web3j.abi.datatypes.generated.Uint256;  
import org.web3j.crypto.Credentials;  
import org.web3j.protocol.Web3j;  
import org.web3j.protocol.core.DefaultBlockParameter;  
import org.web3j.protocol.core.RemoteCall;  
import org.web3j.protocol.core.methods.request.EthFilter;  
import org.web3j.protocol.core.methods.response.Log;  
import org.web3j.protocol.core.methods.response.TransactionReceipt;  
import org.web3j.tx.Contract;  
import org.web3j.tx.TransactionManager;  
import org.web3j.tx.gas.ContractGasProvider;  
  
/**  
 * <p>Auto generated code.  
 * <p><strong>Do not modify!</strong>  
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,  
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the   
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.  
 *  
 * <p>Generated with web3j version 4.0.1.  
 */  
public class MyToken_sol_MyToken extends Contract {  
    private static final String BINARY = "608060405234801561001057600080fd5b506040516020806103fb8339810180604052602081101561003057600080fd5b810190808051906020019092919050505060008114156100505761271090505b806000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000208190555050610358806100a36000396000f3fe608060405234801561001057600080fd5b5060043610610053576000357c01000000000000000000000000000000000000000000000000000000009004806370a0823114610058578063a9059cbb146100b0575b600080fd5b61009a6004803603602081101561006e57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506100fe565b6040518082815260200191505060405180910390f35b6100fc600480360360408110156100c657600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190505050610116565b005b60006020528060005260406000206000915090505481565b610167816000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546102e990919063ffffffff16565b6000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055506101fa816000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205461030b90919063ffffffff16565b6000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055507f1ac24460ed0d6bc7bccc094de79b3ba64d1171599f23e2bb57eeb39dc1067d06336000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019250505060405180910390a15050565b60008282111515156102fa57600080fd5b600082840390508091505092915050565b600080828401905083811015151561032257600080fd5b809150509291505056fea165627a7a7230582079d755f26e8a6f5e206ece41a37bfde0ee9798bf0f1da7c9cf444d74dd34d65a0029";  
  
    public static final String FUNC_BALANCEOF = "balanceOf";  
  
    public static final String FUNC_TRANSFER = "transfer";  
  
    public static final Event REMAIN_EVENT = new Event("remain",   
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));  
    ;  
  
    @Deprecated  
    protected MyToken_sol_MyToken(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {  
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);  
    }  
  
    protected MyToken_sol_MyToken(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {  
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);  
    }  
  
    @Deprecated  
    protected MyToken_sol_MyToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {  
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);  
    }  
  
    protected MyToken_sol_MyToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {  
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);  
    }  
  
    public RemoteCall<BigInteger> balanceOf(String param0) {  
        final Function function = new Function(FUNC_BALANCEOF,   
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)),   
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));  
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);  
    }  
  
    public RemoteCall<TransactionReceipt> transfer(String _to, BigInteger _value) {  
        final Function function = new Function(  
                FUNC_TRANSFER,   
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_to),   
                new org.web3j.abi.datatypes.generated.Uint256(_value)),   
                Collections.<TypeReference<?>>emptyList());  
        return executeRemoteCallTransaction(function);  
    }  
  
    public List<RemainEventResponse> getRemainEvents(TransactionReceipt transactionReceipt) {  
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(REMAIN_EVENT, transactionReceipt);  
        ArrayList<RemainEventResponse> responses = new ArrayList<RemainEventResponse>(valueList.size());  
        for (Contract.EventValuesWithLog eventValues : valueList) {  
            RemainEventResponse typedResponse = new RemainEventResponse();  
            typedResponse.log = eventValues.getLog();  
            typedResponse.account = (String) eventValues.getNonIndexedValues().get(0).getValue();  
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();  
            responses.add(typedResponse);  
        }  
        return responses;  
    }  
  
    public Flowable<RemainEventResponse> remainEventFlowable(EthFilter filter) {  
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, RemainEventResponse>() {  
            @Override  
            public RemainEventResponse apply(Log log) {  
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(REMAIN_EVENT, log);  
                RemainEventResponse typedResponse = new RemainEventResponse();  
                typedResponse.log = log;  
                typedResponse.account = (String) eventValues.getNonIndexedValues().get(0).getValue();  
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();  
                return typedResponse;  
            }  
        });  
    }  
  
    public Flowable<RemainEventResponse> remainEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {  
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());  
        filter.addSingleTopic(EventEncoder.encode(REMAIN_EVENT));  
        return remainEventFlowable(filter);  
    }  
  
    @Deprecated  
    public static MyToken_sol_MyToken load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {  
        return new MyToken_sol_MyToken(contractAddress, web3j, credentials, gasPrice, gasLimit);  
    }  
  
    @Deprecated  
    public static MyToken_sol_MyToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {  
        return new MyToken_sol_MyToken(contractAddress, web3j, transactionManager, gasPrice, gasLimit);  
    }  
  
    public static MyToken_sol_MyToken load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {  
        return new MyToken_sol_MyToken(contractAddress, web3j, credentials, contractGasProvider);  
    }  
  
    public static MyToken_sol_MyToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {  
        return new MyToken_sol_MyToken(contractAddress, web3j, transactionManager, contractGasProvider);  
    }  
  
    public static RemoteCall<MyToken_sol_MyToken> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, BigInteger _supply) {  
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_supply)));  
        return deployRemoteCall(MyToken_sol_MyToken.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);  
    }  
  
    public static RemoteCall<MyToken_sol_MyToken> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, BigInteger _supply) {  
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_supply)));  
        return deployRemoteCall(MyToken_sol_MyToken.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);  
    }  
  
    @Deprecated  
    public static RemoteCall<MyToken_sol_MyToken> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger _supply) {  
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_supply)));  
        return deployRemoteCall(MyToken_sol_MyToken.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);  
    }  
  
    @Deprecated  
    public static RemoteCall<MyToken_sol_MyToken> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger _supply) {  
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_supply)));  
        return deployRemoteCall(MyToken_sol_MyToken.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);  
    }  
  
    public static class RemainEventResponse {  
        public Log log;  
  
        public String account;  
  
        public BigInteger amount;  
    }  
}  
```  
5、把该Java bean文件放入本章上述的项目内mytoken/src/main/java/com/fenghm/ethdapp/mytoken/contract/MyToken_sol_MyToken.java。  
## （十三）改造Service  
1、修改账户服务mytoken/src/main/java/com/fenghm/ethDApp/mytoken/service/AccountService.java，具体代码如下：  
```  
package com.fenghm.ethdapp.mytoken.service;  
  
import java.io.File;  
import java.math.BigDecimal;  
  
import org.springframework.stereotype.Service;  
import org.web3j.crypto.WalletUtils;  
import org.web3j.tx.Transfer;  
import org.web3j.utils.Convert;  
  
import com.fenghm.ethdapp.mytoken.common.Constant;  
  
@Service  
public class AccountService {  
  
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
```  
注意，该注册账户的服务，会为每个新注册的账户分配一定的以太币，以保证该账户后续可以正常在以太坊上进行交易。  
2、修改代币服务mytoken/src/main/java/com/fenghm/ethdapp/mytoken/service/MyTokenService.java，具体代码如下：  
```  
package com.fenghm.ethdapp.mytoken.service;  
  
import java.math.BigInteger;  
  
import org.springframework.stereotype.Service;  
import org.web3j.crypto.Credentials;  
import org.web3j.crypto.WalletUtils;  
import org.web3j.protocol.core.methods.response.TransactionReceipt;  
  
import com.fenghm.ethdapp.mytoken.common.Constant;  
import com.fenghm.ethdapp.mytoken.contract.MyToken_sol_MyToken;  
  
@Service  
public class MyTokenService {  
  
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
```  
## （十四）增加调度分配以太币  
在新注册账户服务AccountService.java中，系统账户会为每个新注册的账户分配一些以太币，但随着该账户交易的不停发生，这个初始分配的以太币总会有消耗完的一天，所以还应该有一个调度程序，以每天一次（凌晨零点）的频率为每个账户分配一定量的以太币。  
编写调度程序mytoken/src/main/java/com/fenghm/ethdapp/mytoken/schedule/ScheduledTasks.java，具体代码如下：  
```  
package com.fenghm.ethdapp.mytoken.schedule;  
  
import java.io.File;  
import java.math.BigDecimal;  
  
import org.springframework.scheduling.annotation.Scheduled;  
import org.springframework.stereotype.Component;  
import org.web3j.tx.Transfer;  
import org.web3j.utils.Convert;  
  
import com.fenghm.ethdapp.mytoken.common.Constant;  
  
@Component  
public class ScheduledTasks {  
  
	@Scheduled(cron = "0 0 0 * * ?")  
	public void allocateEth() {  
		System.out.println("allocateEth begin");  
		for (File file : Constant.accountFolder.toFile().listFiles()) {  
			System.out.println("found file: " + file);  
			if (!file.isFile()) {  
				continue;  
			}  
			String accountId = file.getName();  
			if (accountId.equals(Constant.systemAccountId)) {  
				continue;  
			}  
			System.out.println("allocate eth to " + accountId);  
			try {  
				Transfer.sendFunds(Constant.admin, Constant.systemCredentials, accountId, BigDecimal.valueOf(1L),  
						Convert.Unit.ETHER).send();  
			} catch (Exception e) {  
				e.printStackTrace();  
				throw new RuntimeException(e);  
			}  
  
		}  
		System.out.println("allocateEth finished");  
	}  
  
}  
```  
## （十五）再运行看看  
到这里，这个系统已经被改造成了一个基于以太坊的DApp系统，我们可以再运行看看效果。怎么运行呢，按照之前的步骤用浏览器在同一个网站上注册、发布、查询、转账走一遍吗？不不不，如果跟之前一样运行，那跟一个中心化系统有什么区别，我们的改造就没有意义了，所以我们要用另一种方式运行这个系统。  
1、首先我们要分别启动两个Geth客户端，注意启动时要开启RPC端口，即启动命令中要加上“ --rpc --rpcapi "db,eth,net,web3,personal" --rpccorsdomain "*" --rpcport 8546”参数，如果两个Geth客户端启动在同一台机器设备上，还要注意区分启动的端口不同相同。  
笔者这里第一个Geth客户端的启动命令为：  
$ geth --datadir data --networkid 20140628 --rpc --rpcapi "db,eth,net,web3,personal" --rpccorsdomain "*" --rpcaddr 0.0.0.0 --nodiscover --port 16333 --rpcport 8546 console  
第二个Geth客户端在远程一台IP为“192.168.213.134”的机器上启动，启动命令为：  
$ geth --datadir data --networkid 20140628 --rpc --rpcapi "db,eth,net,web3,personal" --rpccorsdomain "*" --rpcaddr 0.0.0.0 --nodiscover --port 16333 --rpcport 8547 console  
读者可以根据自己的实际情况进行参考。  
2、把这两个客户端连接组成一个区块链网络，这部份操作可以参考之前初级实践中的组建网络一节。  
3、启动至少一个Geth客户端的挖矿。  
4、准备两个Web应用的properties参数。  
笔者这里第一个Web应用实例的参数设置如下：  
```  
#geth连接参数  
geth.url=http://127.0.0.1:8546  
gas.price=18000000000  
gas.limit=4700000  
account.folder=F:/credentials/credentials0  
system.account.id=0xc93a95297d7d51e923ef04e108d88431adaddba1  
system.account.password=123  
```  
第二个Web应用实例的参数设置如下：  
```  
#geth连接参数  
geth.url=http://192.168.213.134:8547  
gas.price=18000000000  
gas.limit=4700000  
account.folder=F:/credentials/credentials1  
system.account.id=0x090ae71ba33d7cb7210dd8cf52e61d6a0ab7b04f  
system.account.password=123  
```  
其中需要特别注意的是，“geth.url”决定了该Web应用端连接不同的Geth客户端，“account.folder”决定了该Web应用端使用不同的账户文件目录，“system.account.id”决定了该Web应用端使用不同的系统账户文件——该文件是事先从所连接的Geth客户端根据挖矿账户拷贝下来并重命名的。  
5、如果两个Web应用实例是启动在同一台机器上，还需要注意使用不同的上下文根端口。  
笔者这里第一个Web应用实例的参数设置如下：  
```  
#端口及上下文根  
server.port=8088  
server.context-path=/mytoken  
```  
第二个Web应用实例的参数设置如下：  
```  
#端口及上下文根  
server.port=8089  
server.context-path=/mytoken  
```  
6、准备的功夫终于做完了，现在可以启动这两个Web应用实例了。  
7、分别在浏览器输入“http://127.0.0.1:8088/mytoken/”和“http://127.0.0.1:8089/mytoken/”登陆两个Web应用的导航主页。  
两个Web应用实例已经成功启动，拥有同样的功能，只是启动端口不一样（一个是8088，另一个是8089），接下来我们分别模拟两个不同的用户，分别使用这两个Web应用实例。  
8、分别在两个Web应用上注册账户。  
记下两个创建好的账户的账户ID（笔者这里分别为“0xd4ab72795b37b4187ab1498a6de090f8d02e95fb”和“0xc48ac04b8a5b87bf3ec30a153af3fc02069ee3e6”）。  
9、然后使用其中一个账户在对应的Web应用上发布代币。  
记下已发布代币的代币ID（笔者这里为“0x5732a5dd0ebcd8ea7ced92dfa2e2cdd2f6806683”）。  
10、分别查看两个账户在该代币下的代币余额。  
其中第一个账户有发布时的初始金额，而另一个账户是没有余额的。  
11、在第一个Web应用上，从第一个账户对另一个账户进行转账。  
第一个账户对另一个账户进行了转账。  
12、再次查看两个账户在该代币下的代币余额。  
可以看到，在第一个Web应用上的转账操作，在第二个Web应用上的账户也能收到对应的代币。实验完成！  
## （十六）还可以怎么优化  
接下来，读者可以从易用性或者安全性等方面进一步对这个系统进行优化，可能的优化点有：  
(1) 目前标识一个账户，或者标识一种代币，使用的是以“0x”开头的40个字符的随机16进制字符串，该字符串亦即是以太坊里的账户地址，或者合约地址，是由以太坊随机生成的。这种随机字符串，对于用户记忆来说，是非常不友好的，例如用户发布一种代币之后，是难以记住该ID以来后续进行相关查询或者转账操作的。所以，读者可以尝试由用户自行给某个账户或者某种代币起“别名”，再由MySQL数据库记录“别名”与随机ID的映射关系，用户在进行操作时只需要输入“别名”，再由系统转换成随机ID进行后续处理。这样，可以增加系统的用户友好度。  
(2) 经过前章节的学习，我们已经知道在以太坊上进行交易，是需要消耗一定的以太币来购买gas。在本次改造中，我们使用的以太币分配方式，一方面在账户创建时预分配了一些以太币，另一方面通过每天的调度再分配一些以太币。可以进一步优化这种分配方式，把以太币类比成这个系统的积分，用户在系统上的某些行为可以累积积分，更多积分意味着可以进行更多的操作。  
(3) 目前Web应用调用Geth客户端，是通过web3j进行同步调用，即Service层里是使用“send()”函数进行调用，这种调用方式会阻塞用户请求，使用户在页面点击提交请求后，需要等待相当长的时候，才能收到系统返回——因为以太坊上的每一笔交易都需要通过挖矿来执行和确认。可以改为在Service层里使用web3j的“sendAsync()”函数来对Geth客户端进行异步调用，让请求马上答应到用户，从而让用户操作更加流畅。  
(4) 目前该前端页面向后端提交请求是以post方式，在某些浏览器下进行刷新动作会重复提交请求，例如可能会出现用户不知情地重复转账的情况，当然如果改为以get请求那么把密码直接拼在url上也不是很妥，而且目前该Web应用的用户权限管理是直接使用以太坊的账户文件管理，在前端页面直接输入以太坊账户文件的keystore文件密码，这种做法也不是很妥，可以思考如何进一步优化以太坊keystore文件和系统用户权限和安全管理。  
读者可以尝试对这个系统进一步优化，加深对以太坊DApp和Web3j的理解。  
## （十七）小结  
本章先开发了一个传统的基于关系型数据库的Web应用系统，在此基础上引入以太坊区块链，改造成一个去中心化，具有防篡改功能的分布式系统。具体的改造过程有以下几点：  
(1) 数据存储位置：把数据从存储在关系型数据库上，改为存储在区块链上，也就是把数据上链，这样可以达到防篡改的效果；  
(2) 业务逻辑位置：原来的业务逻辑，例如账户密码的判断，余额是否充足的判断，均书写在Service层，而改造后，这些业务逻辑书写在智能合约层（即示例中的Solidity代码），也就是把逻辑上链，这样可以达到公开透明的效果；  
(3) 部署方式；原来是单节点部署启动，改造之后为多节点部署启动，在实际的应用中，应该把不同的节点部署到不同的利益相关方，这样一来可以进一步达到防篡改的效果，另外还可以在某个节点数据丢失时，可以从另外的节点重新获取数据。  
经过这些改造，相信读者已经基本了解DApp与传统应用系统在开发上和功能上的不同。  
