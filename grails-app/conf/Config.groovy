/**
 * Application Configuration
 *
 * @author  Jeroen Wesbeek
 * @since	20100520
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }

}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
	       'org.codehaus.groovy.grails.web.pages', //  GSP
	       'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
	       'org.codehaus.groovy.grails.web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	       'org.springframework',
	       'org.hibernate'

    warn   'org.mortbay.log'
}

graphviz {
    // graphviz installation path is dependent on OS
    // (requirement for class diagram plugin)
        switch (System.properties["os.name"]) {
            case "Mac OS X":
                // define mac path to Graphviz dot executable
                // (install using macports: sudo port install graphviz)
                dot.executable = "/opt/local/bin/dot"
                break
            default:
                // assume the linux default path
                dot.executable = "/usr/bin/dot"
        }
}

// cryptography settings
// @see WizardTaglib.groovy (encrypt)
// @see TemplateEditorController.groovy (decrypt)
crypto {
	shared.secret = "U73reG*mE^\$t@7s!e%"
}

// GSCF specific configuration
gscf {
	domain = [
		// importable entities
		// use: grailsApplication.config.gscf.domain.entities
		entities: [
			// dbnp.data
			'dbnp.data.FeatureBase',
			'dbnp.data.FeatureType',
			'dbnp.data.Ontology',
			'dbnp.data.Term',

			// dbnp.studycapturing
			'dbnp.studycapturing.Assay',
			'dbnp.studycapturing.AssayModule',
			'dbnp.studycapturing.AssayType',
			'dbnp.studycapturing.Compound',
			'dbnp.studycapturing.Event',
			'dbnp.studycapturing.EventGroup',
			'dbnp.studycapturing.Person',
			'dbnp.studycapturing.PersonAffilitation',
			'dbnp.studycapturing.PersonRole',
			'dbnp.studycapturing.Publication',
			'dbnp.studycapturing.Sample',
			'dbnp.studycapturing.SamplingEvent',
			'dbnp.studycapturing.Study',
			'dbnp.studycapturing.StudyPerson',
			'dbnp.studycapturing.Subject',
			'dbnp.studycapturing.Template',
			'dbnp.studycapturing.TemplateEntity',
			'dbnp.studycapturing.TemplateField',
			'dbnp.studycapturing.TemplateFieldListItem',
			'dbnp.studycapturing.TemplateFieldType'
		],

		// importable entities
		// use: grailsApplication.config.gscf.domain.importableEntities
		// @see ImporterController
		importableEntities: [
			none	: [name: 'None', entity:'', type:-1],
			event	: [name: 'Event', entity:'dbnp.studycapturing.Event', type:2],			
			sample	: [name: 'Sample', entity: 'dbnp.studycapturing.Sample', type:4],
			study	: [name: 'Study', entity: 'dbnp.studycapturing.Study', type:0],
			subject	: [name: 'Subject', entity: 'dbnp.studycapturing.Subject', type:1]

		]
	]
}