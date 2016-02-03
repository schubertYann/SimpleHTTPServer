package http.prefab;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import http.ResponseBuilder;
import processing.core.PApplet;
import processing.data.JSONObject;

public class AutoUpdateContext extends ResponseBuilder {

	PApplet parent;

	Map<String, Object> updateObjects = new HashMap<>();

	public AutoUpdateContext(PApplet parent) {
		super();
		this.parent = parent;
	}

	@Override
	public String getResponse(String requestString) {
		// System.out.println(requestString);
		JSONObject allClazzes = parent.parseJSONObject(requestString);
		for (Iterator<String> clazzIter = allClazzes.keyIterator(); clazzIter.hasNext();) {
			String clazzName = clazzIter.next();
			// System.out.println("editing: "+clazzName);
			JSONObject jsonClazz = allClazzes.getJSONObject(clazzName);
			Object updateObject = updateObjects.get(clazzName);
			for (Iterator<String> fieldIter = jsonClazz.keyIterator(); fieldIter.hasNext();) {
				String fieldName = fieldIter.next();
				if (!fieldName.equals("function")) {
					// System.out.println("editing field: "+fieldName);
					try {
						Field field = updateObject.getClass().getField(fieldName);
						// System.out.println(field.getType().getName());
						if (field.getType() == Integer.TYPE) {
							int value = jsonClazz.getInt(fieldName);
							// System.out.println("setting value to: "+value);
							field.set(updateObject, value);
						} else if (field.getType() == Float.TYPE) { // float
							float value = jsonClazz.getFloat(fieldName);
							field.set(updateObject, value);
						} else if (field.getType() == Boolean.TYPE) {
							boolean value = jsonClazz.getBoolean(fieldName);
							field.setBoolean(updateObject, value);
						}
					} catch (NoSuchFieldException | SecurityException e) {
						System.err.println("No acces to the field: " + fieldName + " of class " + clazzName);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						System.err.println("Cannot set field: " + fieldName + " of class " + clazzName);
					}
				} else { // function call
					try {
						Method fct = updateObject.getClass().getMethod(jsonClazz.getString(fieldName), new Class<?>[]{});
							fct.invoke(updateObject, new Object[]{});
					} catch (NoSuchMethodException | SecurityException e) {
						System.err.println("No acces to the method: " + fieldName + " of class " + clazzName);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						System.err.println("Cannot call: " + fieldName + " of class " + clazzName);
					}
				}
			}
		}
		return "{}";
	}

	public void add(Object obj, ClassGui cg) {
		// System.out.println(cg.getName());
		updateObjects.put(cg.getName(), obj);
	}

}