package util;

import java.io.IOException;
import java.io.InputStream;

public class ConfigurationHelper {
	private final static String PROP_FILE_NAME = "config.properties";

	/* Should not need to edit these values */

	private final static String BASE_API_ENDPOINT       = "https://api.syncplicity.com/";
	private final static String OAUTH_TOKEN_URL         = BASE_API_ENDPOINT + "oauth/token";
	private final static String OAUTH_REVOKE_TOKEN_URL  = BASE_API_ENDPOINT + "oauth/revoke";

	private final static String SIMPLE_PASSWORD = "123123aA";

	private static java.util.Properties settings = null;

	/**
	 * If the config files hasn't been loaded, load it from disk
	 */
	private static java.util.Properties getSettings() {
		
		if (settings == null) {
			settings = new java.util.Properties();
			
			InputStream inputStream = ConfigurationHelper.class.getClassLoader().getResourceAsStream("resources/" + PROP_FILE_NAME);
			
			if (inputStream != null) {
				try {
					settings.load(inputStream);
				} 
				catch (IOException e) {
					System.err.println( "Error reading configuration file: ");
					e.printStackTrace();
				}
			}
		}
		return settings;
	}

	/**
	 * Url for retrieving the OAuth token associated with this app's current session with the api gateway
	 */
	public static String getOAuthTokenUrl() {
		return OAUTH_TOKEN_URL;
	}

	/**
	 * URL for login the application out of the api gateway and explicitly invalidating the OAuth token
	 */
	public static String getOAuthRevokeTokenUrl() {
		return OAUTH_REVOKE_TOKEN_URL;
	}

	/**
	 * Returns the application key as defined by the developer portal.
	 * You as a developer must log in to the developer portal and define an application
	 * which then allocates a developer application key and secret keys.
	 */
	public static String getApplicationKey() {
		return getSettings().getProperty("appKey", "");
	}

	/**
	 * Returns the application secret as defined by the developer portal.
	 * You as a developer must log in to the developer portal and define an application
	 * which then allocates a developer application key and secret keys.
	 */
	public static String getApplicationSecret() {
		return getSettings().getProperty("appSecret", "" );
	}
	
	/**
	 * Returns the syncplicity admin key.
	 * The Syncplicity admin key is configured per user in the Syncplicity admin console.
	 * The key allows this application to behave on behalf of that user only.
	 */
	public static String getSyncplicityAdminKey() {
		return getSettings().getProperty("syncplicityAdminToken", "" );
	}

	/**
	 * The owner email should be set to the email created during the initial login to the developer portal.
	 * For example, if you initiated the login to the developer portal as foo.bar@baz.com,
	 * then we instantiated a sandbox account with foo.bar-apidev@baz.com as the owner email.
	 */
	public static String getOwnerEmail() {
		return getSettings().getProperty("ownerEmail", "" );
	}

	/**
	 * Returns the base url of the api gateway
	 */
	public static String getBaseApiEndpointUrl() {
		return BASE_API_ENDPOINT;
	}

	/**
	 * Returns a simple password used for the reporting service
	 */
	public static String getSimplePassword() {
		return SIMPLE_PASSWORD;
	}

	//Get ActiveDirectory properties from config.properties

	/**
	 * Returns Active Directory or LDAP URL
	 */
	public static String getADUrl() {
		return getSettings().getProperty("adUrl", "" );
	}

	/**
	 * Returns AD query user
	 */
	public static String getADQueryUserBaseDN() {
		return getSettings().getProperty("queryUserBaseDN", "" );
	}

	/**
	 * Returns AD query user Password
	 */
	public static String getADQueryUserPassword() {
		return getSettings().getProperty("queryUserPassword", "" );
	}

	/**
	 * Returns AD query search base
	 */
	public static String getADQuerySearchBase() {
		return getSettings().getProperty("querySearchBase", "" );
	}

}
