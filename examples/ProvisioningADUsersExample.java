package examples;

import entities.Group;
import entities.User;
import entities.UserAccountType;
import services.GroupMembersService;
import services.GroupsService;
import services.UsersService;
import util.ADUtils;
import util.APIContext;
import util.ConfigurationHelper;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;


public class ProvisioningADUsersExample {

    private static User[] createdUsers;
    private static Group[] createdGroups;

    /*
     * ProvisioningADUsersExample
     * - Retrieve the users in an OU from AD and create them in Syncplicity.
     * - Retrieve the groups in an OU from AD and create them in Syncplicity.
     * - Retrieve the user's groups from AD and map the Users on Syncplicity accordingly.
     * - Remove the users from their respective groups on Syncplicity.
     * - Delete the groups from Syncplicity.
     * - Delete the users from Syncplicity.
   */
    public static void execute() throws NamingException {

        createUsers(); //Retrieve the users in an OU from AD and create them in Syncplicity.
        createGroup(); //Retrieve the groups in an OU from AD and create them in Syncplicity.
        addUserToGroups(); //Retrieve the user's groups from AD and map the Users on Syncplicity accordingly.
        deleteUserFromGroups(); //Remove the users from their respective groups on Syncplicity.
        deleteGroup(); //Delete the groups from Syncplicity.
        deleteUsers(); //Delete the users from Syncplicity.
    }

    private static void deleteGroup() {

        if (createdGroups == null || createdGroups.length == 0) {
            return;
        }

        // remove created groups
        System.out.println("Start Groups Deletion...");

        for (Group group : createdGroups) {

            System.out.println("");
            System.out.println(String.format("\tDelete Group [%s].", group.Name));

            GroupsService.deleteGroup(group.Id);
        }

        System.out.println("");
        System.out.println("Finished Groups Deletion.");
        ADUtils.proceedToEnter();  //Just pauses the program until you hit 'enter'
    }

    private static void addUserToGroups() throws NamingException {

        if (createdUsers == null || createdUsers.length == 0 || createdGroups == null) {
            return;
        }

        System.out.println("");
        System.out.println("Start Adding Users to respective Groups...");
        User[] user1 = new User[1];
        user1[0] = null;
        for (User user : createdUsers) {

            user1[0] = user;
            //get user groups from AD
            ArrayList<String> userADGroups = ADUtils.getADUsersGroups(user.EmailAddress);
            for (String groupName : userADGroups) {
                String groupId = getGroupGUID(groupName);
                System.out.println(String.format("Adding user[%s] to group [%s].", user.EmailAddress, groupName));
                GroupMembersService.addGroupMembers(
                        groupId,
                        user1
                );

            }

        }

        System.out.println("Finish Adding Users to respective Groups.");
        ADUtils.proceedToEnter();  //Just pauses the program until you hit 'enter'
    }

    private static void deleteUserFromGroups() throws NamingException {

        if (createdUsers == null || createdUsers.length == 0 || createdGroups == null) {
            return;
        }

        System.out.println("");
        System.out.println("Start Removing Users from Groups...");

        for (User user : createdUsers) {
            //get user groups from AD
            ArrayList<String> userADGroups = ADUtils.getADUsersGroups(user.EmailAddress);
            for (String groupName : userADGroups) {
                String groupId = getGroupGUID(groupName);
                System.out.println(String.format("Removing user[%s] from group [%s].", user.EmailAddress, groupName));
                GroupMembersService.deleteGroupMember(
                        groupId,
                        user.EmailAddress);

            }

        }

        System.out.println("Finish Removing Users from respective Groups.");
        ADUtils.proceedToEnter();  //Just pauses the program until you hit 'enter'
    }

    private static void createGroup() throws NamingException {

        System.out.println("");

        String companyGuid = APIContext.getCompanyGuid();
        if (companyGuid == null || companyGuid.isEmpty()) {
            System.err.println("Group Can't Be Created Because Company Guid Wasn't Received During Authorization.");
            return;
        }

        NamingEnumeration<SearchResult> adGroups = ADUtils.getADGroups();
        List<Group> groupsToAdd = new ArrayList<Group>();

        int totalGroups = 0;
        while (adGroups.hasMoreElements()) {
            SearchResult sr;
            totalGroups++;
            sr = (SearchResult) adGroups.next();
            System.out.println(sr.getName().substring(sr.getName().indexOf("=") + 1).trim());
            System.out.println("Start Group Creation...");

            Group group = new Group();
            group.Name = sr.getName().substring(sr.getName().indexOf("=") + 1).trim();
            group.Owner = new User();
            group.Owner.EmailAddress = ConfigurationHelper.getOwnerEmail();

            groupsToAdd.add(group);
        }
        createdGroups = GroupsService.createGroups(companyGuid, groupsToAdd.toArray(new Group[groupsToAdd.size()]));

        System.out.println("");

        if (createdGroups == null || createdGroups.length == 0) {
            System.err.println("Error Occured During Creating Group.");
            return;
        }

        for (Group createdGroup : createdGroups)
            System.out.println(String.format("Finished Group Creation. New Group: %s", createdGroup.Name));

        System.out.println("Total number of Groups: " + totalGroups);
        System.out.println("Finish Groups Creation.");
        ADUtils.proceedToEnter();  //Just pauses the program until you hit 'enter'

    }

