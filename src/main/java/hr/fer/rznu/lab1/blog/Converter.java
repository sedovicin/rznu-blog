package hr.fer.rznu.lab1.blog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Converter {

	public static byte[] convertObjectToJson(final Object object) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		return om.writeValueAsBytes(object);
	}
}
