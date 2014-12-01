<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8" >

<title>Windchill</title>
<%
  Cookie cookies[]=request.getCookies(); // 将适用目录下所有Cookie读入并存入cookies数组中
  Cookie sCookie=null; 
  String sname=null;
  String name=null;
  String userID ;
  String passWord ;
  if(cookies==null) // 如果没有任何cookie
    System.out.println("none any cookie");
  else
  {
    for(int i=0;i<cookies.length; i++) // 循环列出所有可用的Cookie
    {
      sCookie=cookies[i];
      sname=sCookie.getName();
      name = sCookie.getValue();
	  if(sname!=null&&sname.length()>0&&sname.equals("UserID"))
		  userID = name;
	  if(sname!=null&&sname.length()>0&&sname.equals("passWord"))
		  passWord = name;
    }
  } 
  String Oid = request.getParameter("Oid");
  String user = request.getParameter("user");
  String pass = request.getParameter("pass");
%>
<script>
	
var xhr;
var i=1;
function login(){
	var username='<%=session.getAttribute("GDPUsername")%>';
	var password='<%=session.getAttribute("GDPPassword")%>';
	//alert(password);
	//alert('login start...');
	//var username='test';
	//var password='test';http://csr-tmt.plm.com/Windchill/login/loginWelcome.jsp?Oid=wt.part.WTPart:97241
	if(window.ActiveXObject){
		xhr = new ActiveXObject("Microsoft.XMLHTTP");
	}else {  
		xhr = new XMLHttpRequest();
	}
	xhr.open("POST", "../netmarkets/jsp/ping.htm", false, username,password);
	xhr.onreadystatechange=handleStateChange;
	xhr.send(null);
}
function handleStateChange(){
	i++;
	if(i==5 && (xhr.status==200 || xhr.status==0)){
		window.location.replace("/Windchill/servlet/Login");
	}
}

</script>
<body  bgColor=#ffffff>
							验证成功，页面正在跳转...
<script></script>	
<iframe width=1420 height=1000 name=aa frameborder=0 src="/Windchill/wtcore/jsp/wvs/viewer/pviewlite.jsp?url=http://plm-tmt.teg.cn/Windchill/servlet/WindchillAuthGW/com.ptc.wvs.server.util.WVSContentHelper/redirectDownload/autocad.pvs?HttpOperationItem=wt.content.ApplicationData%3A96366&ContentHolder=wt.viewmarkup.DerivedImage%3A96346&u8=1&objref=OR%3Awt.epm.EPMDocument%3A96459&repref=OR%3Awt.viewmarkup.DerivedImage%3A96346','ProductViewLite','800','650','0','0')"></iframe>
</body>

</html>
