 <%@ Page language="c#" Codebehind="UpdateRegistry.aspx.cs" AutoEventWireup="false" Inherits="registry.UpdateRegistry" %>
<% 
string title="Update Record"; 
if (InsertMode) title="Insert Record"; 
string meth="POST";
if (Request.Params["method"] !=null ) meth="GET";
Server.Execute("top.aspx?message="+title);
%>


		<%if (sres==null || InsertMode) {%>
		<form id="UpdateRegistry" method="get" runat="server">
		
			<table>
				<tr>
					<th class = "left">
					<%if (InsertMode) {%>
					<input name="InsertMode" value="t" type="hidden"/>To copy an existing record enter the Identifier here:
					<%}else {%>	
						Enter Identifier of record you wish to update:
					<%}%>
					</th>
					<td class="left" id="SearchID" style="HEIGHT: 26px"><asp:textbox id="SearchIdentifier" runat="server"></asp:textbox></td>
				</tr>
				<tr>
					<th>
						&nbsp;</th>
					<td class="left"><asp:button id="btnSubmit" runat="server" Text="Search"></asp:button></td>
				</tr>
			</table>
		</form>
		<%} 
		if (sres != null || InsertMode) {
			Session["res"]=sres;
			if (ro.Length > 0 ){ 
		%>
		<p class="warn">Read only view of data.</p>
		<% }else { %>
		<% if (!InsertMode) {%>
		<p>Enter your updates below</p>
		<%}%>
		<p class="warn"> Do not forget to enter your password <%if (InsertMode) {%>
		and a NEW identifier for inserting <% }else { %>
		for updating <%}%> the record.</p>
		<%}%>
	
			<form id="InsertEntry" action="RegistryAdmin.asmx/LoadFlatResource" method="<%=meth%>">
	
					
			<table border="1" class="filled" bordercolor="#6BA5D7">
				<tr>
					<th  class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083048">Title?</a></th>
					<td class="left" colspan="3" class="left" ><input <%=ro%> size="100" value="<%=(sres==null)?"":sres.Title%>" name=Title ></td>
				</tr>
		<%if (ro.Length > 0){%>
				<tr>
					<th  class="filled" >Harvested From</th>
					<td class="left" colspan="3" class="left" ><input <%=ro%> size="100" value="<%=(sres==null)?"":sres.harvestedfrom%>" name=harvestedfrom ></td>
				</tr>
		<%}else {%>
				<input <%=ro%> type=hidden size="100" value="<%=(sres==null)?"":sres.harvestedfrom%>" name=harvestedfrom >
		<%} %>
				<tr>
				<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083048">Shortname?</th> 
					<td class="left" ><input  <%=ro%> value="<%=(sres==null)?"":sres.ShortName%>" size="20" name="ShortName"></td>
					 <th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083048">Identifier?</th>
					<td class="left" ><input size="60" 
					<%= InsertMode? "": "readonly value=" + sres.Identifier%> name=Identifier></td>
				
				</tr>
				<tr>
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083049">
						ContactName?</th>
					<td class="left"><input  <%=ro%>  value="<%=(sres==null)?"":sres.CurationContactName%>" 
      name=ContactName></td>
				
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083049">
						ContactEmail?</th>
					<td class="left"><input  <%=ro%>  value="<%=(sres==null)?"":sres.CurationContactEmail%>" 
      name=ContactEmail></td>
				</tr>
		
				<tr>
					<th class="filled" ><a class="doc" href="Registryhelp.htm#_Toc68083049">
						Creator?</th>
					<td class="left"><input  <%=ro%> size="30" value="<%=(sres==null)?"":sres.CurationCreatorName%>" name=Creator 
      ></td>
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083049">
						Publisher?</th>
					<td class="left"><input  <%=ro%> size="40" value="<%=(sres==null)?"":sres.CurationPublisherName%>" name=Publisher 
      ></td>
				</tr>
		
				<tr>
				<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083049">
						Contributor?</th>
					<td class="left"><input  <%=ro%>  size="30" value="<%=(sres==null)?"":sres.CurationContributor%>" 
      name=Contributor></td>
  
					<th rowspan=2 class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083050">
						Subject?</th>
					<td rowspan=2 class="left"><textarea  name=Subject <%=ro%>  cols="40" ><%= (sres!=null&&sres.Subject!=null)? String.Join(",",sres.Subject): "" %> 
					
				      </textarea></td>
				</tr>
				      <tr>		
					<th class="filled">
						ResourceType</th><td class="left"><input  <%=ro%>  name="ResourceType" value="<%=(sres==null)?"":sres.ResourceType%>" >
						</td>
      </tr>
      

				<tr>
					<th  class="filled"><A class="doc" href="Registryhelp.htm#_Toc68083050">
						Description?</th>
					<td class="left" colspan="3"><textarea cols=80 rows=3 <%=ro%> name=Description /><%=(sres==null)?"":sres.Description%>
      </textarea></td>
      
				</tr>
				
<tr ><!-- RELATED RESOURCES -->
<th class="filled">Related <br> Resources</th>
<td class="left" colspan="3">
<div >
<table border="1" class="filled" bordercolor="#6BA5D7">
<% if (sres.resourceRelations != null) {
 for (int rel =0; rel < sres.resourceRelations.Length; rel++ ) {
  registry.ResourceRelation resrel = sres.resourceRelations[rel];
%>
<tr>
	<th class="filled"><%=resrel.relationshipType%></th>
	<td><a href="UpdateRegistry.aspx?ro=t&SearchIdentifier=<%=Server.UrlEncode(resrel.relatedResourceIvoId)%>"> <%=resrel.relatedResourceName%> 
	<%=resrel.relatedResourceIvoId%></a></td>
	<%if (ro ==null || ro == "") {%>
	<td><table><tr>	<td class="menusmall"> <a class="menusmall" href="soon.aspx"> remove</a></td>
</tr></table></td>
	<%}%>
</tr>
<%	}}%>
</table>
</div>
</td>
</tr> <!-- end related resources-->
								<tr>
			
					<th class="filled"><A class="doc" href="Registryhelp.htm#_Toc68083050">
						Type?</th>
					<td class="left"><input  <%=ro%>  value="<%=(sres==null)?"":sres.Type%>" name=Type 
      ></td>
			
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083051">
						Instrument?</th><td class="left"><input  <%=ro%> size="50" name="Instrument" value="<%=(sres!=null&&sres.Instrument!=null)? String.Join(",",sres.Instrument):""%>" ></td>
				</tr>

				<tr>
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083049">
						Date?</th>
					<td class="left"><input  <%=ro%>  value="<%=(sres==null)?"":sres.CurationDate.ToString()%>" name=Date 
      ></td>
				
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083049">
						Version?</th>
					<td class="left"><input  <%=ro%>  value="<%=(sres==null)?"":sres.CurationVersion%>" name=Version 
      ></td>
				</tr>
			
				<tr>
					<th class="filled"><A class="doc" href="Registryhelp.htm#_Toc68083050">
						ReferenceURL?</th>
					<td class="left">
				<% if (ro.Length > 0 ) {
				    if (sres!=null) { %>
					<a href="<%=sres.ReferenceURL%>"><%=sres.ReferenceURL%></a> 
				<% }} else { %>
				<input size="40" <%=ro%>  value="<%=(sres==null)?"":sres.ReferenceURL%>" 

      name=ReferenceURL>
				<%}%>
				</td>
				
					<th class="filled"><A class="doc" href="Registryhelp.htm#_Toc68083054">
						ServiceURL?</th>
					<td class="left">
				<% if (ro.Length > 0 ) {
				    if (sres!=null) { %>
					<a href="<%=sres.ServiceURL%>"><%=sres.ServiceURL%></a> 
				<% }} else { %>
					<input  size="40" <%=ro%>  value="<%=(sres==null)?"":sres.ServiceURL%>" name=ServiceURL>
				<%}%>
				</td>
				</tr>
<tr>
					<th  class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083051">
						CoverageSpatial?</th>
					<td class="left"><textarea rows=2 cols="40" <%=ro%> name=CoverageSpatial ><%=(sres==null)?"":registry.votxslt.transformRegion(sres.CoverageSpatial)%> 
      </textarea></td>
      					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083051">
						CoverageTemporal?</th>
					<td class="left"><textarea rows=2 cols="40"  <%=ro%>  name=CoverageTemporal><%=(sres==null)?"":sres.CoverageTemporal%> 
					</textarea></td>

</tr>
				<tr>
						<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083051">RegionOfRegard?
						</th>
					<td class="left"><input  <%=ro%>  
						value="<%=(sres==null)?0:sres.CoverageRegionOfRegard%>" name=EntrySize 
      ></td>
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083051">
						CoverageSpectral?</th>
					<td class="left"><input size="40"  <%=ro%>  value="<%=(sres!=null&&sres.CoverageSpectral!=null)? String.Join(",",sres.CoverageSpectral):""%>" 
      name=CoverageSpectral></td>
				

				</tr>
				

				
								<tr>
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083050">
						ContentLevel?</th><td class="left"><input  <%=ro%>  name="ContentLevel" value="<%=(sres!=null&&sres.ContentLevel!=null)
						? String.Join(",",sres.ContentLevel): "" %>" ></td>
				
					<th class="filled"><a class="doc" href="Registryhelp.htm#_Toc68083051">
						Facility?</th><td class="left"><input size="40" <%=ro%>  name="Facility" value="<%=(sres==null)?"":sres.Facility%>" ></td>
				</tr>

												<tr>
					<th class="filled">
						ModificationDate</th><td class="left"><input readonly  value="<%=(sres!=null)? sres.ModificationDate.ToString(): ""%>" ></td>
					<th class="filled">
						ValidationLevel</th><td class="left"><input <%=ro%>  name="validationLevel"  value="<%=(sres!=null)? sres.validationLevel.ToString(): ""%>" ></td>
						
				     <% if (sres == null || sres.GetType() == typeof (registry.DBResource)){ %>
				     				<input type=hidden name="MaxQueryRegionSizeLat" value="0"/>
				<input type=hidden name="MaxQueryRegionSizeLong" value="0"/>
				<input type=hidden name="ImageServiceType" value=""/>
				<input type=hidden name="MaxFileSize" value="0"/>
				<input type=hidden name="MaxImageSizeLat" value="0"/>
				<input type=hidden name="MaxImageSizeLong" value="0"/>
				<input type=hidden name="MaxImageExtentLat" value="0"/>
				<input type=hidden name="MaxImageExtentLong" value="0"/>
				<input type=hidden name="MaxSR" value="0"/>
				<input type=hidden name="MaxRecords" value="0"/>
				<input type=hidden name="Format" value=""/>
				<input type=hidden name="VOTableColumns" value=""/>
				<input type=hidden name="Compliance" value=""/>
				<input type=hidden name="Longitude" value="0"/>
				<input type=hidden name="Latitude" value="0"/>					
				<input type=hidden name="PrimaryTable" value="0"/>					
				<input type=hidden name="PrimaryKey" value="0"/>					

				     <%}%>
						
				</tr>	
				<tr ><!-- Interfaces -->
<th class="filled">Interfaces</th>
<td class="left" colspan="3">
<div >
<table border="1" class="filled" bordercolor="#6BA5D7">
<% if (sres.resourceInterfaces != null) {
 for (int inf =0; inf < sres.resourceInterfaces.Length; inf++ ) {
  registry.ResourceInterface intf = sres.resourceInterfaces[inf];
%>
<tr>
<th class=filled>Number</th>
<th class=filled>Type</th>
<th class=filled>QType</th>
<th class=filled>ResultType</th>
<th class=filled>AccessURL</th>
</tr>
<tr>
<%if (ro ==null || ro == "") {%>
	<td><table><tr>	<td class="menusmall"> <a class="menusmall" href="soon.aspx"> remove</a></td>
</tr></table></td>
	<%}%>
	<td><table><tr>	<td class="menusmall"> <a class="menusmall" href="ShowParams.aspx?interfaceNum=<%=intf.interfaceNum%>"> Params</a></td>
</tr></table></td>

	<td><%=intf.interfaceNum%></td>
	<td><%=intf.type%></td>
	<td><%=intf.qtype%></td>
	<td><%=intf.resultType%></td>
	<td class="left"><a  href="<%=intf.accessURL%>"> <%=intf.accessURL%> </a></td>
</tr>
<%	}}%>
</table>
</div>
</td>
</tr> <!-- end interfaces-->					

<tr><th class="filled">Footprint</th><td class="left" colspan="3"><input size="100"  <%=ro%>  value="<%=(sres==null)?"":sres.footprint%>" 
      name=footprint></td></tr>

				
				<%if (sres!=null && sres.GetType() == typeof (registry.ServiceCone)) {
				registry.ServiceCone sc = (registry.ServiceCone)sres;
				%>
				<tr><td class="left" colspan="4">
				<table>
				<tr><th colspan="6" class="filled">ConeSearch</th></tr>
				<tr>
						<th class="filled">VOTableColumns</th><td colspan="5">
						<input size="100" <%=ro%> 
							value="<%=(sc==null)?"":sc.VOTableColumns%>" name = "VOTableColumns"/>
						</td></tr>
				<tr>
				
				
					<th class="filled">
						MaxSR</th><td class="left"><input  <%=ro%>  name="MaxSR" 
							value="<%=(sc==null)?0:sc.MaxSearchRadius%>" ></td>
				
					<th class="filled">MaxRecords</th>
						<td class="left"><input  <%=ro%>  name="MaxRecords" 
						value="<%=(sc==null)?0:sc.MaxRecords%>" >
						
				<input type=hidden name="MaxQueryRegionSizeLat" value="0"/>
				<input type=hidden name="MaxQueryRegionSizeLong" value="0"/>
				<input type=hidden name="ImageServiceType" value=""/>
				<input type=hidden name="MaxFileSize" value="0"/>
				<input type=hidden name="MaxImageSizeLat" value="0"/>
				<input type=hidden name="MaxImageSizeLong" value="0"/>
				<input type=hidden name="MaxImageExtentLat" value="0"/>
				<input type=hidden name="MaxImageExtentLong" value="0"/>
				<input type=hidden name="Compliance" value=""/>
				<input type=hidden name="Longitude" value="0"/>
				<input type=hidden name="Latitude" value="0"/>	
				<input type=hidden name="Format" value=""/>
				<input type=hidden name="PrimaryTable" value="0"/>					
				<input type=hidden name="PrimaryKey" value="0"/>					

	
						</td>
				</tr>
				
				
				</table></td>
			
		

			<%}
			if (sres!=null &&sres.GetType() == typeof (registry.ServiceSimpleImageAccess)){
			registry.ServiceSimpleImageAccess sia = (registry.ServiceSimpleImageAccess)sres; 
			%>
		<tr><td class="left" colspan="4">
		<table>
			
				<tr><th colspan="6" class="filled">Simple Image Access</th></tr>
				</tr>
								<tr>
					<th class="filled">
						Format</th><td class="left" colspan="4"><input  <%=ro%> size="60" 
						name="Format" value="<%=(sres!=null&&sia.Format!=null)? String.Join(",",sia.Format):""%>" ></td>
				</tr>

				<tr>
						<th class="filled">VOTableColumns</th><td colspan="4">
						<input size="100" <%=ro%> value="<%=(sia==null)?"":sia.VOTableColumns%>" 
						name = "VOTableColumns"/>
						</td></tr>
				<tr>
					<th class="filled">
						ImageServiceType</th>
					<td class="left"><input  <%=ro%>  
					value="<%=(sia==null)?"Archive":sia.ImageServiceType%>" name="ImageServiceType"/></td>
					<th class="filled">
						MaxqueryRegionSizeLat</th><td class="left"><input  <%=ro%>  name="MaxQueryRegionSizeLat" 
						value="<%=(sia==null)?0:sia.MaxQueryRegionSizeLat%>" ></td>
					<th class="filled">
						MaxqueryRegionSizeLong</th><td class="left"><input  <%=ro%>  name="MaxQueryRegionSizeLong" 
						value="<%=(sia==null)?0:sia.MaxQueryRegionSizeLong%>" ></td>
				</tr>
				<tr>
					<th class="filled">
						MaxRecords</th><td class="left"><input  <%=ro%>  name="MaxRecords" 
						value="<%=(sres==null)?0:sia.MaxRecords%>" ></td>					
					<th class="filled">
						MaxImageExtentLat</th><td class="left"><input  <%=ro%>  name="MaxImageExtentLat" 
						value="<%=(sia==null)?0:sia.MaxImageExtentLat%>" ></td>
					<th class="filled">
						MaxImageExtentLong</th><td class="left"><input  <%=ro%>  name="MaxImageExtentLong" 
						value="<%=(sia==null)?0:sia.MaxImageExtentLong%>" ></td>
				</tr>
				<tr>
					<th class="filled">
						MaxFileSize</th><td class="left"><input  <%=ro%>  name="MaxFileSize" 
						value="<%=(sia==null)?0:sia.MaxFileSize%>" ></td>					
					<th class="filled">
						MaxImageSizeLat</th><td class="left"><input  <%=ro%>  name="MaxImageSizeLat" 
						value="<%=(sia==null)?0:sia.MaxImageSizeLat%>" ></td>
					<th class="filled">
						MaxImageSizeLong</th><td class="left"><input  <%=ro%>  name="MaxImageSizeLong" 
						value="<%=(sia==null)?0:sia.MaxImageSizeLong%>" >
				<input type=hidden  name="MaxSR" value="0"/>
				<input type=hidden name="Compliance" value=""/>
				<input type=hidden name="Longitude" value="0"/>
				<input type=hidden name="Latitude" value="0"/>	
								<input type=hidden name="PrimaryTable" value="0"/>					
				<input type=hidden name="PrimaryKey" value="0"/>					


						</td>

				</tr>

				<tr>
			
							
			
				</tr>
	
				
				</table></td>
			<%}
			if (sres!=null && sres.GetType() == typeof (registry.ServiceSkyNode)){
			registry.ServiceSkyNode sn = (registry.ServiceSkyNode)sres; 
			%>
		
		<tr><td class="left" colspan="4">
				<table>
								
				
				<tr>
					<th class="filled">
						SkyNode Compliance</th>
					<td class="left"><input  <%=ro%>  
					value="<%=(sn==null)?"":sn.Compliance%>" name="Compliance">
					
				<input type=hidden name="MaxQueryRegionSizeLat" value="0"/>
				<input type=hidden name="MaxQueryRegionSizeLong" value="0"/>
				<input type=hidden name="ImageServiceType" value=""/>
				<input type=hidden name="MaxFileSize" value="0"/>
				<input type=hidden name="MaxImageSizeLat" value="0"/>
				<input type=hidden name="MaxImageSizeLong" value="0"/>
				<input type=hidden name="MaxImageExtentLat" value="0"/>
				<input type=hidden name="MaxImageExtentLong" value="0"/>
				<input type=hidden name="MaxSR" value="0"/>
				<input type=hidden name="Format" value=""/>
				<input type=hidden name="VOTableColumns" value=""/>
					</td>
					<th class="filled">
						Longitude</th>
					<td class="left"><input  <%=ro%>  
					value="<%=(sn==null)?-1:sn.Longitude%>" name="Longitude">
					</td>
					<th class="filled">
						Latitude</th>
					<td class="left"><input  <%=ro%>  
					value="<%=(sn==null)?-1:sn.Latitude%>" name="Latitude">
					</td>

				</tr>
				<tr>
					<th class="filled">
						MaxRecords</th><td class="left"><input  <%=ro%>  name="MaxRecords" 
						value="<%=(sn==null)?0:sn.MaxRecords%>" ></td>					

					<th class="filled">
						PrimaryTable</th>
					<td class="left">
						<input  <%=ro%>  
							value="<%=(sn==null)?"":sn.PrimaryTable%>"name="PrimaryTable" />					
					</td>
					<th class="filled">
						PrimaryKey</th>
					<td class="left">
						<input  <%=ro%>  
							value="<%=(sn==null)?"":sn.PrimaryKey%>"name="PrimaryKey" />					
					</td>

				</tr>
				</table></td>

				</tr>
				<%}%>

				<% if (ro == null || ro.Length == 0) {%>
				<tr>
					<th class="filled">
						PassPhrase</th><td class="left"><input  <%=ro%>  name="PassPhrase" type="password"></td>
				</tr>

				<tr>
					<td class="left">&nbsp;</td>
					<td class="left"><input name="update" type="submit" value="<%=title%>"></td>
				</tr>
				
				<%}%>
			</table>
		</form>
		
		<%}//End if for Valid SimpleResource%>
	</body>
</HTML>
