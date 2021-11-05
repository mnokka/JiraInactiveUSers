/*
 November 2021 mika.nokka1@gmail.com
 
 POC:
 
 Utility tool to be executed in script console. Finds (defined datelimit) inactive users and lists them
 
 Tnx to Adaptavist library examples for the original codes

*/

import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.bc.user.UserService
import com.atlassian.jira.bc.user.search.UserSearchParams
import com.atlassian.jira.bc.user.search.UserSearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.crowd.embedded.api.CrowdService
import groovy.xml.MarkupBuilder

import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId

import org.apache.log4j.Logger
import org.apache.log4j.Level


def crowdService = ComponentAccessor.getComponent(CrowdService)

// Number of days the user was not logged in Date
def numOfDays = 100
def dateLimit = LocalDateTime.now().minusDays(numOfDays)

// set logging to Jira log
def log = Logger.getLogger("GetInactiveUsers") // change for customer system
log.setLevel(Level.DEBUG)  // DEBUG INFO
 
log.debug("---------- GetInactiveUsers started -----------")


// Search all active users
UserSearchParams.Builder paramBuilder = UserSearchParams.builder()
	.allowEmptyQuery(true)
	.includeActive(true)
	.includeInactive(false)

def jiraServiceContext = new JiraServiceContextImpl(ComponentAccessor.jiraAuthenticationContext.loggedInUser)
def allActiveUsers = ComponentAccessor.getComponent(UserSearchService).findUsers(jiraServiceContext, '', paramBuilder.build())

// Users which last activity is before than limit date
def InACtiveUsers = allActiveUsers.findAll { user ->
	def userWithAtributes = crowdService.getUserWithAttributes(user.username)
	def lastLoginMillis = userWithAtributes.getValue('login.lastLoginMillis')

	if (lastLoginMillis?.number) {
		def lastLogin = Instant.ofEpochMilli(Long.parseLong(lastLoginMillis)).atZone(ZoneId.systemDefault()).toLocalDateTime()
		if (lastLogin.isBefore(dateLimit)) {
			user
		}
	}
}

def i=0
allActiveUsers.each {
	log.debug("ACTIVEUSER $i: $it")
	i=i+1
}


if (!InACtiveUsers) {
	log.debug("Nothing matched")
	return 'No Idle users found'
}


InACtiveUsers.each {
	log.debug("USER: $it")
}
