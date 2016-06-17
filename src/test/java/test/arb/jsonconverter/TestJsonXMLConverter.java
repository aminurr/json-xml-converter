package test.arb.converter;

import com.arb.converter.jsonxml.JsonJacksonXmlReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import java.util.Scanner;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.junit.*;
import static org.junit.Assert.assertTrue;
public class TestJsonXMLConverter
{
  @Before
    public void ignoreWhitespace() {
        XMLUnit.setIgnoreWhitespace(true);
  }
  @Test
  public void testParse()
    throws Exception
  {
    String jsonStream =
      new Scanner(TestJsonXMLConverter.class.getResourceAsStream("/Emp.json")).useDelimiter("\\A").next();
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    JsonJacksonXmlReader jxr = new JsonJacksonXmlReader(null, false, "rootNode");
    InputSource source = new InputSource(new StringReader(jsonStream));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Result result = new StreamResult(os);
    transformer.transform(new SAXSource(jxr, source), result);
    ByteArrayInputStream resultedStream = new ByteArrayInputStream(os.toByteArray());
    String convertedXML = new Scanner(resultedStream).useDelimiter("\\A").next();
	String expectedXML = new Scanner(TestJsonXMLConverter.class.getResourceAsStream("/Emp.xml")).useDelimiter("\\A").next();
	Diff diff = XMLUnit.compareXML(expectedXML, convertedXML);
    assertTrue(diff.toString(), diff.similar());
  }


}
