<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/WEB-INF/tlds/birt.tld" prefix="birt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<BODY style="background-color: #F0F0F0;">

		<form id="form">
			<div>
				<h3 align="center" style="color: #84A9CF">Birt Integration With
					Web Application Using Ajax</h3>

				<div style="float: left; width: 25%;">
					<fieldset style="background-color: white;">
						<legend>
							<b style="color: #84A9CF">Select Reports</b>
						</legend>
						<ul>
							<li><a href="#">Hello World Report</a></li>
							<li><a href="#">Books Details Report</a></li>
						</ul>
					</fieldset>
				</div>

				<fieldset style="background-color: white;">
					<legend>
						<b style="color: #84A9CF">Report Details:</b>
					</legend>

					<div id="downloadOptions" align="right">
						Download Report as: <a wicket:id="pdf" href="#">pdf</a> <a
							href="#">xls</a>, <a href="#">doc</a>,

					</div>
					
					
					<birt:parameterPage id="paramPage"
						name="page1"
						reportDesign="IssusReport.rptdesign"
						isCustom="true"
						pattern="frameset"
						format="html"
						baseURL=""
						>
						Select State
						<birt:paramDef id="10" name="startDate" />
						<br><br>
						Select City
						<birt:paramDef id="11" name="endDate" />
						<br><br> 
						Select customer
						<birt:paramDef id="12" name="client" />
						<br><br> 
						<input type="Submit" value="Run Report">
											
					</birt:parameterPage>

					<%-- <birt:viewer id="birtViewer"
						reportDesign="IssusReport.rptdesign" pattern="frameset"
						height="500" width="200" format="pdf" scrolling="yes"
						showParameterPage="true"> --%>
						
						<%-- <birt:paramDef id="startDate" name="startDate" value="01/01/2011" />
						<birt:paramDef id="endDate" name="endDate" value="05/06/2014" />
						<birt:paramDef id="client" name="client" value="7" /> --%>
						
						<%-- <birt:param name="startDate"
							value="01/01/2011"></birt:param>
						<birt:param name="endDate"
							value="05/06/2014"></birt:param>
						<birt:param name="client"
							value="7"></birt:param> --%>
						
						<%-- <birt:param name="startDate"
							value='<%=request.getParameter("startDate")%>'></birt:param>
						<birt:param name="endDate"
							value='<%=request.getParameter("endDate")%>'></birt:param>
						<birt:param name="client"
							value='<%=request.getParameter("client")%>'></birt:param> --%>
					<%-- </birt:viewer> --%>

					<div id="reportData"
						style="height: auto; background-color: white; float: left; min-width: 70%; text-align: center;"
						align="center">
						<br> Reports will be loaded here when user selects report
						from left navigation.
					</div>
				</fieldset>
			</div>
		</form>
</BODY>
</HTML>