
<html>
<head>

<title>Windchill</title>
<%		
		
String username=null;
String password=null;

if("GET".equals(request.getMethod())){
	username=(String)request.getParameter("GDPUsername");
	out.println(username);
	if(username==null) username="";
	password=(String)request.getParameter("GDPPassword");
	out.println(password);
}

if("POST".equals(request.getMethod())){
	username=request.getParameter("username");
	if(username==null) username="";
	password=request.getParameter("password");
}

if(password!=null){

	session.setAttribute("GDPUsername",username);
	session.setAttribute("GDPPassword",password);
	%><script>document.location.href="loginWelcome.jsp";</script><%
	
}	
%>
<script>
	
function verify(){
	var username=document.getElementById("username").value;
	var password=document.getElementById("password").value;
	alert(username);
	if(username.length<1){
		alert("Please input Username.");
		return false;
	}
	
	if(password.length<1){
		alert("Please input Password.");
		return false;
	}
	
	return true;
}
function submit_a(){
	if(verify()){
	   var form = document.loginform;
	   form.submit();
	}
}
</script>
</head>

<body bgColor=#ffffff>
<table width="800" align=center>
<tr><td bgColor=#ffffff>

<%
wt.httpgw.URLFactory urlFactory=new wt.httpgw.URLFactory();
%>

<form name = "loginform" action="<%=urlFactory.getHREF("login/login.jsp")%>" method="POST">
<table border="0" cellpadding="0" cellspacing="0" align="center" height="70%">
<tr><td>


		  <div align="center">Login</div>

		   
			<table width="99%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td width="20%" nowrap height="31" align="right">Username</td>&nbsp;&nbsp;
				<td>
				  <input type="text" id="username" name="username" value="<%=username%>">
				</td>
			</tr>
			<tr>
				<td height="31" align="right">Password</td>&nbsp;&nbsp;
				<td>
				  <input type="password" id="password" name="password">
				</td>
			</tr>
			<tr>

			</tr>
			<tr align="center">
				<td colspan="2" >
					<input type="button" value="确认" onclick="submit_a()"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				 	<input type="button" value="取消" onclick="window.close()"/>
				</td>
			</tr>
			</table>
		</div>
		</div>
        </div>
	</div>
	
</td></tr>

</table>
</form>
</td></tr></table>
</body>
<SCRIPT Language="javascript" FOR=authcode EVENT=onkeypress >
if(event.keyCode==13){
	submit_a();
}
</SCRIPT>
</html>
