
package com.esotericsoftware.jsonbeans;

@SuppressWarnings({ "rawtypes" })
public abstract class ReadOnlySerializer<T> implements JsonSerializer<T> {
	public void write (Json json, T object, Class knownType) {
	}

	abstract public T read (Json json, JsonValue jsonData, Class type);
}
