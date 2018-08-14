package util;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * @author Aravind Kumar Tadakamalla
 */
public class ADConnection {

    /**
     * Performs search in an Active Directory
     * @param returningAttributes Specifies the attributes that will be returned as part of the search.
     * null indicates that all attributes will be returned. An empty array indicates no attributes are returned.
     * @param searchPattern the filter expression to use for the search; may not be null
     * @return an enumeration of <tt>SearchResult</tt>s for the objects that satisfy the filter.
     */
    public static NamingEnumeration<SearchResult> getADContextObjects(String[] returningAttributes, String searchPattern) {
        DirContext ldapContext;
        NamingEnumeration<SearchResult> objects = null;
        try {
            Hashtable<String, String> ldapEnvironmentAttributes = new Hashtable<>();
            ldapEnvironmentAttributes.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            ldapEnvironmentAttributes.put(Context.PROVIDER_URL, ConfigurationHelper.getADUrl());
            ldapEnvironmentAttributes.put(Context.SECURITY_AUTHENTICATION, "simple");
            ldapEnvironmentAttributes.put(Context.SECURITY_PRINCIPAL, ConfigurationHelper.getADQueryUserBaseDN());
            ldapEnvironmentAttributes.put(Context.SECURITY_CREDENTIALS, ConfigurationHelper.getADQueryUserPassword());
            ldapContext = new InitialDirContext(ldapEnvironmentAttributes);

            SearchControls searchControls = new SearchControls();

            searchControls.setReturningAttributes(returningAttributes);

            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String searchBase = ConfigurationHelper.getADQuerySearchBase();

            objects = ldapContext.search(searchBase, searchPattern, searchControls);

            ldapContext.close();
        } catch (Exception e) {
            System.out.printf("LDAP connection error: %s%n", e);
            e.printStackTrace();
            System.exit(-1);
        }
        return objects;
    }
}
