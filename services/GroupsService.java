package services;

import util.APIGateway;
import entities.Group;

import java.util.UUID;

/**
 * Class for requests to group.svc and groups.svc
 *
 */
public class GroupsService 
	extends APIGateway {


	/**
	 * Gets or sets url to Groups service.
	 */
	protected static String groupsUrl;

	/**
	 * Gets or sets url to Group service.
	 */
	protected static String groupUrl;

	protected static String userGroupsUrl;

	static {
		groupsUrl = provisioningAPIUrlPrefix + "groups.svc/%s/groups";
		groupUrl = provisioningAPIUrlPrefix + "group.svc/%s";
		userGroupsUrl = provisioningAPIUrlPrefix + "users_groups.svc/user/%s/groups";
	}

	/**
	 * Creates new groups in company.
	 * 
	 * @param companyGuid Company Guid.
	 * @param groups Array of groups to be created.
	 * 
	 * @return Array of created groups.
	 */
	public static Group[] createGroups(String companyGuid, Group[] groups) {
		return httpPost(String.format(groupsUrl, companyGuid), "application/json", groups );
	}

	/**
	 * Deletes group by Guid.
	 * 
	 * @param groupGuid Group Guid.
	 */
	public static void deleteGroup(UUID groupGuid) {
		httpDelete(String.format(groupUrl, groupGuid), Group.class);
	}

	/**
	 * Gets groups of a user in company.
	 *
	 * @param userGuid USER GUID.
	 *
	 * @return Groups entity.
	 */
	public static Group[] getUsersGroups(String userGuid) {
		return httpGet(String.format(userGroupsUrl, userGuid), Group[].class);

	}

	/**
	 * Gets groups in company.
	 *
	 * @param companyGuid Company GUID.
	 *
	 * @return Groups entity.
	 */
	public static Group[] getCompanyGroups(String companyGuid) {
		return httpGet(String.format(groupsUrl, companyGuid), Group[].class);
	}
}
