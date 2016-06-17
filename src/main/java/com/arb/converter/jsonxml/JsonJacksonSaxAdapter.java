/*
   DESCRIPTION
    A class that parses json stream and generates XML output

    MODIFIED    (MM/DD/YY)
    mARB    21 Feb 2014 - Creation
 */

/**
 *  @version 1.0
 *  @author  mARB
 *  @since   release specific (what release of product did this appear in)
 */
package com.arb.converter.jsonxml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class JsonJacksonSaxAdapter
{
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
  private final JsonParser jsonParser;
  private final ContentHandler contentHandler;
  private final String namespaceUri;
  private final boolean addTypeAttributes;
  private final String artificialRootName;
  private boolean generateArrayName = false;
  private static final JsonFactory JSON_FACTORY = new JsonFactory();
  private static final String ARRAY_ITEM              = "item";

  /**
   * Creates JsonJacksonSaxAdapter that coverts JSON to SAX events.
   * @param json JSON String to parse
   * @param contentHandler target of SAX events
   */
  public JsonJacksonSaxAdapter(final String json, final ContentHandler contentHandler)
  {
    this(parseJson(json), contentHandler);
  }


  /**
   * Creates JsonJacksonSaxAdapter that coverts JSON to SAX events.
   * @param jsonParser parsed JSON
   * @param contentHandler target of SAX events
   */
  public JsonJacksonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler)
  {
    this(jsonParser, contentHandler, "");
  }

  /**
   * Creates JsonJacksonSaxAdapter that coverts JSON to SAX events.
   * @param jsonParser parsed JSON
   * @param contentHandler target of SAX events
   * @param namespaceUri namespace of the generated XML
   */
  public JsonJacksonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler,
                               final String namespaceUri)
  {
    this(jsonParser, contentHandler, namespaceUri, false);
  }

  /**
   * Creates JsonJacksonSaxAdapter that coverts JSON to SAX events.
   * @param jsonParser parsed JSON
   * @param contentHandler target of SAX events
   * @param namespaceUri namespace of the generated XML
   * @param addTypeAttributes adds type information as attributes
   */
  public JsonJacksonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler,
                               final String namespaceUri, final boolean addTypeAttributes)
  {
    this(jsonParser, contentHandler, namespaceUri, addTypeAttributes, null);
  }

  /**
   * Creates JsonJacksonSaxAdapter that coverts JSON to SAX events.
   * @param jsonParser parsed JSON
   * @param contentHandler target of SAX events
   * @param namespaceUri namespace of the generated XML
   * @param addTypeAttributes adds type information as attributes
   * @param artificialRootName if set, an artificial root is generated so JSON documents with more roots can be handeled.
   */
  public JsonJacksonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler,
                               final String namespaceUri, final boolean addTypeAttributes,
                               final String artificialRootName)
  {
    this.jsonParser = jsonParser;
    this.contentHandler = contentHandler;
    this.namespaceUri = namespaceUri;
    this.addTypeAttributes = addTypeAttributes;
    this.artificialRootName = artificialRootName;
    contentHandler.setDocumentLocator(new DocumentLocator());
  }

  public void setGenerateArrayName(boolean b)
  {
    generateArrayName = b;
  }


  private static JsonParser parseJson(final String json)
  {
    try
    {
      return JSON_FACTORY.createJsonParser(json);
    }
    catch (Exception e)
    {
      throw new ParserException("Parsing error", e);
    }
  }

  /**
   * Method parses JSON and emits SAX events.
   */
  public void parse()
    throws ParserException
  {
    try
    {
      jsonParser.nextToken();
      contentHandler.startDocument();
      if (shouldAddArtificialRoot())
      {
        startElement(artificialRootName);
      }
      int elementsWritten = parseObject();
      if (shouldAddArtificialRoot())
      {
        endElement(artificialRootName);
      }
      else if (elementsWritten > 1)
      {
        throw new ParserException("More than one root element. Can not generate legal XML. You can set artificialRootName to generate an artificial root.");
      }
      contentHandler.endDocument();
    }
    catch (Exception e)
    {
      throw new ParserException("Parsing error: " + e.getMessage(), e);
    }
  }

  private boolean shouldAddArtificialRoot()
  {
    return artificialRootName != null && artificialRootName.length() > 0;
  }

  /**
   * Parses generic object.
   *
   * @return number of elements written
   * @throws IOException
   * @throws JsonParseException,IOException
   * @throws Exception
   */
  private int parseObject()
    throws JsonParseException, IOException, SAXException
  {
    int elementsWritten = 0;
    boolean startedArray = START_ARRAY.equals(jsonParser.getCurrentToken());
    while (jsonParser.nextToken() != null && jsonParser.getCurrentToken() != END_OBJECT)
    {
      if (FIELD_NAME.equals(jsonParser.getCurrentToken()))
      {
        String elementName = jsonParser.getCurrentName();
        //jump to element value
        jsonParser.nextToken();
        parseElement(elementName);
        elementsWritten++;
      }
      else
      {
        //array of simple element
        if (startedArray && jsonParser.getCurrentToken().isScalarValue())
        {
          startElement(ARRAY_ITEM);
          parseValue();
          endElement(ARRAY_ITEM);
        }
        else if (startedArray && END_ARRAY.equals(jsonParser.getCurrentToken()))
        {
          //do nothing
        }
        else
        {
          throw new ParserException("Error when parsing. Expected field name got " + jsonParser.getCurrentToken());
        }
      }
    }
    return elementsWritten;
  }

  private void parseElement(final String elementName)
    throws SAXException, JsonParseException, IOException
  {

    JsonToken currentToken = jsonParser.getCurrentToken();
    if (generateArrayName || !START_ARRAY.equals(currentToken))
      startElement(elementName);
    if (START_OBJECT.equals(currentToken))
    {
      parseObject();
    }
    else if (START_ARRAY.equals(currentToken))
    {
      parseArray(elementName);
    }
    else if (currentToken.isScalarValue())
    {
      parseValue();
    }
    if (generateArrayName || !START_ARRAY.equals(currentToken))
      endElement(elementName);
  }

  private void parseArray(final String elementName)
    throws IOException, JsonParseException, SAXException
  {
    while (jsonParser.nextToken() != END_ARRAY && jsonParser.getCurrentToken() != null)
    {
      parseElement(elementName);
    }
  }

  private void parseValue()
    throws SAXException, IOException, JsonParseException
  {
    if (VALUE_NULL != jsonParser.getCurrentToken())
    {
      String text = jsonParser.getText();
      contentHandler.characters(text.toCharArray(), 0, text.length());
    }
  }


  private void startElement(final String elementName)
    throws SAXException
  {
    contentHandler.startElement(namespaceUri, elementName, elementName, getTypeAttributes());
  }


  protected Attributes getTypeAttributes()
  {
    if (addTypeAttributes)
    {
      String currentTokenType = getCurrentTokenType();
      if (currentTokenType != null)
      {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "type", "type", "string", currentTokenType);
        return attributes;
      }
      else
      {
        return EMPTY_ATTRIBUTES;
      }
    }
    else
    {
      return EMPTY_ATTRIBUTES;
    }
  }


  protected String getCurrentTokenType()
  {
    switch (jsonParser.getCurrentToken())
    {
      case VALUE_NUMBER_INT:
        return "int";
      case VALUE_NUMBER_FLOAT:
        return "float";
      case VALUE_FALSE:
        return "boolean";
      case VALUE_TRUE:
        return "boolean";
      case VALUE_STRING:
        return "string";
      case VALUE_NULL:
        return "null";
      case START_ARRAY:
        return "array";
      default:
        return null;
    }
  }


  private void endElement(final String elementName)
    throws SAXException
  {
    contentHandler.endElement(namespaceUri, elementName, elementName);
  }

  public static class ParserException
    extends RuntimeException
  {

    public ParserException(final String message, final Throwable cause)
    {
      super(message, cause);
    }

    public ParserException(final String message)
    {
      super(message);
    }

    public ParserException(final Throwable cause)
    {
      super(cause);
    }

  }

  private class DocumentLocator
    implements Locator
  {

    public String getPublicId()
    {
      Object sourceRef = jsonParser.getCurrentLocation().getSourceRef();
      if (sourceRef != null)
      {
        return sourceRef.toString();
      }
      else
      {
        return "";
      }
    }

    public String getSystemId()
    {
      return getPublicId();
    }

    public int getLineNumber()
    {
      return jsonParser.getCurrentLocation() != null? jsonParser.getCurrentLocation().getLineNr(): -1;
    }

    public int getColumnNumber()
    {
      return jsonParser.getCurrentLocation() != null? jsonParser.getCurrentLocation().getColumnNr(): -1;
    }
  }
}