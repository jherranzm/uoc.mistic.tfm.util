package edu.uoc.mistic.tfm.util.restserver.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class EnvelopedSignature {

	private static final String SECURITY_PROVIDER = "BC";
	private static final String SERVER_P12 = "/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/PKI/private/server.p12";
	private static final String SERVER_CERTIFICATE = "/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/PKI/certs/server.crt";
	public final static String PKCS12_PASSWORD = "Th2S5p2rStr4ngP1ss";
	public static final String PKCS_12 = "PKCS12";

	private static Log logger = LogFactory.getLog(EnvelopedSignature.class);

	public static void main(String[] args) {

		signXMLFile("/Users/jherranzm/Downloads/Invoice_990001.xml", "/Users/jherranzm/Downloads");
		
		//ListSupportedAlgorithms();
	}

	public static void signXMLFile(String fullPathFileToSign, String fullPathDirToExport) {
		
		String fileOut = fullPathDirToExport+".xsig";
		
		try {
			
			File fileToSign = new File(fullPathFileToSign);
			if (!fileToSign.exists()) {
				throw new FileNotFoundException("No existe el fichero " + fullPathFileToSign);
			}
			String fileName = fileToSign.getName();
			
			
			fileOut = fullPathDirToExport+fileName+".xsig";
			
			Security.addProvider(new BouncyCastleProvider());
			Security.addProvider(new org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI());
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509", SECURITY_PROVIDER);
			

			InputStream isServerCrt = new FileInputStream(
					new File(SERVER_CERTIFICATE));
			InputStream isServerKey = new FileInputStream(
					new File(SERVER_P12));

			X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);

			char[] keystorePassword = PKCS12_PASSWORD.toCharArray();
			char[] keyPassword = PKCS12_PASSWORD.toCharArray();

			KeyStore keystore = KeyStore.getInstance(PKCS_12, SECURITY_PROVIDER);
			keystore.load(isServerKey, keystorePassword);
			isServerKey.close();

			PrivateKey key = (PrivateKey) keystore.getKey("Server", keyPassword);
			if (key == null) {
				System.err.println(EnvelopedSignature.class.getCanonicalName() + " ERROR NO hay key!");
			}
			;

//			for (Provider p : Security.getProviders()) {
//				logger.info("Provider:[" + p.getName() + "] [" + p.getInfo() + "]");
//			}

			Set<String> messageDigest = Security.getAlgorithms("MessageDigest");
//			for (String s : messageDigest) {
//				logger.info("MessageDigest:messageDigest: [" + s + "]");
//			}

			Set<String> algorithms = Security.getAlgorithms("Algorithm");
//			for (String s : algorithms) {
//				logger.info("Algorithm:algorithm: [" + s + "]");
//			}

			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",
					new org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI());

			// Create a Reference to the enveloped document
			Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null),
					Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
					null, null);

			// Create the SignedInfo.
			SignedInfo si = fac.newSignedInfo(
					fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
					fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));

			// Create a KeyValue containing the DSA PublicKey that was generated
			KeyInfoFactory kif = fac.getKeyInfoFactory();
			KeyValue kv = kif.newKeyValue(certificate.getPublicKey());
			
			List x509Content = new ArrayList();
			x509Content.add(certificate.getSubjectX500Principal().getName());
			x509Content.add(certificate);
			X509Data xd = kif.newX509Data(x509Content);

			// Create a KeyInfo and add the KeyValue to it
			//KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));
			KeyInfo ki = kif.newKeyInfo(Arrays.asList(kv, xd));

			// Instantiate the document to be signed
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document doc = dbf.newDocumentBuilder()
					.parse(new FileInputStream(fileToSign));
//			logger.info("doc.getNodeName() :  " + doc.getNodeName());
//			logger.info("doc.getBaseURI() :  " + doc.getBaseURI());
//			logger.info("doc.getDocumentURI() :  " + doc.getDocumentURI());
//			logger.info("doc.getDocumentElement() :  " + doc.getDocumentElement());

			// Create a DOMSignContext and specify the DSA PrivateKey and
			// location of the resulting XMLSignature's parent element
			DOMSignContext dsc = new DOMSignContext(key, doc.getDocumentElement());

			// Create the XMLSignature (but don't sign it yet)
			XMLSignature signature = fac.newXMLSignature(si, ki);

			// Marshal, generate (and sign) the enveloped signature
			signature.sign(dsc);

			OutputStream os = System.out;
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.transform(new DOMSource(doc), new StreamResult(os));

			OutputStream osFile = new FileOutputStream(new File(fileOut));
			TransformerFactory tfFile = TransformerFactory.newInstance();
			Transformer transFile = tfFile.newTransformer();
			transFile.transform(new DOMSource(doc), new StreamResult(osFile));
			
		} catch (UnrecoverableKeyException | CertificateException | NoSuchProviderException | KeyStoreException
				| NoSuchAlgorithmException | InvalidAlgorithmParameterException | KeyException | IOException
				| SAXException | ParserConfigurationException | MarshalException | XMLSignatureException
				| TransformerFactoryConfigurationError | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void ListSupportedAlgorithms() {
	    String result = "";
		
	    // get all the providers
	    Provider[] providers = Security.getProviders();
	    
	    for(Provider provider : providers) {
	    	for(Object ks : provider.keySet()) {
	    		String k = ks.toString();
	    		System.out.println(k);
	    	}
	    }

//	    for (int p = 0; p < providers.length; p++) {
//	        // get all service types for a specific provider
//	        Set<Object> ks = providers[p].keySet();
//	        Set<String> servicetypes = new TreeSet<String>();
//	        for (Iterator<Object> it = ks.iterator(); it.hasNext();) {
//	            String k = it.next().toString();
//	            k = k.split(" ")[0];
//	            if (k.startsWith("Alg.Alias."))
//	                k = k.substring(10);
//
//	            servicetypes.add(k.substring(0, k.indexOf('.')));
//	        }
//
//	        // get all algorithms for a specific service type
//	        int s = 1;
//	        for (Iterator<String> its = servicetypes.iterator(); its.hasNext();) {
//	            String stype = its.next();
//	            Set<String> algorithms = new TreeSet<String>();
//	            for (Iterator<Object> it = ks.iterator(); it.hasNext();) {
//		            String k = it.next().toString();
//		            k = k.split(" ")[0];
//		            if (k.startsWith(stype + "."))
//		                algorithms.add(k.substring(stype.length() + 1));
//		            else if (k.startsWith("Alg.Alias." + stype +"."))
//		                algorithms.add(k.substring(stype.length() + 11));
//
//			        int a = 1;
//			        for (Iterator<String> ita = algorithms.iterator(); ita.hasNext();) {
//			            result += ("[P#" + (p + 1) + ":" + providers[p].getName() + "]" +
//			                       "[S#" + s + ":" + stype + "]" +
//			                       "[A#" + a + ":" + ita.next() + "]\n");
//			            a++;
//			        }
//			
//			        s++;
//	            }
//	}
	    }
}
