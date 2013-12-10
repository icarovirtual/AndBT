package br.pucrs.tcii.datahandling;

import java.nio.charset.Charset;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 * Default serializer for AndBT. Uses JSON to serialize date. You can override
 * this class functions if you wish to use your own serializer.
 */
public abstract class Serializer {
	/**
	 * Convert data to JSON format and return it as an array of bytes.
	 * 
	 * @param data
	 *            object to convert
	 * @return data as an array of bytes
	 */
	public byte[] fromObject(Object data) {
		Gson parser = new Gson();
		String json = parser.toJson(data, data.getClass()).trim();
		// Converts JSON string to UTF-8 array of bytes
		return json.getBytes(Charset.defaultCharset());
	}

	/**
	 * Convert data to object of type.
	 * 
	 * @param data
	 *            object as an array of bytes
	 * @param type
	 *            type of return object
	 * @return object of type
	 */
	public <T> T toObject(byte[] data, Class<T> type) throws JsonSyntaxException, JsonParseException {
		// Converts byte array to UTF-8 JSON string
		String json = new String(data, Charset.defaultCharset()).trim();
		try {
			Gson parser = new Gson();
			return parser.fromJson(json, type);
		} catch (JsonSyntaxException e1) {
			Log.e(Serializer.class.getName(), json + " is a malformed JSON");
			throw e1;
		} catch (JsonParseException e2) {
			throw e2;
		}
	}
}
