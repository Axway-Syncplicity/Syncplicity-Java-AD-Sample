package util;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * @author Aravind Kumar Tadakamalla
 */
public class ADConnection {

    public static NamingEnumeration<SearchResult> getADContextObjects(String[] returnAtts, String searchPattern) throws NamingException {
        DirContext ldapContext;
        NamingEnumeration<SearchResult> objects = null;
        try {
            Hashtable<String, String> ldapEnvAtts = new Hashtable<String, String>();
            ldapEnvAtts.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            ldapEnvAtts.put(Context.PROVIDER_URL, ConfigurationHelper.getADUrl());
            ldapEnvAtts.put(Context.SECURITY_AUTHENTICATION, "simple");
            ldapEnvAtts.put(Context.SECURITY_PRINCIPAL, ConfigurationHelper.getADQueryUserBaseDN());
            ldapEnvAtts.put(Context.SECURITY_CREDENTIALS, ConfigurationHelper.getADQueryUserPassword());
            ldapContext = new InitialDirContext(ldapEnvAtts);

            SearchControls searchCtrls = new SearchControls();

            searchCtrls.setReturningAttributes(returnAtts);

            searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String searchBase = ConfigurationHelper.getADQuerySearchBase();

            objects = ldapContext.search(searchBase, searchPattern, searchCtrls);

            ldapContext.close();
        } catch (Exception e) {
            System.out.println("LDAP connection error: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
        return objects;
    }
}
