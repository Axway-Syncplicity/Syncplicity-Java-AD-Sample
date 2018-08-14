package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import oauth.OAuth;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public abstract class APIGateway {
	
	private final static String ACCEPT_HEADER = "Accept";
	private final static String JSON_CONTENT_TYPE = "application/json";

	protected static String provisioningAPIUrlPrefix;

	static {
		provisioningAPIUrlPrefix = ConfigurationHelper.getBaseApiEndpointUrl() + "provisioning/";
	}

	/**
	 * Creates request object to invoke the api REST call.
	 * 
	 * @param method The request's method.
	 * @param uri The url of request.
	 * 
	 * @return Created request.
	 */
	private static HttpURLConnection createRequest(String method, String uri, boolean isAuthenticationCall )
			throws IOException {
		
		System.out.println(String.format("Creating %s request to %s", method.toUpperCase(), uri));

		URL url = new URL(uri);

		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.setRequestMethod(method.toUpperCase());
		request.setRequestProperty(ACCEPT_HEADER, JSON_CONTENT_TYPE);
		request.setConnectTimeout(15000);
		request.setDoOutput(true);
		request.setDoInput(true);

		return applyConsumerCredentials(request, isAuthenticationCall );
	}

	/**
	 * Writes the body to the request.
	 * 
	 * @param request The request object.
	 * @param body The string representation of body.
	 */
	private static void writeBody(HttpURLConnection request, String body, String contentType)
			throws IOException {
		

		if( contentType.equals( "application/json") ) {
			//This is just to pretty-print the JSON response to the console, you do not
			//need to do this of course for the real application that you would write.
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(body);
			body = gson.toJson(je);
			body = body.replaceAll(" ", "  " );
		}
		
		System.out.println( "[Body] " + body);

		OutputStream requestStream = request.getOutputStream();

		requestStream.write(body.getBytes(Charset.forName("UTF-8")));
		requestStream.flush();
		requestStream.close();
	}

	/**
	 * Applies the application key and secret to the request
	 * 
	 * @param request The request object.
	 * 
	 * @return The current request.
	 */
	private static HttpURLConnection applyConsumerCredentials(HttpURLConnection request, boolean isAuthenticationCall ) {

		//If this is the first OAuth authentication call, then we don't have an OAuth Bearer token (access token), so we will use the
		//Application Key and Application Secret as the consumer credentials for the application.  However, once we've successfully
		//connected to the api gateway for the first time, we will receive an OAuth access token (Bearer token), you will
		//need to manage that bearer token and use it for subsequent calls to the API gateway.

		if( isAuthenticationCall ) {
			
			String encoded = Base64.encode( (ConfigurationHelper.getApplicationKey() + ":" + ConfigurationHelper.getApplicationSecret()).getBytes() );
			
			System.out.println( "[Header] Authorization: Basic " + encoded + "\n" 
			                   + "\t\t(Base64 encoded combination of App key and App secret)\n" 
			                   + "\t\t" + ConfigurationHelper.getApplicationKey() + ":" + ConfigurationHelper.getApplicationSecret());
			System.out.println( "[Header] Sync-App-Token: " + ConfigurationHelper.getSyncplicityAdminKey() );
			request.addRequestProperty("Authorization", "Basic " + encoded);
			request.setRequestProperty("Sync-App-Token", ConfigurationHelper.getSyncplicityAdminKey()  );
		}
		else {
			System.out.println( "[Header] AppKey: " +  ConfigurationHelper.getApplicationKey() );
			System.out.println( "[Header] Authorization: Bearer " +  APIContext.getAccessToken() );
			request.setRequestProperty("AppKey", ConfigurationHelper.getApplicationKey());
			request.setRequestProperty("Authorization", "Bearer " + APIContext.getAccessToken() );
		}

		return request;
	}

	/**
	 * Reads the response from the request and returns the received object.
	 * 
	 * @param request The request object.
	 * @param classType The type of received object.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	private static <T> T readResponse(HttpURLConnection request, Class<T> classType, boolean suppressErrors) {
        return readResponse(request, classType, suppressErrors, null);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T readResponse(HttpURLConnection request, Class<T> classType, boolean suppressErrors, BooleanResult shouldRefreshToken ) {

		try {
			if( shouldRefreshToken != null ) {
				shouldRefreshToken.setResult(false);
			}
			
			System.out.println();
			System.out.println("Trying to read response...");

			try (InputStream responseStream = request.getInputStream()) {
				if (responseStream == null) {
					System.out.println("Response wasn't received.");
					return null;
				}

				String response = ReadStreamAsString(responseStream);

				if( StringUtils.isEmpty(response) || StringUtils.isWhitespace(response) ) {
					System.out.println("Received response is empty.");
					return null;
				}

				//This is just to pretty-print the JSON response to the console, you do not
				//need to do this of course for the real application that you would write.
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser jp = new JsonParser();
				JsonElement je = jp.parse(response);
				String prettyJsonString = gson.toJson(je);
				prettyJsonString = prettyJsonString.replaceAll( " ", "  " );

				System.out.println( "Response: \n" + prettyJsonString );

				if (!classType.isAssignableFrom(String.class)) {
					return JSONSerialization.deserialize(response, classType);
				}

				return (T) response;
			}
		} 
		catch (IOException e) {
			
			if( !suppressErrors ) {
				System.err.println();
				System.err.println(String.format("\tError occurs during request to %s.", request.getURL().toString()));
				
				try {
					System.err.println(String.format("\tReceived: %d %s.", request.getResponseCode(), request.getResponseMessage()));
					String errorMessage;
					try (InputStream errorStream = request.getErrorStream()){
						if(errorStream != null) {
							errorMessage = ReadStreamAsString(errorStream);
						} else {
							errorMessage = null;
						}
					}

					System.err.printf("\t\t%s\n", errorMessage);
				} catch (IOException e1) {
					// Do NOT reproduce empty catch blocks in production code. Exceptions must be either rethrown, or handled.
				}
				
				e.printStackTrace();
			}
			
			try {
				// it's needed to authorize again and then send the same request again
                if ( shouldRefreshToken != null &&
                    (request.getResponseCode() == 401 ||
                    (request.getResponseCode() == 403 && "Forbidden".equals(request.getResponseMessage()))) )
                {
                    shouldRefreshToken.setResult(true);
                }
			} catch (IOException e1) {
				// Do NOT reproduce empty catch blocks in production code. Exceptions must be either rethrown, or handled.
			}
		}

		return null;
	}

	private static String ReadStreamAsString(InputStream errorStream) throws IOException {
		String errorMessage;
		try (InputStreamReader reader = new InputStreamReader(errorStream)) {
			try (BufferedReader bufferedReader = new BufferedReader(reader)) {
				StringBuilder errorMessageBuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					errorMessageBuilder.append(line);
					errorMessageBuilder.append("\n");
				}

				errorMessage = errorMessageBuilder.toString();
			}
		}
		return errorMessage;
	}


	/**
	 * Create GET HTTP request to url and return deserialized object of type
	 * type.
	 * 
	 * @param uri       The request url.
	 * @param classType The type of returned object.
	 * @
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpGet(String uri, Class<T> classType) {
		return httpGet( uri, classType, false );
	}
	
	/**
	 * Create GET HTTP request to url and return deserialized object of type
	 * type.
	 * 
	 * @param uri             The request url.
	 * @param classType      The type of returned object.
	 * @param suppressErrors boolean to determine if output should be print to console on errors
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpGet(String uri, Class<T> classType, boolean suppressErrors ) {
		HttpURLConnection request;
		String method = "GET";
		try {
			request = createRequest(method, uri, false);
		} catch (IOException e) {
			e.printStackTrace();
			
			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
        T response = readResponse(request, classType, suppressErrors, shouldRefreshToken);

        if (shouldRefreshToken.getResult())
        {
        	System.out.println();
        	System.out.println("Trying to re-authenticate using the same credentials.");

            // it's needed to authorize again
            // trying to do it and then re-send the initial request
            OAuth.refreshToken();

            System.out.println();
            if (!APIContext.isAuthenticated())
            {
            	System.out.println("The OAuth authentication has failed, GET request can't be performed.");
                return null;
            }

            System.out.println("Authentication was successful. Trying to send GET request again for the last time.");

            try {
    			request = createRequest(method, uri, false);
    		} catch (IOException e) {
    			e.printStackTrace();
    			
    			return null;
    		}
            response = readResponse(request, classType, suppressErrors );
        }
        
		return response;
	}

	/**
	 * Create POST HTTP request to url with body and return deserialized object
	 * of type classType.
	 * 
	 * @param uri       The request url.
	 * @param body      The request body.
	 * @param classType The type of returned object.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpPost(boolean isAuthenticationCall, String uri, String contentType, String body, Class<T> classType) {
		HttpURLConnection request;
		String method = "POST";
		try {
			request = createRequest(method, uri, isAuthenticationCall );
			request.setRequestProperty("Content-Type", contentType );

			writeBody(request, body, contentType);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
        T response = readResponse(request, classType, false, shouldRefreshToken);
        
        if (!isAuthenticationCall && shouldRefreshToken.getResult())
        {
        	System.out.println();
        	System.out.println("Trying to re-authenticate using the same credentials.");

            // it's needed to authorize again
            // trying to do it and then re-send the initial request
            OAuth.refreshToken();

            System.out.println();
            if (!APIContext.isAuthenticated())
            {
            	System.out.println("The OAuth authentication has failed, POST request can't be performed.");
                return null;
            }

            System.out.println("Authentication was successful. Trying to send POST request again for the last time.");
            
            try {
            	request = createRequest(method, uri, false);
    			request.setRequestProperty("Content-Type", contentType );

    			writeBody(request, body, contentType);
    		} catch (IOException e) {
    			e.printStackTrace();  			
    			return null;
    		}
            
            response = readResponse(request, classType, false);
        }
        		
		return response;
	}

	/**
	 * Create POST HTTP request to url with entity and return deserialized
	 * object of type type.
	 * 
	 * @param uri The request url.
	 * @param entity The entity.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T httpPost(String uri, String contentType, T entity ) {
		
		return httpPost(false, uri, contentType, JSONSerialization.serialize(entity), (Class<T>) entity.getClass());
	}

	/**
	 * Create PUT HTTP request to url with body and return deserialized object
	 * of type classType.
	 * 
	 * @param uri The request url.
	 * @param body The request body.
	 * @param classType The type of returned object.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpPut(String uri, String body, Class<T> classType) {
		HttpURLConnection request;
		String method = "PUT";
		try {
			request = createRequest(method, uri, false);
			request.setRequestProperty("Content-Type", JSON_CONTENT_TYPE);

			writeBody(request, body, JSON_CONTENT_TYPE);
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
        T response = readResponse(request, classType, false, shouldRefreshToken);
        
        if (shouldRefreshToken.getResult())
        {
        	System.out.println();
        	System.out.println("Trying to re-authenticate using the same credentials.");

            // it's needed to authorize again
            // trying to do it and then re-send the initial request
            OAuth.refreshToken();

            System.out.println();
            if (!APIContext.isAuthenticated())
            {
            	System.out.println("The OAuth authentication has failed, PUT request can't be performed.");
                return null;
            }

            System.out.println("Authentication was successful. Trying to send PUT request again for the last time.");
            
            try {
            	request = createRequest(method, uri, false);
            	request.setRequestProperty("Content-Type", JSON_CONTENT_TYPE);

            	writeBody(request, body, JSON_CONTENT_TYPE);
    		} catch (IOException e) {
    			
    			e.printStackTrace();  			
    			return null;
    		}
            
            response = readResponse(request, classType, false);
        }
        
        return response;
	}

	/**
	 * Create PUT HTTP request to url with entity and return deserialized object
	 * of type T.
	 * 
	 * @param uri The request url.
	 * @param entity The entity.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T httpPut(String uri, T entity) {
		
		return httpPut(uri, JSONSerialization.serialize(entity), (Class<T>) entity.getClass());
	}

	/**
	 * Create DELETE HTTP request to url with body and return deserialized
	 * object of type classType.
	 * 
	 * @param uri The request url.
	 * @param classType The type of returned object.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpDelete(String uri, Class<T> classType) {
		HttpURLConnection request;
		String method = "DELETE";
		try {
			request = createRequest(method, uri, false);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
        T response = readResponse(request, classType, false, shouldRefreshToken);
        
        if (shouldRefreshToken.getResult())
        {
        	System.out.println();
        	System.out.println("Trying to re-authenticate using the same credentials.");

            // it's needed to authorize again
            // trying to do it and then re-send the initial request
            OAuth.refreshToken();

            System.out.println();
            if (!APIContext.isAuthenticated())
            {
            	System.out.println("The OAuth authentication has failed, DELETE request can't be performed.");
                return null;
            }

            System.out.println("Authentication was successful. Trying to send DELETE request again for the last time.");
            
            try {
            	request = createRequest(method, uri, false);
    		} catch (IOException e) {
    			
    			e.printStackTrace();  			
    			return null;
    		}
            
            response = readResponse(request, classType, false);
        }
        
		return response;
	}
}
