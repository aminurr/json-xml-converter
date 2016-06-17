/*
   DESCRIPTION
    The class reads a JSON Streams and delegats call to <code>JsonJacksonSaxAdapter</code> to raise Sax events and generate XML file

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
import com.fasterxml.jackson.core.JsonParser; 
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.io.IOException;
public class JsonJacksonXmlReader
  implements XMLReader
{

  private ContentHandler contentHandler;
  private final String namespaceUri;
  private final boolean addTypeAttributes;
  private final String artificialRootName;


  /**
   * Creates JsonJacksonXmlReader
   */
  public JsonJacksonXmlReader()
  {
    this("");
  }

  /**
   * Creates JsonJacksonXmlReader
   * @param namespaceUri namespace uri of the resulting XML.
   */
  public JsonJacksonXmlReader(String namespaceUri)
  {
    this(namespaceUri, false);
  }

  /**
   * Creates JsonJacksonXmlReader
   * @param namespaceUri namespace uri of the resulting XML.
   * @param addTypeAttributes if true adds attributes with type info
   */
  public JsonJacksonXmlReader(String namespaceUri, boolean addTypeAttributes)
  {
    this(namespaceUri, addTypeAttributes, null);
  }

  /**
   * Creates JsonJacksonXmlReader
   * @param namespaceUri namespace uri of the resulting XML.
   * @param addTypeAttributes if true adds attributes with type info
   * @param artificialRootName if set, an artificial root is generated so JSON documents with more roots can be handeled.
   */
  public JsonJacksonXmlReader(String namespaceUri, boolean addTypeAttributes, String artificialRootName)
  {
    this.namespaceUri = namespaceUri;
    this.addTypeAttributes = addTypeAttributes;
    this.artificialRootName = artificialRootName;
  }


  public boolean getFeature(String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new UnsupportedOperationException();
  }

  public void setFeature(String name, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {

  }

  public Object getProperty(String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    throw new UnsupportedOperationException();
  }


  public void setProperty(String name, Object value)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    //ignore
  }

  public void setEntityResolver(EntityResolver resolver)
  {
    //ignore
  }

  public EntityResolver getEntityResolver()
  {
    throw new UnsupportedOperationException();
  }

  public void setDTDHandler(DTDHandler handler)
  {
    //ignore
  }

  public DTDHandler getDTDHandler()
  {
    throw new UnsupportedOperationException();
  }

  public void setContentHandler(ContentHandler handler)
  {
    this.contentHandler = handler;
  }

  public ContentHandler getContentHandler()
  {
    return contentHandler;
  }

  public void setErrorHandler(ErrorHandler handler)
  {
    //ignore

  }

  public ErrorHandler getErrorHandler()
  {
    throw new UnsupportedOperationException();
  }


  public void parse(InputSource input)
    throws IOException, SAXException
  {
    JsonFactory jsf = new JsonFactory();
    jsf.configure(JsonParser.Feature. ALLOW_UNQUOTED_FIELD_NAMES, true);
    jsf.configure(JsonParser.Feature.ALLOW_COMMENTS,true);
    JsonParser jsonParser = jsf.createJsonParser(input.getCharacterStream());
    new JsonJacksonSaxAdapter(jsonParser, contentHandler, namespaceUri, addTypeAttributes, artificialRootName).parse();
  }

  public void parse(String systemId)
    throws IOException, SAXException
  {
    throw new UnsupportedOperationException();
  }

  public String getNamespaceUri()
  {
    return namespaceUri;
  }
}