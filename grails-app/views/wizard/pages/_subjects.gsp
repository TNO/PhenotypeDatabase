<%
/**
 * Subjects page
 *
 * @author  Jeroen Wesbeek
 * @since   20100113
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<wizard:pageContent>
	<span class="info">
		<span class="title">Add subjects to your study</span>
		In this screen you can add subjects to your study based on a given template. Note that the 'species' select will probably
		move to the template as an ontology reference and will not be asked anymore.<br/>
		<i>Note that you can edit multiple subjects at once by selecting multpiple rows by either ctrl-clicking them or dragging a selection over them.</i>
	</span>

	<wizard:textFieldElement name="addNumber" description="Number of subjects to add" error="addNumber" value="1" size="4" maxlength="4">
		The number of subjects to add to your study
	</wizard:textFieldElement>
	<wizard:termElement name="species" description="of species" value="" ontology="1132">
		The species of the subjects you would like to add to your study
	</wizard:termElement>
	<wizard:templateElement name="template" description="with template" value="" error="template" entity="${dbnp.studycapturing.Subject}" >
		The template to use for this study
	</wizard:templateElement>
	<wizard:ajaxButtonElement name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()">
	</wizard:ajaxButtonElement>


<g:if test="${subjects}">
	<g:each var="subjectTemplate" in="${subjectTemplates}">
		<g:set var="showHeader" value="${true}" />
		<h1>${subjectTemplate.getValue().name} template</h1>
		<div class="table">
		<g:each var="subjectId" in="${subjectTemplate.getValue().subjects}">
		  	<g:if test="${showHeader}">
			<g:set var="showHeader" value="${false}" />
			<div class="header">
				<div class="firstColumn">#</div>
				<div class="firstColumn"></div>
				<wizard:templateColumnHeaders entity="${subjects[ subjectId ]}" class="column" />				
			</div>
			</g:if>
			<div class="row">
				<div class="firstColumn">${subjectId + 1}</div>
				<div class="firstColumn">
					<wizard:ajaxButton name="delete" src="../images/icons/famfamfam/delete.png" alt="delete this subject" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${subjectId});" afterSuccess="onWizardPage()" />
				</div>
				<wizard:templateColumns id="${subjectId}" entity="${subjects[ subjectId ]}" template="${subjects[ subjectId ].template}" name="subject_${subjectId}" class="column" subject="${subjects[ subjectId ]}" />
			</div>
		</g:each>
		</div>
		<div class="sliderContainer">
			<div class="slider" ></div>
		</div>
	</g:each>
</g:if>

	<g:if env="development">
	<span class="info">
		<span class="known">Known issues</span>
		<ul>
			<li>autocomplete fields (like ontologies) deselect the selected rows and hence don't replicate</li>
			<li>ontology fields should replicate value <i>and</i> hidden fields</li>
			<li>ontology hidden fields should be processed by the back-end as well (not yet implemented)</li>
			<li>ontology fields now show suggestions for <i>all</i> available ontologies. This has to be narrowed down in the future.</li>
			<li>ontology hidden fields should be handled by the taglibrary / controller as well</li>
		</ul>
	</span>
	</g:if>

</wizard:pageContent>