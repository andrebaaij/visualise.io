package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.db.*;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commons.database.DatabaseConnection;

import models.UserIdentity;

import securesocial.core.java.SecureSocial;

public class Templates  extends Controller{

	@SecureSocial.UserAwareAction
	public static Result query(String searchTerm, Integer count, Integer startIndex, String sort, String sortOrder) {
		UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);

		Integer default_count = 25,
			max_count = 100,
			default_startIndex = 0;
	
		String default_searchTerm = null,
				default_sort = "popular",
				default_sortOrder = "asc";
		
		Map<String, String> possible_sort = new HashMap<String,String>();
			possible_sort.put("popular", "`u`.`count`");
			possible_sort.put("date", "`t`.`id`");
		
		ArrayList<String> possible_sortOrder = new ArrayList<String>();
		possible_sortOrder.add("asc");
		possible_sortOrder.add("desc");
		
	searchTerm = searchTerm == null || searchTerm.equalsIgnoreCase("") ? default_searchTerm : searchTerm;
	count = count == null || count > max_count ? default_count : count;
	startIndex = startIndex == null ? default_startIndex : startIndex;
	sort = sort == null || !possible_sort.containsKey(sort) ? default_sort : sort;
	sortOrder = sortOrder == null || !possible_sortOrder.contains(sortOrder) ? default_sortOrder : sortOrder;
	
	ObjectNode rootNode = Json.newObject();

	DatabaseConnection db;
		try {

			db = new DatabaseConnection(DB.getDataSource("charticle"));
			ArrayList<ArrayList<Object>> resultSet;
			
			String queryTag  = "";
			String[] searchTerms = {};
			
			if (searchTerm != null) {
				searchTerms = searchTerm.split("\\s+");
				 
				String queryTagLike = "`tt`.`tag` like ?";
				searchTerms[0] = "%" + searchTerms[0] + "%";
				for(int i = 1; i < searchTerms.length; i++) {
					queryTagLike += " or `tt`.`tag` like ?";
					searchTerms[i] = "%" + searchTerms[i] + "%";
				}
				
				queryTag = " where exists (" +
					"	select 1" +
					"	from `template_tag` `tt`" +
					"	where `tt`.`template_id` = `x`.`id`" +
					"		and ("+queryTagLike+")" +
					" )";
			}

			resultSet = db.executeQuery(
					DatabaseConnection.SELECT,
				" select `t`.`application_id`, `t`.`name`, `t`.`description`, `t`.`thumbnail`, `x`.`id`" +
				" from (" +
				"		select max(`tp`.`template_id`) as `id`" +
				"		from `template_publication` `tp`" +
				"			inner join `publication` `pub` on (`pub`.`id` = `tp`.`publication_id`)" +
				"			inner join `template` `tx` on (`tx`.`id` = `tp`.`template_id`)" +
				"			inner join `user_template` `ut` on (`ut`.`template_id` = `tp`.`template_id`)" +
				"		where `pub`.`type` = 'Public' or `ut`.`user_id` = ?" +
				"		group by `tx`.`application_id`" +
				"	) `x`" +
				"	inner join `template` `t` on (`t`.`id` = `x`.`id`)" +
				"	left join (" +
				"		select `ut`.`template_id`, count(distinct `user_id`) as `count`" +
				"		from `user_template` `ut`" +
				"	) `u` on (`u`.`template_id` = `t`.`application_id`)" +
				queryTag +
				" order by " + possible_sort.get(sort) + " " + sortOrder +
				" limit ?,?;", user.getId(), (Object[]) searchTerms, startIndex, count);
			
			ArrayNode itemsNode = rootNode.putArray("items");
			
			Iterator<ArrayList<Object>> iResult = resultSet.iterator();
			
			iResult.next(); //Remove headers from resultset
			while(iResult.hasNext()) {
				ArrayList<Object> Row = iResult.next();
				ObjectNode itemNode = itemsNode.addObject();
				
				itemNode.put("id", (Integer) Row.get(0));
				itemNode.put("name", (String) Row.get(1));
				itemNode.put("description", (String) Row.get(2));
				itemNode.put("thumbnail", (String) Row.get(3));
				itemNode.put("publicationId", (Integer) Row.get(4));
			}
			
			return ok(rootNode);
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError();
		}
	}
}
