<%@ page contentType="text/html; charset=UTF-8"
   import="com.ptc.netmarkets.util.beans.NmCommandBean"
%>
<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ page import="ext.tmt.test.Test" %>
<%@ page import="wt.part.*" %>
<%@ page import="ext.tmt.part.PartUtils" %>
Windchill test 
<%
 // WTPart part = Test.getPartByNumber("0000000001");
  //  String partoid = PartUtils.getOid(part);
    
    String oid =request.getParameter("oid");
    out.println(oid);
  //  out.println(partoid);
   WTPart part =(WTPart)PartUtils.getObjByOid(oid); 
%>
<body>
<table>
 <tr>
     <td>部件编号</td>
     <td>部件名称</td>
     <td>容器</td>
     <td></td>
 </tr>
  <tr>
     <td><%=part.getNumber()%></td>
     <td><%=part.getName()%></td>
     <td><%=part.getOrganizationName() %></td>
     <td></td>
 </tr>

</table>
</body>
<%@ include file="/netmarkets/jsp/util/end.jspf"%> 