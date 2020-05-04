<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Account list</title>
</head>
<body>
	<b>Account list</b>
	<%
		List<String> accountIds = (List<String>) request.getAttribute("accountIds");
		for (String accountId : accountIds) {
	%>
	<p><%=accountId%></p>
	<%
		}
	%>
	<p>
		<input type="button" onclick="window.history.back()" value="back" />
	</p>
	<p>
		<input type="button" onclick="location.href='../home'" value="home" />
	</p>
</body>
</html>