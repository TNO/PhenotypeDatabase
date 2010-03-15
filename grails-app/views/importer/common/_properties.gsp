<%
	/**
	 * Properties template which shows entities and allows to assign properties to columns
	 *
	 * @author Tjeerd Abma
	 * @since 20100210
	 * @package importer
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<g:form name="propertiesform" action="saveproperties">
    <table>
	  <g:each var="stdentity" in ="${standardentities}">
	      <tr><td colspan="2"><h4>${stdentity.name}</h4></td></tr>
	      <tr>
		  <td>Identifier:</td>
		  <td>Columnname:</td>
		  <td>Property:</td>
	      </tr>	      
		<g:each var="selentity" in="${selectedentities}">
		    <g:if test="${selentity.type.toLong()==stdentity.type}">
			<tr>
			    <td width="12px">
				<g:checkBox name="identifier"/>
			    </td>
			    <td class="header" width="200px">
				<b>${header[selentity.columnindex.toInteger()].name}</b>
			    </td>
			    <td>				
				<importer:propertyChooser name="columnproperty" entity="${selentity.type.toLong()}" columnindex="${selentity.columnindex}"/>
			    </td>
			</tr>
		    </g:if>
		</g:each>
	      <tr>
		  <td colspan="2">
		      <hr />
		  </td>
	      </tr>
	  </g:each>
	<tr>
	    <td>
		<input type="submit" name="savebutton" value="Next"/>
	    </td>
	</tr>

    </table>
</g:form>
