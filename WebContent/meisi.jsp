<%@ page language="java" contentType="text/html; charset=Windows-31J"
    pageEncoding="windows-31j"%>
<jsp:useBean id="body" class="com.cocolog_nifty.kjunichi.Meisi"></jsp:useBean>
<jsp:setProperty name="body" property="meisi" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Windows-31J">
<title>WebSearch検索</title>
</head>
<body>
<form>
キーワード = <input type="text" name="meisi" value="${body.meisi}" /><br/>
<input type="submit" value="検索" /><br/>
</form>
検索結果： ${body.url}
</body>
</html>