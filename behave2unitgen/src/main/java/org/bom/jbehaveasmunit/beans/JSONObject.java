package org.bom.jbehaveasmunit.beans;

/**
 * Wrapper-Class to host an object that is created by jackson/JSON-Parser
 * 
 * @author Carsten Severin
 *
 * @param <T>
 */
public class JSONObject<T> {

	T object;

	public JSONObject(T object){
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}
		
}
