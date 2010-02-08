package dbnp.studycapturing

/**
 * This is the superclass for template fields. Normally, this class will not be instantiated.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
abstract class TemplateField implements Serializable {
	String name
	TemplateFieldType type
	String unit

    static hasMany = [listEntries : String] // to store the entries to choose from when the type is 'item from predefined list'

	static constraints = {
		name(unique: true)
		unit(nullable: true, blank: true)
	}

	String toString() {
		return name
	}
}
