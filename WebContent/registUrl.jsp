<%@ page language="java" contentType="text/html; charset=Windows-31J"
    pageEncoding="windows-31j"%>
<jsp:useBean id="body" class="com.cocolog_nifty.kjunichi.RegistUrl"></jsp:useBean>
<jsp:setProperty name="body" property="meisi" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Windows-31J">
<title>Insert title here</title>
</head>
<body>
<form>
�o�^URL = <input type="text" name="meisi" value="${body.url}" /><br/>
<input type="submit" value="����" /><br/>
</form>
�������ʁF ${body.regist}
</body>
</html>