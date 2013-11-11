package models;

import securesocial.core.AuthenticationMethod;
import securesocial.core.Identity;
import securesocial.core.OAuth1Info;
import securesocial.core.OAuth2Info;
import securesocial.core.PasswordInfo;
import securesocial.core.SocialUser;
import securesocial.core.UserIdFromProvider;
import scala.Option;

public class UserIdentity extends SocialUser {
	/**
    * 
    */
	private static final long serialVersionUID = -6761233323083280324L;
	private Integer id;

	public UserIdentity(UserIdFromProvider id, String firstName,
			String lastName, String fullName, Option<String> email,
			Option<String> avatarUrl, AuthenticationMethod authMethod,
			Option<OAuth1Info> oAuth1Info, Option<OAuth2Info> oAuth2Info,
			Option<PasswordInfo> passwordInfo) {
		super(id, firstName, lastName, fullName, email, avatarUrl, authMethod, oAuth1Info,
				oAuth2Info, passwordInfo);
	}

	public UserIdentity(Identity user) {
		super(user.userIdFromProvider(), user.firstName(), user.lastName(),
				user.fullName(), user.email(), user.avatarUrl(), user
						.authMethod(), user.oAuth1Info(), user.oAuth2Info(),
				user.passwordInfo());
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}
}
