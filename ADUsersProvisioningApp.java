import examples.ProvisioningADUsersExample;
import oauth.OAuth;
import util.APIContext;

import javax.naming.NamingException;
import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @author Syncplicity
 * NOTE: this is not copyrighted material.         
 */
public class ADUsersProvisioningApp {

	private static void disableSslVerification() {
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
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}
	
	static {
		disableSslVerification();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println( "ActiveDirectory user provisioning app starting...");
		System.out.println("");
		
		/* 
		 * The sample app will show simplified examples of calls that you can make against the 
		 * api gateway using the available REST calls.
		 * 
		 * The example calls that this app will make include:
		 * 
		 * Authorization
		 * - OUath authorization call (to allow this app to connect to the gateway and make API calls)
		 * 
		 * Provisioning
		 * - Creating new users associated with a company
		 * - Creating a new user group (a group as defined in Syncplicity as having access to the same shared folders)
		 * - Associating the newly created users with the new user group
		 * 
		 * Content
		 * - Creating a Syncpoint to allow uploads/downloads to folders
		 * - Uploading a folder with one or more files in it.
		 * 
		 * Reporting
		 * - Scheduling an existingSyncplicity Report and download the CSV for that report
		 */

		OAuth.authenticate();
		
		System.out.println("");
		
		if( !APIContext.isAuthenticated() ) {
			System.err.println( "The OAuth authentication has failed, the app cannot continue." );
			System.exit(1);
		}
		else {
			System.out.println( "Authentication was successful." );			
		}

        try {
			ProvisioningADUsersExample.execute();
        } catch (NamingException e) {
            e.printStackTrace();
        }

        System.out.println("");
		System.out.println("Sample ActiveDirectory user provisioning app has completed execution.");
	}

}
