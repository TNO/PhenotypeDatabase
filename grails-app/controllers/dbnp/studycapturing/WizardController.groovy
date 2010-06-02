package dbnp.studycapturing

import dbnp.data.*

// Grails convertors is imported in order to create JSON objects
import grails.converters.*


/**
 * Wizard Controler
 *
 * The wizard controller handles the handeling of pages and data flow
 * through the study capturing wizard.
 *
 * @author Jeroen Wesbeek
 * @since 20100107
 * @package studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class WizardController {
	/**
	 * index method, redirect to the webflow
	 * @void
	 */
	def index = {
		/**
		 * Do you believe it in your head?
		 * I can go with the flow
		 * Don't say it doesn't matter (with the flow) matter anymore
		 * I can go with the flow (I can go)
		 * Do you believe it in your head?
		 */
		redirect(action: 'pages')
	}

	/**
	 * WebFlow definition
	 * @see http://grails.org/WebFlow
	 * @void
	 */
	def pagesFlow = {
		// start the flow
		onStart {
			// define flow variables
			flow.page = 0
			flow.pages = [
				//[title: 'Templates'],			// templates
				[title: 'Start'],				// load or create a study
				[title: 'Study'],				// study
				[title: 'Subjects'],			// subjects
				[title: 'Events'],				// events and event grouping
				[title: 'Groups'],				// groups
				[title: 'Samples'],				// samples
				[title: 'Confirmation'],		// confirmation page
				[title: 'Done']					// finish page
			]
			success()
		}

		// render the main wizard page which immediately
		// triggers the 'next' action (hence, the main
		// page dynamically renders the study template
		// and makes the flow jump to the study logic)
		mainPage {
			render(view: "/wizard/index")
			onRender {
				flow.page = 1
				success()
			}
			on("next").to "start"
		}

		// create or modify a study
		start {
			render(view: "_start")
			onRender {
				flow.page = 1
				success()
			}
			on("next").to "study"
			on("modify").to "modify"
		}

		// load a study to modify
		modify {
			render(view: "_modify")
			onRender {
				flow.page = 1
				flash.cancel = true
				success()
			}
			on("cancel") {
				flow.study = null

				success()
			}.to "start"
			on("next") {
				// TODO: loading a study is not yet implemented
				//       create a error stating this feature is
				//       not yet implemented
				flash.errors = [:]
				this.appendErrorMap(
					['study': 'Loading a study and modifying it has not yet been implemented. Please press \'cancel\' to go back to the initial page...'],
					flash.errors
				)

				error()
			}.to "modify"
		}

		// render and handle the study page
		// TODO: make sure both template as well as logic will
		//       handle Study templates as well!!!
		study {
			render(view: "_study")
			onRender {
				flow.page = 2
				success()
			}
			on("refresh") {
				flash.values = params

				// handle study data
				this.handleStudy(flow, flash, params)

				// remove errors as we don't want any warnings now
				flash.errors = [:]

				success()
			}.to "study"
			on("switchTemplate") {
				flash.values = params

				// handle study data
				this.handleStudy(flow, flash, params)

				// remove errors as we don't want any warnings now
				flash.errors = [:]

				success()
			}.to "study"
			on("previous") {
				flash.errors = [:]

				// handle the study
				this.handleStudy(flow, flash, params)

				// reset errors
				flash.errors = [:]

				success()
			}.to "start"
			on("next") {
				flash.errors = [:]

				if (this.handleStudy(flow, flash, params)) {
					success()
				} else {
					error()
				}
			}.to "subjects"
		}

		// render and handle subjects page
		subjects {
			render(view: "_subjects")
			onRender {
				flow.page = 3

				if (!flow.subjects) {
					flow.subjects = [:]
					flow.subjectTemplates = [:]
				}
				success()
			}
			on("refresh") {
				flash.values = params
				success()
			}.to "subjects"
			on("add") {
				// handle subjects
				this.handleSubjects(flow, flash, params)

				flash.errors = [:]
				flash.values = params
				def speciesTerm = Term.findByName(params.species);
				def subjectTemplateName = params.get('template');
				def subjectTemplate = Template.findByName(subjectTemplateName);

				// add this subject template to the subject template array
				if (!flow.subjectTemplates[ subjectTemplateName ]) {
					flow.subjectTemplates[ subjectTemplateName ] = [
						name: subjectTemplateName,
						template: subjectTemplate,
						subjects: [:]
					]
				}

				// add x subjects of species y
				(params.addNumber as int).times {
					def increment = (flow.subjects.size()) ? (flow.subjects.keySet().max() + 1) : 0
					def subject = new Subject(
						name: 'Subject ' + (increment + 1),
						species: speciesTerm,
						template: subjectTemplate
					)

					// instantiate a new Subject
					flow.subjects[ increment ] = subject

					// and remember the subject id with the template
					def subjectsSize = (flow.subjectTemplates[ subjectTemplateName ].subjects.size()) ? (flow.subjectTemplates[ subjectTemplateName ].subjects.keySet().max() + 1) : 0
					flow.subjectTemplates[ subjectTemplateName ].subjects[ subjectsSize ] = increment
				}

				success()
			}.to "subjects"
			on("next") {
				flash.errors = [:]

				// check if we have at least one subject
				// and check form data
				if (flow.subjects.size() < 1) {
					// append error map
					this.appendErrorMap(['subjects': 'You need at least to create one subject for your study'], flash.errors)
					error()
				} else if (!this.handleSubjects(flow, flash, params)) {
					error()
				} else {
					success()
				}
			}.to "events"
			on("delete") {
				// handle subjects
				this.handleSubjects(flow, flash, params)

				flash.errors = [:]
				def delete = params.get('do') as int;

				// remove subject
				if (flow.subjects[ delete ] && flow.subjects[ delete ] instanceof Subject) {
					// remove subject from templates
					flow.subjectTemplates.each() { templateName, templateData ->
						templateData.subjects.remove( delete )
					}

					// remove subject altogether
					flow.subjects.remove( delete )
				}
			}.to "subjects"
			on("previous") {
				flash.errors = [:]

				// handle form data
				if (!this.handleSubjects(flow, flash, params)) {
					error()
				} else {
					success()
				}
			}.to "study"
		}

		// render events page
		events {
			render(view: "_events")
			onRender {
				flow.page = 4

				if (!flow.event) {
					flow.event			= new Event()
					flow.events			= []
					flow.eventGroups	= []
					flow.eventGroups[0]	= new EventGroup(name: 'Group 1')	// 1 group by default
					flow.eventTemplates	= [:]
				} else if (!flash.values) {
					// set flash.values.templateType based on the event instance
					flash.values = [:]
					flash.values.templateType = (flow.event instanceof Event) ? 'event' : 'sample'
				}
				success()
			}
			on("switchTemplate") {
				flash.values = params

				// handle study data
				this.handleEvents(flow, flash, params)

				// remove errors as we don't want any warnings now
				flash.errors = [:]
			}.to "events"
			on("add") {
				flash.values			= params
				def eventTemplateName	= (params.get('eventType') == 'event') ? params.get('eventTemplate') : params.get('sampleTemplate')
				def eventTemplate		= Template.findByName(eventTemplateName)

				// handle study data
				this.handleEvents(flow, flash, params)

				// validate event object
				if (flow.event.validate()) {
					// add this event template to the event template array
					if (!flow.eventTemplates[ eventTemplateName ]) {
						flow.eventTemplates[ eventTemplateName ] = [
							name: eventTemplateName,
							template: eventTemplate,
							events: []
						]
					}

					// it validated! Duplicate the event object...
					def newEvent	= flow.event
					def increment	= flow.events.size()

					// ...store it in the events map in the flow scope...
					flow.events[ increment ] = newEvent

					// ...and 'reset' the event object in the flow scope
					flow.event = new Event(template: newEvent.template)
					
					// remember the event id with the template
					def eventSize = flow.eventTemplates[ eventTemplateName ]['events'].size()
					flow.eventTemplates[ eventTemplateName ]['events'][ eventSize ] = increment

					success()
				} else {
					// it does not validate, show error feedback
					flash.errors = [:]
					this.appendErrors(flow.event, flash.errors)
					error()
				}
			}.to "events"
			on("deleteEvent") {
				flash.values = params
				def delete = params.get('do') as int;

				// handle event groupings
				this.handleEventGrouping(flow, flash, params)

				// remove event
				if (flow.events[ delete ] && flow.events[ delete ] instanceof Event) {
					flow.events.remove(delete)
				}

				success()
			}.to "events"
			on("addEventGroup") {
				flash.values = params
				
				// handle study data
				this.handleEvents(flow, flash, params)

				// handle event groupings
				this.handleEventGrouping(flow, flash, params)

				def increment = flow.eventGroups.size()
				def groupName = "Group " + (increment + 1)

				// check if group name exists
				def nameExists = true
				def u = 0

				// make sure a unique name is generated
				while (nameExists) {
					u++
					def count = 0
					
					flow.eventGroups.each() {
						if (it.name == groupName) {
							groupName = "Group " + (increment + 1) + "," + u
						} else {
							count++
						}
					}

					nameExists = !(count == flow.eventGroups.size())
				}

				flow.eventGroups[increment] = new EventGroup( name: groupName )

				success()
			}.to "events"
			on("deleteEventGroup") {
				flash.values = params
				def delete = params.get('do') as int;

				// handle event groupings
				this.handleEventGrouping(flow, flash, params)

				// remove the group with this specific id
				if (flow.eventGroups[delete] && flow.eventGroups[delete] instanceof EventGroup) {
					// remove this eventGroup
					flow.eventGroups.remove(delete)
				}

				success()
			}.to "events"
			on("previous") {
				// handle event groupings
				this.handleEventGrouping(flow, flash, params)
			}.to "subjects"
			on("next") {
				flash.values = params
				flash.errors = [:]

				// handle study data
				if (flow.events.size() < 1) {
					// append error map
					this.appendErrorMap(['events': 'You need at least to create one event for your study'], flash.errors)
					error()						
				} else if (this.handleEvents(flow, flash, params)) {
					success()
				} else {
					error()
				}
			}.to "groups"
		}

		// groups page
		groups {
			render(view: "_groups")
			onRender {
				flow.page = 5
				success()
			}
			on("previous") {
				this.handleSubjectGrouping(flow, flash, params)
				success()
			}.to "events"
			on("next") {
				this.handleSubjectGrouping(flow, flash, params)
				success()
			}.to "samples"
		}

		// samples page
		samples {
			render(view: "_samples")
			onRender {
				flow.page = 6
				flow.bla = "samples"

				// iterate through subjects
				flow.subjects.each() { subject ->
					println subject.value.name

					// iterate through events
					flow.events.each() { event ->
						println "bla"
						if (event instanceof SamplingEvent) {
							//println event.getFieldValue('name')
							println event.startTime
							println event.endTime
						}

					}
				}

				success()
			}
			on("previous") {
				success()
			}.to "groups"
			on("next") {
				success()
			}.to "confirm"
		}

		// confirmation
		confirm {
			render(view: "_confirmation")
			onRender {
				flow.page = 7
			}
			on("toStudy").to "study"
			on("toSubjects").to "subjects"
			on("toEvents").to "events"
			on("toGroups").to "groups"
			on("previous").to "samples"
			on("next").to "waitForSave"
		}

		waitForSave {
			render(view: "_wait")
			onRender {
				flow.page = 8
			}
			on("next").to "save"
		}

		// store all study data
		save {
			action {
				println "saving..."
				flash.errors = [:]

				// persist data to the database
				try {
					println ".saving wizard data..."

					// add events to study
					println ".add events to study"
					flow.events.each() { event ->
						if (event instanceof SamplingEvent) {
							flow.study.addToSamplingEvents(event)
						} else {
							flow.study.addToEvents(event)
						}
					}

					// add subjects to study
					println ".add subjects to study"
					flow.subjects.each() { subjectId, subject ->
						flow.study.addToSubjects(subject)
					}

					// add eventGroups to study
					println ".add eventGroups to study"
					flow.eventGroups.each() { eventGroup ->
						flow.study.addToEventGroups(eventGroup)
					}

					// save study
					println ".saving study"
					if (!flow.study.save(flush:true)) {
						this.appendErrors(flow.study, flash.errors)
						throw new Exception('error saving study')
					}
					println ".saved study "+flow.study+" (id: "+flow.study.id+")"

					success()
				} catch (Exception e) {
					// rollback
					this.appendErrorMap(['exception': e.toString() + ', see log for stacktrace' ], flash.errors)

					// stacktrace in flash scope
					flash.debug = e.getStackTrace()

					error()
				}
			}
			on("error").to "error"
			on(Exception).to "error"
			on("success").to "done"
		}

		// error storing data
		error {
			render(view: "_error")
			onRender {
				flow.page = 7
			}
			on("next").to "save"
			on("previous").to "samples"
		}

		// render page three
		done {
			render(view: "_done")
			onRender {
				flow.page = 8
			}
			on("previous") {
				// TODO
			}.to "confirm"
		}
	}

	/**
	 * re-usable code for handling study form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleStudy(flow, flash, params) {
		// create study instance if we have none
		if (!flow.study) flow.study = new Study();

		// create date instance from date string?
		// @see WizardTagLibrary::dateElement{...}
		if (params.get('startDate')) {
			params.startDate = new Date().parse("d/M/yyyy", params.get('startDate').toString())
		} else {
			params.remove('startDate')
		}

		// if a template is selected, get template instance
		def template = params.remove('template')
		if (template instanceof String && template.size() > 0) {
			flow.study.template = Template.findByName(template)
		} else if (template instanceof Template) {
			flow.study.template = template
		}

		// iterate through fields
		if (flow.study.template) {
			flow.study.giveFields().each() {
				flow.study.setFieldValue(it.name, params.get(it.escapedName()))
			}
		}

		// validate study
		if (flow.study.validate()) {
			return true
		} else {
			// validation failed, feedback errors
			flash.errors = [:]
			this.appendErrors(flow.study, flash.errors)
			return false
		}
	}

	/**
	 * re-usable code for handling subject form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleSubjects(flow, flash, params) {
		def names = [:];
		def errors = false;
		def id = 0;

		// iterate through subject templates
		flow.subjectTemplates.each() { subjectTemplate ->
			// iterate through subjects
			subjectTemplate.getValue().subjects.each() { subjectIncrement, subjectId ->
				// iterate through fields (= template fields and domain properties)
				flow.subjects[ subjectId ].giveFields().each() { subjectField ->
					// set the field
					flow.subjects[ subjectId ].setFieldValue(
						subjectField.name,
						params.get( 'subject_' + subjectId + '_' + subjectField.escapedName() )
					)
				}

				// validate subject
				if (!flow.subjects[ subjectId ].validate()) {
					errors = true
					this.appendErrors(flow.subjects[ subjectId ], flash.errors, 'subject_' + subjectId + '_')
				}
			}
		}

		return !errors
	}

	/**
	 * re-usable code for handling event form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleEvents(flow, flash, params) {
		def errors = false
		def template = null

		// handle the type of event
		if (params.eventType == 'event') {
			flow.event = new Event();
			template = params.remove('eventTemplate')
		} else if (params.eventType == 'sample') {
			flow.event = new SamplingEvent();
			template = params.remove('sampleTemplate')
		}

		// if a template is selected, get template instance
		if (template instanceof String && template.size() > 0) {
			params.template = Template.findByName(template)
		} else if (template instanceof Template) {
			params.template = template
		} else {
			params.template = null
		}

		// set template
		if (params.template) flow.event.template = params.template

		// update event instance with parameters
		flow.event.giveFields().each() { eventField ->
			flow.event.setFieldValue(eventField.name, params[ eventField.escapedName() ])	
		}

		// handle event objects
		flow.eventTemplates.each() { eventTemplate ->
			// iterate through events
			eventTemplate.getValue().events.each() { eventId ->
				// iterate through template fields
				flow.events[ eventId ].giveFields().each() { eventField ->
					flow.events[ eventId ].setFieldValue(eventField.name, params.get( 'event_' + eventId + '_' + eventField.escapedName() ) )
				}

				// validate event
				if (!flow.events[ eventId ].validate()) {
					errors = true
					this.appendErrors(flow.events[ eventId ], flash.errors, 'event_' + eventId + '_')
				}
			}
		}

		// handle event grouping
		handleEventGrouping(flow, flash, params)

		return !errors
	}

	/**
	 * re-usable code for handling event grouping in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleEventGrouping(flow, flash, params) {
		// walk through eventGroups
		def g = 0
		flow.eventGroups.each() { eventGroup ->
			def e = 0

			// reset events
			eventGroup.events = new HashSet()

			// iterate through events
			flow.events.each() {
				if (params.get('event_' + e + '_group_' + g) == 'on') {
					eventGroup.addToEvents(it)
				}
				e++
			}
			g++
		}
	}

	/**
	 * re-usable code for handling subject grouping in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleSubjectGrouping(flow, flash, params) {
		// iterate through event groups
		def g = 0
		flow.eventGroups.each() { eventGroup ->
			// reset subjects
			eventGroup.subjects = new HashSet()

			// iterate through subjects
			flow.subjects.each() { subjectId, subject ->
				// is this combination set?
				if (params.get('subject_' + subjectId + '_group_' + g) != null) {
					eventGroup.addToSubjects(subject)
				}
			}

			g++
		}
	}

	/**
	 * return the object from a map of objects by searching for a name
	 * @param String name
	 * @param Map map of objects
	 * @return Object
	 */
	def getObjectByName(name, map) {
		def result = null
		map.each() {
			if (it.name == name) {
				result = it
			}
		}

		return result
	}

	/**
	 * transform domain class validation errors into a human readable
	 * linked hash map
	 * @param object validated domain class
	 * @returns object  linkedHashMap
	 */
	def getHumanReadableErrors(object) {
		def errors = [:]
		object.errors.getAllErrors().each() {
			def message = it.toString()

			//errors[it.getArguments()[0]] = it.getDefaultMessage()
			errors[it.getArguments()[0]] = message.substring(0, message.indexOf(';'))
		}

		return errors
	}

	/**
	 * append errors of a particular object to a map
	 * @param object
	 * @param map linkedHashMap
	 * @void
	 */
	def appendErrors(object, map) {
		this.appendErrorMap(this.getHumanReadableErrors(object), map)
	}

	def appendErrors(object, map, prepend) {
		this.appendErrorMap(this.getHumanReadableErrors(object), map, prepend)
	}

	/**
	 * append errors of one map to another map
	 * @param map linkedHashMap
	 * @param map linkedHashMap
	 * @void
	 */
	def appendErrorMap(map, mapToExtend) {
		map.each() {key, value ->
			mapToExtend[key] = ['key': key, 'value': value, 'dynamic': false]
		}
	}

	def appendErrorMap(map, mapToExtend, prepend) {
		map.each() {key, value ->
			mapToExtend[prepend + key] = ['key': key, 'value': value, 'dynamic': true]
		}
	}

	/**
	 * Parses a RelTime string and returns a nice human readable string
	 *
	 * @returns Human Readable string or a HTTP response code 400 on error
	 */
	def ajaxParseRelTime = {
		if (params.reltime == null) {
			response.status = 400;
			render('reltime parameter is expected');
		}

		try {
			def reltime = RelTime.parseRelTime(params.reltime);
			render reltime.toPrettyString();
		} catch (IllegalArgumentException e) {
			response.status = 400;
			render(e.getMessage());
		}
	}
}