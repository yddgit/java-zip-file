package com.my.project;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

public class XmlUtil {

    public static void getXmlElement() throws Exception {

        ByteArrayOutputStream outputStream = null;
        try {
            // 读取XML文件
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse( XmlUtil.class.getClassLoader().getResourceAsStream( "test.xml" ) );

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            ProcessingInstruction processingInstruction = (ProcessingInstruction) (doc.getDocumentElement()
                    .getPreviousSibling());
            processingInstruction.setData( "type=\"text/xsl\" href=\"test.xsl\"" );

            // 查询Element并修改属性
            Element templateId = (Element) (xpath.compile( "/PEOPLE/PERSON[@age='30']" ).evaluate( doc,
                    XPathConstants.NODE ));
            templateId.setAttribute("age", "31");

            // 生成新的XML文档
            DOMSource source = new DOMSource( doc );
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
            transformer.setOutputProperty( OutputKeys.VERSION, "1.0" );
            transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
            transformer.setOutputProperty( OutputKeys.STANDALONE, "no" );

            outputStream = new ByteArrayOutputStream();
            transformer.transform( source, new StreamResult( outputStream ) );
            String xmlData = outputStream.toString();
            System.out.println(xmlData);
        } catch ( Exception e ) {
            throw e;
        } finally {
            if ( outputStream != null ) {
                outputStream.close();
            }
        }

    }
}
