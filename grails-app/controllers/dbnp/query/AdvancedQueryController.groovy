package dbnp.query

import dbnp.studycapturing.*

// TODO: Make use of the searchable-plugin possibilities instead of querying the database directly

/**
 * Basic web interface for searching within studies
 *
 * @author Robert Horlings (robert@isdat.nl)
 */
class AdvancedQueryController {
	def entitiesToSearchFor = [ 'Study': 'Studies', 'Sample': 'Samples']
    def index = {
		[entitiesToSearchFor: entitiesToSearchFor, searchableFields: getSearchableFields()]
    }

	/**
	 * Searches for studies or samples based on the user parameters.
	 * 
	 * @param	entity		The entity to search for ( 'Study' or 'Sample' )
	 * @param	criteria	HashMap with the values being hashmaps with field, operator and value.
	 * 						[ 0: [ field: 'Study.name', operator: 'equals', value: 'term' ], 1: [..], .. ]
	 */
	def search = {
		if( !params.criteria ) {
			flash.error = "No criteria given to search for. Please try again.";
			redirect( action: 'index' )
		}

		if( !params.entity || !entitiesToSearchFor*.key.contains( params.entity ) ) {
			flash.error = "No or incorrect entity given to search for. Please try again.";
			redirect( action: 'index', params: [ criteria: parseCriteria( params.criteria ) ] )
		}

		// Create a search object and let it do the searching
		Search search;
		switch( params.entity ) {
			case "Study":	search = new StudySearch();		break;
			case "Sample":	search = new SampleSearch();	break;
			
			// This exception will only be thrown if the entitiesToSearchFor contains more entities than 
			// mentioned in this switch structure.
			default:		throw new Exception( "Can't search for entities of type " + params.entity );	
		}
		
		search.execute( parseCriteria( params.criteria ) );
		
		render( view: search.getView(), model: [search: search] );
	}
	
	/**
	 * Returns a map of entities with the names of the fields the user can search on
	 * @return
	 */
	protected def getSearchableFields() {
		def fields = [:];
		
		getEntities().each {
			def entity = getEntity( 'dbnp.studycapturing.' + it );
			
			if( entity ) {
				def domainFields = entity.giveDomainFields();
				def templateFields = TemplateField.findAllByEntity( entity )
				
				fields[ it ] = ( domainFields + templateFields ).collect { it.name }.unique().sort { a, b -> a[0].toUpperCase() + a[1..-1] <=> b[0].toUpperCase() + b[1..-1] };
			}
		}
		
		return fields;
	}
	
	/**
	 * Parses the criteria from the query form given by the user
	 * @param	c	Data from the input form and had a form like
	 * 
	 *	[
	 *		0: [entityfield:a.b, operator: b, value: c],
	 *		0.entityfield: a.b,
	 *		0.operator: b,
	 *		0.field: c
	 *		1: [entityfield:f.q, operator: e, value: d],
	 *		1.entityfield: f.q,
	 *		1.operator: e,
	 *		1.field: d
	 *	]
	 *
	 * @return	List with [entity: ..., field: ..., entityfield: ..., operator: ..., value: ...] tuples.
	 */
	protected List parseCriteria( def c ) {
		ArrayList list = [];
		
		// Loop through all keys of c and remove the non-numeric ones
		c.each {
			if( it.key ==~ /[0-9]+/ ) {
				def criterium = it.value;
				
				def field = criterium.entityfield?.split( /\./ );
				
				if( field.size() > 1 ) {
					criterium.entity = field[0].toString();
					criterium.field = field[1].toString();
				} else {
					criterium.entity = null;
					criterium.field = field;
				}
				
				list << criterium;
			}
		}
		
		return list;
	}
	
	/**
	 * Returns all entities for which criteria can be entered
	 * @return
	 */
	protected def getEntities() {
		return [ 'Study', 'Subject', 'Sample', 'Event', 'SamplingEvent', 'Assay' ]
	}
	
	/**
	* Creates an object of the given entity.
	*
	* @return False if the entity is not a subclass of TemplateEntity
	*/
   protected def getEntity( entityName ) {
	   // Find the templates
	   def entity
	   try { 
		   entity = Class.forName(entityName, true, this.getClass().getClassLoader())

		   // succes, is entity an instance of TemplateEntity?
		   if (entity.superclass =~ /TemplateEntity$/ || entity.superclass.superclass =~ /TemplateEntity$/) {
			   return entity;
		   } else {
			   return false;
		   }
	   } catch( ClassNotFoundException e ) {
	   		log.error "Class " + entityName + " not found: " + e.getMessage()
	   		return null;
	   }

   }

}
