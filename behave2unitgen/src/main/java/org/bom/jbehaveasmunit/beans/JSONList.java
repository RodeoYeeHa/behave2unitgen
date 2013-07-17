package org.bom.jbehaveasmunit.beans;

/**
 * Wrapper-Class to host an object that is created by jackson/JSON-Parser
 * 
 * @author Carsten Severin
 *
 * @param <T>
 */
public class JSONList<T> {

	T[] list;
	
	public JSONList(T[] list){
		this.list = list;
	}

	public T[] getList() {
		return list;
	}

	public void setList(T[] list) {
		this.list = list;
	}
	
	
}
