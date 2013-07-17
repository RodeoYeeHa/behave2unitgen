package org.bom.behave2unitgen.example.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Contact")
public class Contact{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	Long id;
	
	@Column(name = "FIRST_NAME")
	String firstname;
	
	@Column(name = "LAST_NAME")
	String lastname;

	@Column(name = "OWNER")
	String owner;
	
	public Contact(){
		
	}

	public Contact(String firstname, String lastname){
		this.firstname = firstname;
		this.lastname = lastname;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
