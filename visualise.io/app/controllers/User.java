package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import play.db.*;
import commons.database.DatabaseConnection;

import models.Entity;
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
		
	   	List<Entity> entities = new ArrayList<Entity>();
   	
		DatabaseConnection db;
		try {
			ArrayList<ArrayList<Object>> resultSet;

			db = new DatabaseConnection(DB.getDataSource("charticle"));
			resultSet = db.executeQuery(
			            DatabaseConnection.SELECT,
			            "select x.type, x.id, x.name, x.description, x.thumbnail from (select 'visualisation' as `type`, " +
			            "   `v`.`id`, " +
			            "   `v`.`name`, " +
			            "   `v`.`description`, " +
			            "   `v`.`thumbnail`, " +
			            "	`v`.`updateDate` " +
			            "from `user_hasAuthorisation_visualisation` `uav` " +
			            "	inner join `visualisation` `v` on (`v`.`id` = `uav`.`visualisation_id`) " +
			            "where `uav`.`user_id` = ? " +
			            " " +
			            "union all " +
			            " " +
			            "select 'template' as `type`, " +
			            "   `t`.`application_id`, " +
			            "   `t`.`name`, " +
			            "   `t`.`description`, " +
			            "   `t`.`thumbnail`, " +
			            "	`t`.`updateDate` " +
			            "from `user_template` `ut` " +
			            "	left outer join ( " +
			            "		select max(`t`.`id`) as id, `t`.`application_id` " +
			            "		from `template` `t` " +
			            "		group by `t`.`application_id` " +
			            "		) m on (`m`.`application_id` = `ut`.`template_id`) " +
			            "	left outer join `template` `t` on (`t`.`id` = `m`.`id`) " +
			            "where `ut`.`user_id` = ? ) x " +
			            "order by x.updateDate desc;",
			            userId, userId);
			
			
			if (resultSet.size() > 1) {
				Iterator<ArrayList<Object>> i = resultSet.iterator();
				i.next(); // remove headers from resultset
				while (i.hasNext()) {
					ArrayList<Object> r = i.next();
					Entity entity = new Entity((Integer) r.get(1));
					entity.name = (String) r.get(2);
					entity.description = (String) r.get(3);
					entity.thumbnail = (String) r.get(4);
					entity.instance = (String) r.get(0);
					entities.add(entity);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError();
		}
		
		return ok(views.html.user.overview.render(user, userId, entities));
		
		
	}
	
	public static Result getTemplates(Integer userId) {
		return null;
	}
	
	public static Result getVisualisations(Integer userId) {
		return null;
	}
}
