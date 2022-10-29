package com.esotericsoftware.kryo;

import static com.esotericsoftware.kryo.util.Util.className;
import static com.esotericsoftware.kryo.util.Util.isAscii;
import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;

import org.objenesis.instantiator.ObjectInstantiator;

@SuppressWarnings({ "rawtypes" })
public class Registration {
	private final Class type;
	private final boolean typeNameAscii;
	private final int id;
	private Serializer serializer;
	private ObjectInstantiator instantiator;

	public Registration (Class type, Serializer serializer, int id) {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		if (serializer == null) throw new IllegalArgumentException("serializer cannot be null.");
		this.type = type;
		this.serializer = serializer;
		this.id = id;
		typeNameAscii = isAscii(type.getName());
	}

	public Class getType () {
		return type;
	}

	public boolean isTypeNameAscii () {
		return typeNameAscii;
	}

	/** Returns the registered class ID.
	 * @see Kryo#register(Class) */
	public int getId () {
		return id;
	}

	public Serializer getSerializer () {
		return serializer;
	}

	public void setSerializer (Serializer serializer) {
		if (serializer == null) throw new IllegalArgumentException("serializer cannot be null.");
		this.serializer = serializer;
		if (TRACE) trace("kryo", "Update registered serializer: " + type.getName() + " (" + serializer.getClass().getName() + ")");
	}

	/** @return May be null if not yet set. */
	public ObjectInstantiator getInstantiator () {
		return instantiator;
	}

	/** Sets the instantiator that will create a new instance of the type in {@link Kryo#newInstance(Class)}. */
	public void setInstantiator (ObjectInstantiator instantiator) {
		if (instantiator == null) throw new IllegalArgumentException("instantiator cannot be null.");
		this.instantiator = instantiator;
	}

	public String toString () {
		return "[" + id + ", " + className(type) + "]";
	}
}
