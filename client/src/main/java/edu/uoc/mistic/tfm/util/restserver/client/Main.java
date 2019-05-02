package edu.uoc.mistic.tfm.util.restserver.client;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {
  public static void main(String[] args) throws Exception {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //factory.setIgnoringComments(true);
    //factory.setCoalescing(true); // Convert CDATA to Text nodes
    factory.setNamespaceAware(true); // No namespaces: this is default
    //factory.setValidating(false); // Don't validate DTD: also default

    DocumentBuilder parser = factory.newDocumentBuilder();

    File f = new File("/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/SignedInvoices/invoice_990001_xml_20190329_2020707_xml.xsig");
    System.out.println("File exists? : " + f.exists());
    Document document = parser.parse(new FileInputStream(f));
    System.out.println("Doc:" + document.getNodeName());
    System.out.println("Doc:" + document.getDocumentURI());
    System.out.println("Doc:" + document.getTextContent());
    
    NodeList sections = document.getElementsByTagName("Signature");
    System.out.println("sections:" + sections.getLength());
    
    NodeList nl = document.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
    if (nl.getLength() == 0) {
        throw new Exception("No XML Digital Signature Found, document is discarded");
    }
    int numSections = sections.getLength();
    for (int i = 0; i < numSections; i++) {
      Element section = (Element) sections.item(i); // A <sect1>

      Node title = section.getFirstChild();
      while (title != null && title.getNodeType() != Node.ELEMENT_NODE)
        title = title.getNextSibling();

      if (title != null)
        System.out.println(title.getFirstChild().getNodeValue());
    }
  }
}