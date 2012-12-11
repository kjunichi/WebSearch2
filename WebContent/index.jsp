<%@ page language="java" contentType="text/html; charset=windows-31j"
    pageEncoding="windows-31j"%>
<jsp:useBean id="body" class="com.example.BMICalc"></jsp:useBean>
<jsp:setProperty name="body" property="height" />
<jsp:setProperty name="body" property="weight" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=windows-31j">
<title>Insert title here</title>
</head>
<body>
<form>
身長[cm] = <input type="text" name="height" value="${body.height}" /><br/>
体重[kg] = <input type="text" name="weight" value="${body.weight}" /><br/>
<input type="submit" value="計算する" /><br/>
</form>
BMI は ${body.bmi} です (18.5～25が標準です)。
</body>
</html>