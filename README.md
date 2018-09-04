# Syncplicity-Java-AD-Sample

## Description

This sample app is similar to the Java command-line sample app
but has been extended to show how a developer might invoke various APIs against Active Directory (AD)
to provision users and assign those users to various user groups based on the metadata that is configured inside AD.

### Active Directory Structure

Active Directory is very flexible in regards to its schema (structure).
Although there are well-known schema, such as Microsoft Active Directory,
in general case the schemas might be very different from one organization to another.

This sample targets a schema with the following aspects:

* Two object classes are present:
  * Users (`objectClass=inetOrgPerson`)
  * Groups (`objectClass=groupOfUniqueNames`)
* Group important properties:
  * Group name: `cn` attribute
  * Members: `uniqueMember` multi-valued attribute, listing `DN`'s of group member users
* User important properties:
  * Full name: `cn` attribute
  * Email address: `mail` attribute

To work with directory of a different structure,
the code would need to be changed in the way it queries the AD and which object attributes it reads.
Please contact your system administrators for help with discovering your AD schema.

## System Requirements

* Java SE SDK: 1.8+

## Usage

This sample application demonstrates usage of Syncplicity APIs. This is what you need to know or do before you begin to use Syncplicity APIs:

* Make sure you have an Enterprise Edition account you can use to login to the Developer Portal at <https://developer.syncplicity.com>.
* Log into Syncplicity Developer Portal using your Syncplicity login credentials.
  Only Syncplicity Enterprise Edition users are allowed to login to the Developer Portal.
  Based on the configuration done by your Syncplicity administrator,
  Syncplicity Developer Portal will present one of the following options for login:
  * Basic Authentication using Syncplicity username and password.
  * Enterprise Single Sign-on using the Web-SSO service used by your organization. We support ADFS, OneLogin, Ping and Okta.
* Once you have successfully logged in for the first time,
  you must create an Enterprise Edition sandbox account in the Developer Portal.
  This account can be used to safely test your application using all Syncplicity features
  without affecting your company production data.
  * Log into Syncplicity Developer Portal. Click 'My Profile' and then 'Create sandbox'.
    Refer to the documentation for guidance: <https://developer.syncplicity.com/documentation/overview>.
  * You can log into <https://my.syncplicity.com> using the sandbox account.
    Note that the sandbox account email has "-apidev" suffix.
    So, assuming you regular account email is user@domain.com,
    use user-apidev@domain.com email address to log in to your sandbox account.
* Setup your developer sandbox account:
  * Log into the sandbox account at <https://my.syncplicity.com> to make sure its correctly provisioned and that you can access it.
  * Go to the 'Account' menu.
  * Click "Create" under "Application Token" section.
    The token is used to authenticate an application before making API calls.
    Learn more [here](https://syncplicity.zendesk.com/hc/en-us/articles/115002028926-Getting-Started-with-Syncplicity-APIs).
* Review API documentation by visiting Docs page on the <https://developer.syncplicity.com>.
* Register you app in the Developer Portal to obtain the "App Key" and "App Secret".
  
## Running

### Basic sample

1. Clone the sample project.
2. Use your favorite Java IDE to open the `.project` file.
3. Define new app on <https://developer.syncplicity.com>. The app key and app secret values are found in the application page.
    The Syncplicity admin token is found on the "My Account" page of the Syncplicity administration page.
    Use the "Application Token" field on that page to generate a token.
4. Update key values in `resources\config.properties`:
    * Update the the consumer key value (`<App Key>`)
    * Update the consumer secret (`<App Secret>`)
    * Update the Syncplicity admin token (`<Admin Token>`)
    * Update the EE account owner email, typically the sandbox owner email for development purposes (`<Owner Email>`)
5. Determine the settings of the target Active Directory (LDAP) server in `resources\config.properties`:
    * Active directory server url (`adUrl`)
    * Active directory login DN. This will be the user that the application uses to query the active directory (`queryUserBaseDN`).
        **Note**: this is not the account name, you need to get the Distinguished Name (DN) of the user.
    * Active directory password for the above user (`queryUserPassword`).
        **Note**: the sample supports direct authentication on the active directory server. SSO-type authentication might not work.
    * Active directory subtree to specify where to search for users and groups (`querySearchBase`).
6. Configure the IDE to run as an application and use the `ADUsersProvisioningApp` class on launch.
7. Build and Run the application.

### Debugging with Fiddler

By default, the sample app will not be captured by Fiddler.
To make Fiddler capture requests, specify proxy address and port in JVM startup parameters:

    -DproxyHost=127.0.0.1
    -DproxyPort=8888

* If you run the sample from console, add these two parameters to `java.exe` command-line arguments.
* If you use IntelliJ IDEA, make sure to add them to 'VM options' parameter at Run -> Edit Configurations.
* For other IDEs, please refer to their documentation.

Additionally, you will most likely need to disable SSL certificate validation to see HTTPS traffic to Syncplicity.
WARNING: this should never be done in production code! Disabling validation puts application users at risk of
a malicious person intercepting their data without their knowledge.
SSL validation can be disabled by uncommenting this line in `ADUsersProvisioningApp.java`:

    static {
      // unsafeDisableSslVerification();
    }

## Contributing

See contribute.md file in the root directory if you want to submit an issue or add new sample use case etc.

## Team

![alt text][Axwaylogo] Axway Syncplicity Team

[Axwaylogo]: https://github.com/Axway-syncplicity/Assets/raw/master/AxwayLogoSmall.png  "Axway logo"

## License

Apache License 2.0
