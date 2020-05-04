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
	<p>
		<input type="button" onclick="window.history.back()" value="back" />
	</p>
	<!-- 返回到导航主面按钮 -->
	<p>
		<input type="button" onclick="location.href='../home'" value="home" />
	</p>
</body>
</html>