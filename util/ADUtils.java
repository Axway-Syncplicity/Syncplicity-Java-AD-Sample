package util;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Scanner;

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
    public static NamingEnumeration<SearchResult> getADUsers() throws NamingException {

        String searchFilter = "(&(objectClass=user))";
        String[] returnedAtts = {"memberOf", "CN", "mail", "sn", "givenName", "samAccountName"};
        return getADContextObjects(returnedAtts, searchFilter);
    }

    /**
     * Retrieves a list of groups from AD/LDAP for a given search Filter.
     *
     * @return NamingEnumeration of either groups from Active Directory.
     */
    public static NamingEnumeration<SearchResult> getADGroups() throws NamingException {

        String searchFilter = "(&(objectClass=group))";
        String[] returnedAtts = {"memberOf", "CN", "mail", "sn", "givenName", "samAccountName"};
        return getADContextObjects(returnedAtts, searchFilter);
    }

    /**
     * Retrieves a list of groups a user is associated with on the Active Directory.
     *
     * @param userEmail It is email address of the user on AD/LDAP.
     * @return ArrayList of user's groups.
     */
    public static ArrayList<String> getADUsersGroups(String userEmail) throws NamingException {

        ArrayList<String> userADGroups = new ArrayList<String>();
        String searchFilter = "(&(objectClass=user)(mail=%s))";

        searchFilter = String.format(searchFilter, userEmail);

        String[] returnedAtts = {"memberOf", "CN", "mail", "sn", "givenName", "samAccountName"};
        NamingEnumeration<SearchResult> objects = getADContextObjects(returnedAtts, searchFilter);

        while (objects.hasMoreElements()) {
            SearchResult sr = (SearchResult) objects.next();

            Attributes attrs = sr.getAttributes();
            Attribute groupMembers = attrs.get("memberOf");

            for (int i = 0; i < groupMembers.size(); i++) {
                userADGroups.add(groupMembers.get(i).toString().substring(groupMembers.get(i).toString().indexOf("=") + 1, groupMembers.get(i).toString().indexOf(",")));
            }
        }
        return userADGroups;
    }


    public static void proceedToEnter() {
        System.out.println("Press enter to continue...");
        Scanner keyboard = new Scanner(System.in);
        keyboard.nextLine();
        System.out.println("");
        keyboard.close();
    }
}