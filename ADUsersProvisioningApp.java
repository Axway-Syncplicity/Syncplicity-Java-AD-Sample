import examples.ProvisioningADUsersExample;
import oauth.OAuth;
import util.APIContext;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @author Syncplicity
 * NOTE: this is not copyrighted material.         
 */
public class ADUsersProvisioningApp {

	static {
		/*
		 * The method below disables SSL certificate validation errors.
		 * WARNING: running the method exposes the application to man-in-the-middle attacks!
		 * Such code should never be reproduced in production code.
		 * We left it here for the purposes of HTTPS debugging.
		 * If you want to debug the app using Fiddler or another HTTP debugger,
		 * you might want to uncomment the line below in order to avoid creating
		 * custom trusted certificate store.
		 * Make sure to not use any sensitive data when running the sample in this mode:
		 * you are not protected from a malicious person intercepting application traffic
		 * without your knowledge!
		 */
		// unsafeDisableSslVerification();
	}
	
	public static void main(String[] args) {
		
		System.out.println( "ActiveDirectory user provisioning app starting...");
		System.out.println();
		
		/* 
		 * The sample app will show simplified examples of calls that you can make against the 
		 * api gateway using the available REST calls.
		 * 
		 * The example calls that this app will make include:
		 * 
		 * Authorization
		 * - OAuth authorization call (to allow this app to connect to the gateway and make API calls)
		 * 
		 * Provisioning from Active Directory
		 * - Creating new users associated with a company, taking them from an Active Directory (LDAP)
		 * - Creating new user groups from AD groups (a group as defined in Syncplicity as having access to the same shared folders)
		 * - Associating the newly created users with groups
		 */

		OAuth.authenticate();
		
		System.out.println();
		
		if( !APIContext.isAuthenticated() ) {
			System.err.println( "The OAuth authentication has failed, the app cannot continue." );
			System.exit(1);
		}
		else {
			System.out.println( "Authentication was successful." );			
		}

        try {
			ProvisioningADUsersExample.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();
		System.out.println("Sample ActiveDirectory user provisioning app has completed execution.");
	}

	@SuppressWarnings("unused") // Keep the code for the ability to disable SSL validation for running through Fiddler
	private static void unsafeDisableSslVerification() {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] certs, String authType) { }
				public void checkServerTrusted(X509Certificate[] certs, String authType) { }

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			}};
			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = (hostname, session) -> true;
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
	}
}
