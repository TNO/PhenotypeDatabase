package dbnp.authentication

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LogoutController {

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		if( params[ SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter ] ) {
			redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl + "?" + SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter + '=' + params[ SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter ] // '/j_spring_security_logout'
		} else {
			redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
		}
		// TODO  put any pre-logout code here
	}

	def remote = {
		// Log out the remote user
		AuthenticationService.logOffRemotely( params.consumer, params.token )
		
		// Try to rest the redirect url
		if( params[ SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter ] ) {
			redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl + "?" + SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter + '=' + params[ SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter ] // '/j_spring_security_logout'
		} else {
			redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
		}
	}
}