package com.teledoc.server;

import org.postgresql.util.PGobject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.persistence.Converter;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;

/*
 * Note: Eclipse gives an error:
 * //
 * Class "com.teledoc.server.UuidConverter" is listed in the persistence.xml
 * file, but is not annotated
 * //
 * but it is indeed annotated, compiles and works fine, and if you remove it
 * from the xml it breaks.
 * Sorry.
 * You can change it into a warning via
 * Project->Properties->JPA->Errors/Warnings->
 * Class_is_listed_in_the_persistence_xml_file_but_is_not_annotated
 */
// https://stackoverflow.com/a/39885334/513038
@Converter(autoApply = true)
public class ListDoubleConverter implements javax.persistence.AttributeConverter<List<Double>, String>
{
   private static final long serialVersionUID = 1L;
   private static final Gson gson = new Gson(); 

   @Override
   public String convertToDatabaseColumn( List<Double> objectValue )
   {
	   if (objectValue == null) {
		   return null;
	   }
	   return gson.toJson(objectValue);
   }

   @Override
   public List<Double> convertToEntityAttribute( String dataValue )
   {
	   if (dataValue == null) {
		   return null;
	   }
	   return gson.fromJson(dataValue, new TypeToken<List<Double>>(){}.getType());
   }
}