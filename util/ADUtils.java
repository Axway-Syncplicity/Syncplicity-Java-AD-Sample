package util;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;

import static util.ADConnection.getADContextObjects;

/**
 * @author Aravind Kumar Tadakamalla
 */
public class ADUtils {


    /**
     * Retrieves a list of Users from AD/LDAP for a given search Filter.
     *
     * @return NamingEnumeration of either Users from Active Directory.
     */
    public static NamingEnumeration<SearchResult> getADUsers() {

        String searchFilter = "(&(objectClass=inetOrgPerson))";
        String[] returnedAttributes = {"CN", "mail"};
        return getADContextObjects(returnedAttributes, searchFilter);
    }

    /**
     * Retrieves a list of groups from AD/LDAP for a given search Filter.
     *
     * @return NamingEnumeration of either groups from Active Directory.
     */
    public static NamingEnumeration<SearchResult> getADGroups() {

        String searchFilter = "(&(objectClass=groupOfUniqueNames))";
        String[] returnedAttributes = {"CN", "mail"};
        return getADContextObjects(returnedAttributes, searchFilter);
    }

    /**
     * Retrieves a list of groups a user is associated with on the Active Directory.
     *
     * @param userEmail Target user's email
     * @return ArrayList of user's groups.
     */
    public static ArrayList<String> getADUsersGroups(String userEmail) throws NamingException {
        SearchResult userEntry = LookupUserByEmail(userEmail);
        String userDn = userEntry.getNameInNamespace();

        ArrayList<String> userADGroups = new ArrayList<>();
        String searchFilter = String.format("(&(uniqueMember=%s))", userDn);

        String[] returnedAttributes = {"cn"};
        NamingEnumeration<SearchResult> containingGroupObjects = getADContextObjects(returnedAttributes, searchFilter);

        while (containingGroupObjects.hasMoreElements()) {
            SearchResult sr = containingGroupObjects.next();

            Attributes attrs = sr.getAttributes();
            String groupName = (String) attrs.get("cn").get();
            userADGroups.add(groupName);
        }
        return userADGroups;
    }

    private static SearchResult LookupUserByEmail(String email) throws NamingException {
        String searchFilter = String.format("(&(objectClass=inetOrgPerson)(mail=%s))", email);

        String[] returnedAttributes = { };
        NamingEnumeration<SearchResult> userLookupResult = getADContextObjects(returnedAttributes, searchFilter);
        return userLookupResult.next();
    }

    /**
     * Pauses the program until user hits 'enter', prompting to do so.
     */
    public static void proceedToEnter() throws IOException {
        System.out.println("Press enter to continue...");
        System.in.read();
    }
}