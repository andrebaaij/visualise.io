package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import play.db.*;
import commons.database.DatabaseConnection;

import models.UserIdentity;
import models.Visualisation;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.*;
import scala.collection.JavaConverters.*;

public class User extends Controller{
	
	@SecureSocial.SecuredAction
	public static Result getOverview(Integer userId) {
		UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);
		
	   	List<Visualisation> objects = new ArrayList<Visualisation>();
   	
		DatabaseConnection db;
		try {
			ArrayList<ArrayList<Object>> resultSet;

			db = new DatabaseConnection(DB.getDataSource("charticle"));
			resultSet = db.executeQuery(
			            DatabaseConnection.SELECT,
			            "   	  select `v`.`id`, `v`.`name`, `v`.`description`, `v`.`thumbnail` from `user_hasAuthorisation_visualisation` `uav`" +
			            "	inner join `visualisation` `v` on (`v`.`id` = `uav`.`visualisation_id`)" +
			            "where `uav`.`user_id` = ? " +
			            "order by `v`.`id`;",
			            userId);

			if (resultSet.size() > 1) {
				int j = 1;
				Iterator<ArrayList<Object>> i = resultSet.iterator();
				i.next(); // remove headers from resultset
				while (i.hasNext()) {
					ArrayList<Object> r = i.next();
					Visualisation v = new Visualisation((Integer) r.get(0));
					v.setName((String) r.get(1));
					v.setDescription((String) r.get(2));
					v.setThumbnail((String) r.get(3));
					v.instanceType = "visualisation";
					objects.add(v);
					j++;
				}
			}
			
			resultSet = db.executeQuery(
	            DatabaseConnection.SELECT,
	            "select `t`.`application_id`, `t`.`name`, `t`.`description`, `t`.`thumbnail`" +
	            "from `user_template` `ut`" +
	            "	left outer join (" +
	            "		select max(`t`.`id`) as id, `t`.`application_id`" +
	            "		from `template` `t`" +
	            "		group by `t`.`application_id`" +
	            "		) m on (`m`.`application_id` = `ut`.`template_id`)" +
	            "	left outer join `template` `t` on (`t`.`id` = `m`.`id`)" +
	            "where `ut`.`user_id` = ? " +
	            "order by `t`.`id`;",
	            userId);
		
			if (resultSet.size() > 1) {
				int j = 1;
				Iterator<ArrayList<Object>> i = resultSet.iterator();
				i.next(); // remove headers from resultset
				while (i.hasNext()) {
					ArrayList<Object> r = i.next();
					models.Visualisation v = new models.Visualisation((Integer) r.get(0));
					v.setName((String) r.get(1));
					v.setDescription((String) r.get(2));
					v.setThumbnail((String) r.get(3));
					v.instanceType = "template";
					objects.add(v);
					j++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError();
		}
		
		return ok(views.html.user.overview.render(user, userId, objects));
		
		
	}
	
	public static Result getTemplates(Integer userId) {
		return null;
	}
	
	public static Result getVisualisations(Integer userId) {
		return null;
	}
}
