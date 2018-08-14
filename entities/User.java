package entities;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@SuppressWarnings("serial")
public class User implements Serializable {
	public UUID Id;

	public String EmailAddress;

	public String FirstName;

	public String LastName;

	public String Password;

	public Boolean Disabled = null;

	public UserAccountType AccountType = entities.UserAccountType.values()[0];

	public Date LastLoginDateUtc = null;

	public Date CreatedDateUtc;
}