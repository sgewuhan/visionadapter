<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>用户切换</title>
<script LANGUAGE="JavaScript">
<!--
function openwin1() {
   window.open ("//loginWelcome.jsp", "newwindow", "height=100, width=400, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, status=no")
}

function openwin2() {
   window.open ("//loginWelcome.jsp", "newwindow", "height=100, width=400, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, status=no")
}
//-->
</script>
</head>
<body>
<form action="">


<table >
	<tr>
		<td colspan="2"><input type="submit" name="user1" id="user1" value="用户1" onclick="openwin1()"></td>
		<td colspan="2"><input type="submit" name="user2" id="user2" value="用户2" onclick="openwin1()"></td>
		<td colspan="2"><input type="submit" name="closed" id="closed" value="关闭"></td>
	</tr>
	<tr>
	    <td colspan="6">
	     <iframe></iframe>
	    </td>
	</tr>
</table>
</form>
</body>
</html>