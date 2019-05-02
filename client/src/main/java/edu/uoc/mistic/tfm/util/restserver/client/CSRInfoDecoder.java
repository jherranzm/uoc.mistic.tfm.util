package edu.uoc.mistic.tfm.util.restserver.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.Security;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

public class CSRInfoDecoder {

//private static Logger LOG = Logger.getLogger(CSRInfoDecoder.class.getName());

private static final String COUNTRY = "2.5.4.6";
private static final String STATE = "2.5.4.8";
private static final String LOCALE = "2.5.4.7";
private static final String ORGANIZATION = "2.5.4.10";
private static final String ORGANIZATION_UNIT = "2.5.4.11";
private static final String COMMON_NAME = "2.5.4.3";
private static final String EMAIL = "2.5.4.9";

private static final String csrPEM = "-----BEGIN CERTIFICATE REQUEST-----\n"
        + "MIICxDCCAawCAQAwfzELMAkGA1UEBhMCVVMxETAPBgNVBAgMCElsbGlub2lzMRAw\n"
        + "DgYDVQQHDAdDaGljYWdvMQ4wDAYDVQQKDAVDb2RhbDELMAkGA1UECwwCTkExDjAM\n"
        + "BgNVBAMMBUNvZGFsMR4wHAYJKoZIhvcNAQkBFg9rYmF4aUBjb2RhbC5jb20wggEi\n"
        + "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDSrEF27VvbGi5x7LnPk4hRigAW\n"
        + "1feGeKOmRpHd4j/kUcJZLh59NHJHg5FMF7u9YdZgnMdULawFVezJMLSJYJcCAdRR\n"
        + "hSN+skrQlB6f5wgdkbl6ZfNaMZn5NO1Ve76JppP4gl0rXHs2UkRJeb8lguOpJv9c\n"
        + "tw+Sn6B13j8jF/m/OhIYI8fWhpBYvDXukgADTloCjOIsAvRonkIpWS4d014deKEe\n"
        + "5rhYX67m3H7GtZ/KVtBKhg44ntvuT2fR/wB1FlDws+0gp4edlkDlDml1HXsf4FeC\n"
        + "ogijo6+C9ewC2anpqp9o0CSXM6BT2I0h41PcQPZ4EtAc4ctKSlzTwaH0H9MbAgMB\n"
        + "AAGgADANBgkqhkiG9w0BAQsFAAOCAQEAqfQbrxc6AtjymI3TjN2upSFJS57FqPSe\n"
        + "h1YqvtC8pThm7MeufQmK9Zd+Lk2qnW1RyBxpvWe647bv5HiQaOkGZH+oYNxs1XvM\n"
        + "y5huq+uFPT5StbxsAC9YPtvD28bTH7iXR1b/02AK2rEYT8a9/tCBCcTfaxMh5+fr\n"
        + "maJtj+YPHisjxKW55cqGbotI19cuwRogJBf+ZVE/4hJ5w/xzvfdKjNxTcNr1EyBE\n"
        + "8ueJil2Utd1EnVrWbmHQqnlAznLzC5CKCr1WfmnrDw0GjGg1U6YpjKBTc4MDBQ0T\n"
        + "56ZL2yaton18kgeoWQVgcbK4MXp1kySvdWq0Bc3pmeWSM9lr/ZNwNQ==\n"
        + "-----END CERTIFICATE REQUEST-----\n";


private static final String csrPEM2 = "-----BEGIN CERTIFICATE REQUEST-----\n"
		+ "MIIE6zCCAtUCAQAwgYUxCzAJBgNVBAYTAkVTMRIwEAYDVQQIDAlDYXRhbHVueWExE\n"
		+ "jAQBgNVBAcMCUJhcmNlbG9uYTEoMCYGA1UECgwfVW5pdmVyc2l0YXQgT2JlcnRhIG\n"
		+ "RlIENhdGFsdW55YTEPMA0GA1UECwwGTUlTVElDMRMwEQYDVQQDDApVc3VhcmlvQXB\n"
		+ "wMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAypE9kO7jpad7eTBw59xN\n"
		+ "mhm3PgjgQJF23eLxKB8c6eIi810S8Xx7soFk+7gE/JSIu3ZdESMV0g9Qa2Btnr/Wv\n"
		+ "R1Cm/moabNPecGZoZHwBhQNBePDN9DKdYDGtRkGSt1ZyEQeAHWPRKnRa2PgP1TIoW\n"
		+ "QCVGoA2zzQugwTPi1uXIysfYtUSbn5B9bKEMXlIRzzsn8rn59GadS+8BSMV+0v3dX\n"
		+ "CkrCSVvl95lsueWyLwWMyJMSt5FRvZu3KaABQmd28RNZWSd5EwfbJ3yDb69/xK46a\n"
		+ "y6iJGcXo9g+WYLf+q4lkCDU6arJmz1TFXNZDho6cOwaHLNpoHxPrRU7K0TlRUEZWg\n"
		+ "aXYcbP+ENDW5cY79VU+pntHyRq4yk+4ToZaDUQ+bJ0O1aBWU+ocAd7emLiVjc3s0S\n"
		+ "07d0aHEEYUUOoQzFlbg48SwUOKWU45jv/3mB48rOY6/Q9Cg8zlbX/WDz0UXuXnI5I\n"
		+ "z2uErnYmaxv92oFV6+l4j4YdvcmsWnvGg2JbJqADJmIuYbax2/1yeUtETyPg9onqq\n"
		+ "0r55rsd2yPsqFyoExPzvWemRGTvM7x70SGaz0sLuiqeK5dq+fRryr1tRLbL3xsBpp\n"
		+ "uz/UX9UBou8zCvnSSVx6yS/nkE4rlsxzpxytd5PEPp4FevpTaPDkavlyFe20+V7pi\n"
		+ "VQkCfiRNmV3HUCAwEAAaAiMCAGCSqGSIb3DQEJDjETMBEwDwYDVR0TAQH/BAUwAwE\n"
		+ "B/zALBgkqhkiG9w0BAQsDggIBAHwcPEr3wne03n64XHmMIfdhk5mYQqAt1xnQVpKO\n"
		+ "NQtAhyXyDok2vuLhK+iltVm6+QYjpL74oZyX2tEOyYCdjQo+hUi7AF3bzghaMe8aF\n"
		+ "54k/KR5Y2Bcp1mEuAkzO7bBq2C5HAWB5TJBwSYWDnQRIjYtY4AfDTqDHU1DoyGDft\n"
		+ "xZrlpI74qWWs/ZXZMKpUN14G7rh/KgeI1+M23qAhXcFKVXFhiO2Y7JU6i/X5Beuc/\n"
		+ "CEn3TAM3x+520q6xEHJ+KskpjISG03XGKQA26GA73AHu3L1TFUbcrGm2aEmhtWBoG\n"
		+ "Moob6etZcYo12XC3qMgwSTMMU0bcOGSUdSLHgEFHUi0AquIyyHhbtn2WEWn1ZarcZ\n"
		+ "mJgpr8sOP4dFaGn09S/G3ecL7/uxFR4pX2SYqJgQWCr6lqECvk0zgyUFYkQC7SJ53\n"
		+ "0bQJKsIk76CqlKF+UzZSoDhpe5DvbZ5TqNtfelvnONTx4b7siWT/qgdWEnajpXBjx\n"
		+ "aipQeTCJ4BIRHlwL6qnFiBDx/VKATdsBHtHgr8lOt13XT/33ahfdVtE7heWgyOeku\n"
		+ "2Q/Y3gXieQSoQl6pqGQn9Ak7dX//od7BgMZD9BUtkMro0YqgY4iDWsYf6qYuM8SlO\n"
		+ "TrQoe0mFBb2WBDDHgyQcVQX5S1AshYfA8cHdb5mq7DjCf+b+Ncwc+MOe/xc\n"
		+ "-----END CERTIFICATE REQUEST-----\n";

public static void main(String[] args) {
    InputStream stream = new ByteArrayInputStream(csrPEM2.getBytes(StandardCharsets.UTF_8));

    CSRInfoDecoder m = new CSRInfoDecoder();
    m.readCertificateSigningRequest(stream);
}

public String readCertificateSigningRequest(InputStream csrStream) {

    PKCS10CertificationRequest csr = convertPemToPKCS10CertificationRequest(csrStream);
    String compname = null;

    if (csr == null) {
        System.out.println("FAIL! conversion of Pem To PKCS10 Certification Request");
    } else {
       X500Name x500Name = csr.getSubject();

       System.out.println("x500Name is: " + x500Name + "\n");

       //RDN cn = x500Name.getRDNs(BCStyle.EmailAddress)[0];
       //System.out.println(cn.getFirst().getValue().toString());
       //System.out.println(x500Name.getRDNs(BCStyle.EmailAddress)[0]);
       System.out.println("COUNTRY: " + getX500Field(COUNTRY, x500Name));
       System.out.println("STATE: " + getX500Field(STATE, x500Name));
       System.out.println("LOCALE: " + getX500Field(LOCALE, x500Name));
       System.out.println("ORGANIZATION: " + getX500Field(ORGANIZATION, x500Name));
       System.out.println("ORGANIZATION_UNIT: " + getX500Field(ORGANIZATION_UNIT, x500Name));
       System.out.println("COMMON_NAME: " + getX500Field(COMMON_NAME, x500Name));
       System.out.println("EMAIL: " + getX500Field(EMAIL, x500Name));
    }
    return compname;
}


private String getX500Field(String asn1ObjectIdentifier, X500Name x500Name) {
    RDN[] rdnArray = x500Name.getRDNs(new ASN1ObjectIdentifier(asn1ObjectIdentifier));

    String retVal = null;
    for (RDN item : rdnArray) {
        retVal = item.getFirst().getValue().toString();
    }
    return retVal;
}

private PKCS10CertificationRequest convertPemToPKCS10CertificationRequest(InputStream pem) {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    PKCS10CertificationRequest csr = null;
    ByteArrayInputStream pemStream = null;

    pemStream = (ByteArrayInputStream) pem;

    Reader pemReader = new BufferedReader(new InputStreamReader(pemStream));
    PEMParser pemParser = null;
    try {
        pemParser = new PEMParser(pemReader);
        Object parsedObj = pemParser.readObject();
        System.out.println("PemParser returned: " + parsedObj);
        if (parsedObj instanceof PKCS10CertificationRequest) {
            csr = (PKCS10CertificationRequest) parsedObj;
        }
        pemParser.close();
    } catch (IOException ex) {
    	System.out.println("IOException, convertPemToPublicKey: " +ex);
    } finally {
        if (pemParser != null) {
            //IOUtils.closeQuietly(pemParser);
        	//pemParser.close();
        }
    }
    return csr;
}
}