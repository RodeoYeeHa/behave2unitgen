package org.bom.behave2unitgen.example.service;

import java.util.Iterator;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.bom.behave2unitgen.example.beans.Contact;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("ContactDataService")
@Transactional
public class ContactDataService {

	@Autowired
	SessionFactory sessionFactory;

	public Contact getContact(Long id) {
		return (Contact) sessionFactory.getCurrentSession().get(Contact.class,
				id);
	}

	@SuppressWarnings("unchecked")
	public void renameContact(String lastnameOld, String lastnameNew){
		List<Contact> cList  = sessionFactory.getCurrentSession()
				.createQuery("from Contact c where c.lastname = :lastname").setParameter("lastname", lastnameOld).list();
				
		if (cList.isEmpty())throw new RuntimeException("No such Contact found: " + lastnameOld);
		
		for (Iterator<Contact>it = cList.iterator(); it.hasNext();){
			Contact c = it.next();
			c.setLastname(lastnameNew);
			sessionFactory.getCurrentSession().saveOrUpdate(c);
		}
	}
	
	public void insertContact(Contact c) throws TooManyContactsException {
		if (countContact() < 100) {
			sessionFactory.getCurrentSession().save(c);
		} else {
			throw new TooManyContactsException(
					"More than 100 contacts are not allowed to store");
		}
	}

	public int countContact() {
		return ((Number) sessionFactory.getCurrentSession()
				.createQuery("select count(c.id) from Contact c")
				.uniqueResult()).intValue();
	}

	@SuppressWarnings("unchecked")
	public List<Contact> findContacts(String searchQuery) {
		return (List<Contact>) sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Contact c where c.firstname like :firstnameQuery")
				.setParameter("firstnameQuery", searchQuery.replaceAll("\\*", "%"))
				.list();

	}

}
