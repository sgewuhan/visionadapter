<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<html>
<head>
<%@ page import="ext.tmt.utils.*"%>
<%@ page import="wt.folder.SubFolder" %>
<%@ page import="wt.inf.container.WTContainer" %>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8" >

<title>Windchill</title>
<%
  String user = "pm-rw";//request.getParameter("username");
  String pass = "PM.plm2014";//request.getParameter("password");

  //String containerName=new String(request.getParameter("containerName").getBytes("ISO-8859-1"),"UTF-8");
  String folderOid=request.getParameter("folderOid");
  String containerOid="";
  System.out.println("folderOid---"+folderOid);


 SubFolder folder= (SubFolder)WindchillUtil.getObjectByOid(folderOid);
   System.out.println("folder---"+folder);
    containerOid=WindchillUtil.getContaintOidByFolder(folder);
   System.out.println("containerOid----"+containerOid);;
%>
<script>
	
var xhr;
var i=1;
function login(){
	var username="<%=user%>";
	var password="<%=pass%>";
	if(window.ActiveXObject){
		xhr = new ActiveXObject("Microsoft.XMLHTTP");
	}else {  
		xhr = new XMLHttpRequest();
	}
	xhr.open("POST", "../netmarkets/jsp/ping.htm", false,username,password);
	xhr.onreadystatechange=handleStateChange;
	
	xhr.send(null);
}

function handleStateChange(){
	i++;
	if(i==5 && (xhr.status==200 || xhr.status==0)){
		window.location.replace("/Windchill/servlet/Navigation");
	}
}

var iframe = document.createElement("iframe");
iframe.src = "http://plm-tmt.teg.cn/Windchill/app/#ptc1/tcomp/infoPage?oid=OR:<%=folderOid%>&ContainerOid=OR:<%=containerOid%>&u8=1";

if (iframe.attachEvent){
    iframe.attachEvent("onload", login();
    } else {
    iframe.onload = login();
}

document.body.appendChild(iframe);

</script>
</head>
<body  bgColor=#ffffff>							
<script>login();</script>	
<iframe width=1360 height=1030 name=aa frameborder=0 src="http://plm-tmt.teg.cn/Windchill/app/#ptc1/tcomp/infoPage?oid=OR:<%=folderOid%>&ContainerOid=OR:<%=containerOid%>&u8=1"></iframe>
</body>

</html>