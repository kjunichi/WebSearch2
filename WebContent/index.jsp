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
�g��[cm] = <input type="text" name="height" value="${body.height}" /><br/>
�̏d[kg] = <input type="text" name="weight" value="${body.weight}" /><br/>
<input type="submit" value="�v�Z����" /><br/>
</form>
BMI �� ${body.bmi} �ł� (18.5�`25���W���ł�)�B
</body>
</html>