    private static void createUsers() throws NamingException {

        System.out.println("");
        System.out.println("Start Users Creation...");

        List<User> usersToAdd = new ArrayList<User>();

        NamingEnumeration<SearchResult> users = ADUtils.getADUsers();

        int totalUsers = 0;
        while (users.hasMoreElements()) {
            SearchResult sr;
            totalUsers++;
            sr = (SearchResult) users.next();
            System.out.println(">>>" + sr.getName());
            Attributes attrs = sr.getAttributes();


            User user = new User();
            user.AccountType = UserAccountType.LimitedBusiness;
            user.EmailAddress = attrs.get("mail").toString().substring(attrs.get("mail").toString().indexOf(":") + 1).trim();
            user.Password = ConfigurationHelper.getSimplePassword();
            user.FirstName = attrs.get("givenName").toString().substring(attrs.get("givenName").toString().indexOf(":") + 1).trim();
            user.LastName = attrs.get("sn").toString().substring(attrs.get("sn").toString().indexOf(":") + 1).trim();

            usersToAdd.add(user);
            System.out.println("");
            System.out.println(String.format("\tChecking if user [%s] already exists.", user.EmailAddress));
            //Checking if we've created this user already, if so, we'll delete it, and then recreate it again (below).
            User checkUser = UsersService.getUser(user.EmailAddress, true);

            System.out.println("");

            if (checkUser != null) {
                //If this is the second time running, we'll need to clean up (delete) previous run's users
                //This is just to keep the sample code working as if it was the first time run...
                System.out.println(String.format("\tUser found. Cleanup of User  [%s].  Deleting user that may have been created from previous Sample App run.", user.EmailAddress));
                try {
                    UsersService.deleteUser(user.EmailAddress);
                } catch (Exception e) {
                    //ignore exceptions for sample app only
                }
            } else {
                System.out.println("\tNo user found in company, continuing successfully.");
            }
        }
        System.out.println("Total number of Users: " + totalUsers);

        System.out.println("");
        System.out.println("Calling UsersService to add users");
        createdUsers = UsersService.createUsers(usersToAdd.toArray(new User[usersToAdd.size()]));

        System.err.println("");
        if (createdUsers == null || createdUsers.length == 0) {

            int addedCount = (createdUsers == null ? 0 : createdUsers.length);

            System.err.println(String.format("\tError Occured During Creating Some Of Users. Number of Created Users:%d", addedCount));
        } else {
            System.out.println("\tFinished Users Creation.");
        }

        ADUtils.proceedToEnter();  //Just pauses the program until you hit 'enter'
    }

    private static void deleteUsers() {

        if (createdUsers == null || createdUsers.length == 0) {
            return;
        }

        System.out.println("");
        System.out.println("Press enter before Users Deletion...");

        // remove created users
        System.out.println("Start Users Deletion...");

        for (User user : createdUsers) {

            System.out.println("");
            System.out.println(String.format("\tDelete User [%s].", user.EmailAddress));

            UsersService.deleteUser(user.EmailAddress);
        }

        System.out.println("");
        System.out.println("Finished Users Deletion.");
        ADUtils.proceedToEnter();  //Just pauses the program until you hit 'enter'
    }

    /**
     * Gets GroupGUID for a given groupName
     * @author Aravind Kumar Tadakamalla
     * @param groupName Group Guid.
     *
     * @return String ID of the group.
     */
    private static String getGroupGUID(String groupName) {
        String retGroupID = null;
        String companyGUID = APIContext.getCompanyGuid();
        Group[] companyGroups= GroupsService.getCompanyGroups(companyGUID);

        for (Group group : companyGroups) {
            if (group.Name.equalsIgnoreCase(groupName)) {
                retGroupID = group.Id;
                break;
            }
        }
        return retGroupID;
    }

}
