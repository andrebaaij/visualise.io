package models;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import commons.database.DatabaseConnection;
import play.db.*;

public class Visualisation extends Entity{
	public Integer id;
	
	public String name,
		description,
		type,
		definition,
		data,
		thumbnail,
		instanceType;
	
	public Template template;
	
	public Visualisation(String name, String description, int templateId) {
		this.name = name;
		this.description = description;
		this.template = new Template(templateId);
	}

	public Visualisation(Integer id) {
		super(id);
	}
	public Boolean isAuthorized(Integer userId) {

		try {
			ArrayList<ArrayList<Object>> resultSet;
			
			DatabaseConnection db = new DatabaseConnection(DB.getDataSource("charticle"));
			resultSet = db.executeQuery(
					DatabaseConnection.SELECT,
			      "select fnIsUserAuthorizedForVisualisation(?,?)",
			      userId, this.id);
			if (resultSet.size() > 1) {
				return (Boolean) resultSet.get(1).get(0);
			}
			else {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	

	public boolean isPublic() {
		DatabaseConnection db;
		try {
			ArrayList<ArrayList<Object>> resultSet;

			db = new DatabaseConnection(DB.getDataSource("charticle"));
			resultSet = db.executeQuery(
					DatabaseConnection.SELECT,
			      "select spIsReportPublic(?)",
			      this.id);
			if (resultSet.size() > 1) {
				return (Boolean) resultSet.get(1).get(0);
			}
			else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
   }
	
	/**
    * @return the jSONString
    */
   public String getDecodedDefinition() {
   	try {
	      return URLDecoder.decode(this.definition,"UTF-8");
	      
      } catch (UnsupportedEncodingException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	      return this.definition;
      }
   }
   
	/**
    * @param definition the jSONString to set
    */
   public void setEncodedDefinition(String definition) {
   	try {
	      this.definition = URLEncoder.encode(definition,"UTF-8");
      } catch (UnsupportedEncodingException e) {
      	this.definition = definition;
      	// TODO Auto-generated catch block
	      e.printStackTrace();
      }
   }

   public boolean dbSelect(Integer userId) {
		if (isAuthorized(userId)) {
			
		   // TODO Auto-generated method stub
			DatabaseConnection db;
			try {
				ArrayList<ArrayList<Object>> resultSet;
	
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				resultSet = db.executeQuery(
						DatabaseConnection.SELECT,
				            "   	  select `v`.`name`, `v`.`description`, `v`.`JSON`, `v`.`data`, t.id, t.name, t.description, t.definition" +
				            " from `visualisation` `v` " +
				            "   inner join visualisation_template vt on (vt.visualisation_id = v.id)" +
				            "   inner join `template` t on (t.id= vt.template_id)" +
				            "where `v`.`id` = ?;",
				            this.id);
				
				if (resultSet.size() > 1) {
					int j = 1;
					Iterator<ArrayList<Object>> i = resultSet.iterator();
					i.next(); // remove headers from resultset
					
					ArrayList<Object> r = i.next();
					this.name = (String) r.get(0);
					this.description = (String) r.get(1);
					this.definition = (String) r.get(2);
					this.data = (String) r.get(3);
					
					this.template = new models.Template((Integer) r.get(4));
					this.template.name = (String) r.get(5);
					this.template.description = (String) r.get(6);
					this.template.definition = (String) r.get(7);
					
					return true;
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
   }

	public boolean dbUpdate(Integer userId) {
		if (isAuthorized(userId)) {

			DatabaseConnection db;
			try {
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				db.executeQuery(
						DatabaseConnection.UPDATE,
				            "update `visualisation`" +
				            "set `name` = ?," +
				            "   `description` = ?," +
				            "   `JSON` = ?," +
				            "   `data` = ?," +
				            "   `thumbnail` = ? " +
				            "where `id` = ?;",
				            this.name, this.description,this.definition, this.data, this.thumbnail, this.id);
				
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}


	public boolean dbInsert(Integer userId, Integer templateId) {
	   // TODO Auto-generated method stub
		DatabaseConnection db;
		try {
			if(this.template.isPublished(userId)) {
				this.template.dbSelect(userId);
				
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				db.executeQuery(
						DatabaseConnection.INSERT,
				            "insert into `visualisation` (`name`, `description`, `thumbnail`)" +
				            "select ?,?,?;", this.name, this.description, this.template.thumbnail);
				this.id = db.getGeneratedKey();
				this.definition = "{\"id\":\"" + this.id.toString() + "\"}";
				
				db.executeQuery(
						DatabaseConnection.INSERT,
		            "insert into `user_hasAuthorisation_visualisation` (`user_id`, `visualisation_id`, `authorisationRole_id`)" +
		            "select ?, ?, 1;", userId, this.id);
	
				db.executeQuery(
						DatabaseConnection.INSERT,
		            "insert into `visualisation_template` (`visualisation_id`, `template_id`)" +
		            "select ?, ?", this.id, templateId);
				
				db.executeQuery(
						DatabaseConnection.UPDATE,
		            "update `visualisation` " +
		            "set `JSON` = ?" +
		            "where id = ?;",this.definition, this.id);
				this.id = db.getGeneratedKey();
				return true;
			}
			else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean dbDelete(Integer userId) {
	   // TODO Auto-generated method stub
		DatabaseConnection db;
		try {
			db = new DatabaseConnection(DB.getDataSource("charticle"));
			db.executeQuery(
					DatabaseConnection.DELETE,
	            "delete from `user_hasAuthorisation_visualisation` " +
	            "where `visualisation_id` = ?;", this.id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean dbCopy(Integer userId) {
		DatabaseConnection db;
		try {

			db = new DatabaseConnection(DB.getDataSource("charticle"));
			db.executeQuery(
					DatabaseConnection.INSERT,
			            " insert into `visualisation` (`parent_id`, `name`, `description`, `type`, `JSON`, `Data`, `tempData`)" +
			            " select `v`.`id`, `v`.`name`, `v`.`description`, `v`.`type`, `v`.`JSON`, `v`.`Data`, `v`.`Data`" +
			            " from `visualisation` `v`" +
			            "    inner join `visualisation_publication` `vp` on (`vp`.`visualisation_id` = `v`.`id`)" +
			            "    inner join `publication` `p` on (`p`.`id` = `vp`.`publication_id` and `p`.`type` = 'public')" +
			            " where `v`.`id` = ?;", this.id);
			this.id = db.getGeneratedKey();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
   }
}
