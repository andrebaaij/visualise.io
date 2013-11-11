/**
 * Copyright 2012 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import models.UserIdentity;

import org.joda.time.DateTime;

import play.Application;
import play.Logger;
import scala.Option;
import securesocial.core.AuthenticationMethod;
import securesocial.core.Identity;
import securesocial.core.PasswordInfo;
import securesocial.core.SocialUser;
import securesocial.core.UserIdFromProvider;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;
import commons.database.DatabaseConnection;

import play.db.*;

/**
 * A Sample In Memory user service in Java
 * 
 * Note: This is NOT suitable for a production environment and is provided only
 * as a guide. A real implementation would persist things in a database
 */
public class InMemoryUserService extends BaseUserService {
	private HashMap<String, UserIdentity> users = new HashMap<String, UserIdentity>();
	private HashMap<String, Token> tokens = new HashMap<String, Token>();

	public InMemoryUserService(Application application) {
		super(application);
	}

   /**
    * Saves the user.  This method gets called when a user logs in.
    * This is your chance to save the user information in your backing store.
    * @param user
    */
	@Override
	public UserIdentity doSave(Identity user) {
		System.out.println("doSave(user)");
		// First check if the user already exists in the users hashmap
		if (users.containsKey(user.userIdFromProvider().authId() + user.userIdFromProvider().providerId())) {
			return users.get(user.userIdFromProvider().authId() + user.userIdFromProvider().providerId());
		}
		// Now insert the user in the database if it does not exist yet
		else {
			DatabaseConnection db;
			try {
				ArrayList<ArrayList<Object>> resultSet;
				
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				resultSet = db.executeQuery(
				      DatabaseConnection.SELECT,
				      "select fnSetUser(?,?,?,?,?,?,?);",
				      user.firstName(), user.lastName(),
				      user.userIdFromProvider().authId(), user.passwordInfo().get().salt().get(),
				      user.passwordInfo().get().password(), user.userIdFromProvider().providerId());
				
				UserIdentity userIdentity = new UserIdentity(user);
				userIdentity.setId((Integer) resultSet.get(1).get(0));
				users.put(user.userIdFromProvider().authId() + user.userIdFromProvider().providerId(), userIdentity);
				
				return userIdentity;
			} catch (Exception e) {
				// If an error occurred we must log this and return nothing, as the action could not be succesfully completed.
				Logger.error(e.toString());
				return null;
			}
		}
	}

	@Override
	public void doSave(Token token) {
		System.out.println("doSave(token)");
		if (!tokens.containsKey(token.uuid)) {
			DatabaseConnection db;
			try {
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				db.executeQuery(
			      DatabaseConnection.INSERT,
			      "insert into `userToken` (id, email, isSignUp, creationTime, expirationTime) " +
			      "values (?,?,?,?,?);",
			      token.uuid,
			      token.email,
			      token.isSignUp,
			      token.creationTime,
			      token.expirationTime
					);
				tokens.put(token.uuid, token);
			} catch (Exception e) {
				// If an error occurred we must log this and return nothing, as the action could not be succesfully completed.
				Logger.error(e.toString());
			}
		}
	}

	@Override
	public Identity doFind(UserIdFromProvider userId) {
		System.out.println("doFind(userId)");
		if (users.containsKey(userId.authId() + userId.providerId())) {
			return users.get(userId.authId() + userId.providerId());
		}
		else {
			DatabaseConnection db;
			try {
				ArrayList<ArrayList<Object>> resultSet;
	
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				resultSet = db.executeQuery(
				            DatabaseConnection.SELECT,
				            "select id, firstName, lastName, email, salt, password from user where email = ? and providerId = ?",
				            userId.authId(),userId.providerId());
				
				if (resultSet.size() > 1) {
					String firstName = (String) resultSet.get(1).get(1);
					String lastName = (String) resultSet.get(1).get(2);
					Option<String> email = Option.apply((String) resultSet.get(1).get(3));
					Option<String> salt = Option.apply((String) resultSet.get(1).get(4));
					String fullName = firstName + " " + lastName;
					Option<PasswordInfo> passwordInfo = Option.apply(new PasswordInfo("bcrypt", (String) resultSet.get(1).get(5), salt));
					
					Identity user = new SocialUser(userId, firstName, lastName, fullName,
					      email, null, 
					      AuthenticationMethod.UserPassword(), 
				      	null, //oAuth1Info is not used
					      null, //oAuth2Info is not used
					      passwordInfo);
					
					UserIdentity userIdentity = new UserIdentity(user);
					userIdentity.setId((Integer) resultSet.get(1).get(0));
					users.put(user.userIdFromProvider().authId() + user.userIdFromProvider().providerId(), userIdentity);
					return userIdentity;
				}
				else return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public Token doFindToken(String tokenId) {
		System.out.println("doFindToken(tokenId)");
        if (tokens.containsKey(tokenId)) {
			return tokens.get(tokenId);
		}
		else {
			DatabaseConnection db;
			try {
				ArrayList<ArrayList<Object>> resultSet;
	
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				resultSet = db.executeQuery(
				            DatabaseConnection.SELECT,
				            "select email, isSignUp, creationTime, expirationTime from user where id = ?",
				            tokenId);
				
				if (resultSet.size() > 1) {
					Token token = new Token();
					token.uuid = tokenId;
					token.email = (String) resultSet.get(1).get(0);
					token.isSignUp = (Boolean) resultSet.get(1).get(1);
					token.creationTime = (DateTime) resultSet.get(1).get(2);
					token.expirationTime = (DateTime) resultSet.get(1).get(3);
					return token;
				}
				else return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public UserIdentity doFindByEmailAndProvider(String sEmail, String providerId) {
		System.out.println("doFindByEmailAndProvider(sEmail, providerId)");
		if (users.containsKey(sEmail + providerId)) {
			return users.get(sEmail + providerId);
		}
		else {
			DatabaseConnection db;
			try {
				ArrayList<ArrayList<Object>> resultSet;
	
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				resultSet = db.executeQuery(
				            DatabaseConnection.SELECT,
				            "select id, firstName, lastName, email, salt, password from user where email = ? and providerId = ?",
				            sEmail,providerId);
				
				if (resultSet.size() > 1) {
					UserIdFromProvider userId = new UserIdFromProvider(sEmail, providerId);
					

					String firstName = (String) resultSet.get(1).get(1);
					String lastName = (String) resultSet.get(1).get(2);
					Option<String> email = Option.apply((String) resultSet.get(1).get(3));
					Option<String> salt = Option.apply((String) resultSet.get(1).get(4));
					String fullName = firstName + " " + lastName;
					Option<PasswordInfo> passwordInfo = Option.apply(new PasswordInfo("bcrypt", (String) resultSet.get(1).get(5), salt));
					
					Identity user = new SocialUser(userId, firstName, lastName, fullName,
					      email, null, 
					      AuthenticationMethod.UserPassword(), 
				      	null, //oAuth1Info is not used
					      null, //oAuth2Info is not used
					      passwordInfo);
					
					UserIdentity userIdentity = new UserIdentity(user);
					userIdentity.setId((Integer) resultSet.get(1).get(0));
					users.put(user.userIdFromProvider().authId() + user.userIdFromProvider().providerId(), userIdentity);
					return userIdentity;
					
				}
				else return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public void doDeleteToken(String uuid) {
		tokens.remove(uuid);
	}

	@Override
	public void doDeleteExpiredTokens() {
		Iterator<Map.Entry<String, Token>> iterator = tokens.entrySet()
		      .iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Token> entry = iterator.next();
			if (entry.getValue().isExpired()) {
				iterator.remove();
			}
		}
	}
}