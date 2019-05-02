package edu.uoc.mistic.tfm.util.restserver.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

public class CreateCSR {

	private static final String RSA = "RSA";
	private static final String BC = "BC";
	private static final String SHA256WITH_RSA = "SHA256withRSA";
	private static final String P12_PASSWORD = "Th2S5p2rStr4ngP1ss";
	private static final int MILLIS_PER_DAY = 24*60*60*1000;
	
	
	
	public static String getCertificateFromCSR(String csr) {
		
		try {
			
			
			System.out.println("csr : " + csr);
			
			JcaPKCS10CertificationRequest jcaPKCS10CertificationRequest;

			PEMParser pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Base64.getDecoder().decode(csr.getBytes()))));
			Object parsedObj = pemParser.readObject();
			pemParser.close();
			System.out.println("PemParser returned: " + parsedObj);
			if (!(parsedObj instanceof PKCS10CertificationRequest)){
				throw new Exception("No ha llegado un CSR correcto!");
			}
			jcaPKCS10CertificationRequest = new JcaPKCS10CertificationRequest((PKCS10CertificationRequest)parsedObj);
			System.out.println("PublicKey : " + jcaPKCS10CertificationRequest.getPublicKey());
			
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509", BC);
			
			File fileCAP12 = new File("/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/PKI/CAkeystore.p12");
            InputStream isCAP12 = new FileInputStream(fileCAP12);
			
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(isCAP12, P12_PASSWORD.toCharArray());
			PrivateKey key = (PrivateKey)keystore.getKey("ca", P12_PASSWORD.toCharArray());
			
			File fileCACrt = new File("/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/PKI/certs/CA.crt");
            InputStream isCACrt = new FileInputStream(fileCACrt);
            
            X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(isCACrt);
            
            int validity = 365; //No of days the certificate should be valid
            String serialNo = "0001"; // a unique number

            Date issuedDate = new Date();
            Date expiryDate = new Date(System.currentTimeMillis() + validity * MILLIS_PER_DAY); //MILLIS_PER_DAY=86400000l
            //JcaPKCS10CertificationRequest jcaRequest = new JcaPKCS10CertificationRequest(csr);
            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(caCert,
                    new BigInteger(serialNo), issuedDate, expiryDate, jcaPKCS10CertificationRequest.getSubject(), jcaPKCS10CertificationRequest.getPublicKey());
            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                    extUtils.createAuthorityKeyIdentifier(caCert))
                    .addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(jcaPKCS10CertificationRequest
                            .getPublicKey()))
                    .addExtension(Extension.basicConstraints, true, new BasicConstraints(0))
                    .addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage
                            .keyEncipherment))
                    .addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
            ContentSigner signer = new JcaContentSignerBuilder(SHA256WITH_RSA).setProvider(BC).build(key);
 
            //Add the CRL endpoint
//            DistributionPointName crlEp = new DistributionPointName(new GeneralNames(new GeneralName(GeneralName
//                    .uniformResourceIdentifier, CA_CRL_ENDPOINT_URL)));
//            DistributionPoint disPoint = new DistributionPoint(crlEp, null, null);
//            certificateBuilder.addExtension(Extension.cRLDistributionPoints, false,
//                    new CRLDistPoint(new DistributionPoint[]{disPoint}));
 
            //Add the OCSP endpoint
