package com.teledoc.server;

import org.postgresql.util.PGobject;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.persistence.Converter;

import java.io.StringReader;
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
public class UuidConverter implements javax.persistence.AttributeConverter<UUID, String>
{
   private static final long serialVersionUID = 1L;

   @Override
   public String convertToDatabaseColumn( UUID objectValue )
   {
	   if (objectValue == null) {
		   return null;
	   }
	   return objectValue.toString();
   }

   @Override
   public UUID convertToEntityAttribute( String dataValue )
   {
	   if (dataValue == null) {
		   return null;
	   }
	   return UUID.fromString(dataValue);
   }
}