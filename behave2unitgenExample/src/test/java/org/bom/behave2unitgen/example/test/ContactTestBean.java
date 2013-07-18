package org.bom.behave2unitgen.example.test;

/**
 * This bean belongs to the JSON-Test
 * @author Carsten Severin
 * @see ContactJSONStoryTest
 *
 */
public class ContactTestBean {

	String firstname;
	
	String lastname;
	
	String owner;

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	
	
}