//            AccessDescription ocsp = new AccessDescription(AccessDescription.id_ad_ocsp,
//                    new GeneralName(GeneralName.uniformResourceIdentifier, CA_OCSP_ENDPOINT_URL)
//            );
//            ASN1EncodableVector authInfoAccessASN = new ASN1EncodableVector();
//            authInfoAccessASN.add(ocsp);
//            certificateBuilder.addExtension(Extension.authorityInfoAccess, false, new DERSequence(authInfoAccessASN));
            
            X509Certificate signedCert = new JcaX509CertificateConverter().setProvider(BC).getCertificate
                    (certificateBuilder.build(signer));
            
            System.out.println(signedCert.getSubjectDN().getName());
            System.out.println(signedCert.getIssuerDN().getName());
            
            System.out.println(signedCert);
            
            System.out.println("-----BEGIN CERTIFICATE-----\n" + Base64.getEncoder().encodeToString(signedCert.getEncoded()) + "\n-----END CERTIFICATE-----");
            
            return Base64.getEncoder().encodeToString(signedCert.getEncoded());
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		return null;
		
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Provider bc = new org.bouncycastle.jce.provider.BouncyCastleProvider(); 
		Security.insertProviderAt(bc, 1);
		
		
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance(RSA);
			gen.initialize(2048);
			KeyPair pair = gen.generateKeyPair();
			PrivateKey privateKey = pair.getPrivate();
			PublicKey publicKey = pair.getPublic();
			
			//import javax.security.auth.x500.X500Principal
			X500Principal subject = new X500Principal ("C=ES, ST=Catalunya, L=Barcelona, O=Universitat Oberta de Catalunya, OU=MISTIC, CN=10.0.2.2, EMAILADDRESS=jherranzm@gmail.com");
			
			
			
			//import org.bouncycastle.operator.ContentSigner
			ContentSigner signGen = new JcaContentSignerBuilder(SHA256WITH_RSA).build(privateKey);
			
			//import org.bouncycastle.pkcs.PKCS10CertificationRequest;
			//import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
			//import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

			PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, publicKey);
			PKCS10CertificationRequest csr = builder.build(signGen);
			
			StringWriter sw = new StringWriter();
			JcaPEMWriter pemString = new JcaPEMWriter(sw);
			pemString.writeObject(csr);
			pemString.close();
			
			System.out.println("sw : " + sw.toString());
			
			System.out.println("cert:" + getCertificateFromCSR(sw.toString()));

			PEMParser pemParser = new PEMParser(new StringReader(sw.toString()));
			Object parsedObj = pemParser.readObject();
			pemParser.close();
			System.out.println("PemParser returned: " + parsedObj);
			if (parsedObj instanceof PKCS10CertificationRequest){
				JcaPKCS10CertificationRequest jcaPKCS10CertificationRequest = new JcaPKCS10CertificationRequest((PKCS10CertificationRequest)parsedObj);
				System.out.println("PublicKey : " + jcaPKCS10CertificationRequest.getPublicKey());
			}
			
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509", BC);
			
			File fileCAP12 = new File("/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/PKI/CAkeystore.p12");
            InputStream isCAP12 = new FileInputStream(fileCAP12);
			
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(isCAP12, P12_PASSWORD.toCharArray());
			PrivateKey key = (PrivateKey)keystore.getKey("ca", P12_PASSWORD.toCharArray());
			
			File fileCACrt = new File("/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/PKI/certs/CA.crt");
            InputStream isCACrt = new FileInputStream(fileCACrt);
            
            X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(isCACrt);
            
            int validity = 365; //No of days the certificate should be valid
            String serialNo = "0001"; // a unique number

            Date issuedDate = new Date();
            Date expiryDate = new Date(System.currentTimeMillis() + validity * MILLIS_PER_DAY); //MILLIS_PER_DAY=86400000l
            JcaPKCS10CertificationRequest jcaRequest = new JcaPKCS10CertificationRequest(csr);
            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(caCert,
                    new BigInteger(serialNo), issuedDate, expiryDate, jcaRequest.getSubject(), jcaRequest.getPublicKey());
            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                    extUtils.createAuthorityKeyIdentifier(caCert))
                    .addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(jcaRequest
                            .getPublicKey()))
                    .addExtension(Extension.basicConstraints, true, new BasicConstraints(0))
                    .addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage
                            .keyEncipherment))
                    .addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
            ContentSigner signer = new JcaContentSignerBuilder(SHA256WITH_RSA).setProvider(BC).build(key);
 
            //Add the CRL endpoint
//            DistributionPointName crlEp = new DistributionPointName(new GeneralNames(new GeneralName(GeneralName
//                    .uniformResourceIdentifier, CA_CRL_ENDPOINT_URL)));
//            DistributionPoint disPoint = new DistributionPoint(crlEp, null, null);
//            certificateBuilder.addExtension(Extension.cRLDistributionPoints, false,
//                    new CRLDistPoint(new DistributionPoint[]{disPoint}));
 
            //Add the OCSP endpoint
//            AccessDescription ocsp = new AccessDescription(AccessDescription.id_ad_ocsp,
//                    new GeneralName(GeneralName.uniformResourceIdentifier, CA_OCSP_ENDPOINT_URL)
//            );
//            ASN1EncodableVector authInfoAccessASN = new ASN1EncodableVector();
//            authInfoAccessASN.add(ocsp);
//            certificateBuilder.addExtension(Extension.authorityInfoAccess, false, new DERSequence(authInfoAccessASN));
            
            X509Certificate signedCert = new JcaX509CertificateConverter().setProvider(BC).getCertificate
                    (certificateBuilder.build(signer));
            
            System.out.println(signedCert.getSubjectDN().getName());
            System.out.println(signedCert.getIssuerDN().getName());
            
            System.out.println(signedCert);
            
            System.out.println("-----BEGIN CERTIFICATE-----\n" + Base64.getEncoder().encodeToString(signedCert.getEncoded()) + "\n-----BEGIN CERTIFICATE-----");
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
