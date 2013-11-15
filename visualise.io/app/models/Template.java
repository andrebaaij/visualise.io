package models;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import play.db.*;
import commons.database.DatabaseConnection;

public class Template extends Entity {

	public int version = 0;
	
	public Integer id,
		applicationId;
		
	public String Name,
		Description,
		definition,
		thumbnail,
		tags,
		instanceType;

	public Template () {

	}
	
	public Template (Integer id) {
		this.applicationId = id;
	}

	public Template(String name, String description) {
	   this.name = name;
	   this.description = description;
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
    * @param definition the jSONString to 
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

   public Boolean userIsAuthorized(Integer userId) {
	   if (this.applicationId == null) {
			return false;
		}
		DatabaseConnection db;
		try {
			ArrayList<ArrayList<Object>> result;
			
			db = new DatabaseConnection(DB.getDataSource("charticle"));
			result = db.executeQuery(
			      DatabaseConnection.SELECT,
			      "select fnIsUserAuthorizedForTemplate(?,?);",
			      userId, this.applicationId);
			if (result.size() > 1) {
				return (Boolean) result.get(1).get(0);
			}
			else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean dbSelect(Integer userId) {
		if (userIsAuthorized(userId)) {
			
		   // TODO Auto-generated method stub
			DatabaseConnection db;
			try {
				ArrayList<ArrayList<Object>> result;
	
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				if (this.applicationId != null) {
					result = db.executeQuery(
					            DatabaseConnection.SELECT,
					            "   	  select `t`.`id`, `t`.`application_id`, `t`.`version`, `t`.`name`, `t`.`description`, `t`.`definition`, `t`.`thumbnail`, `t`.`tags`" +
					            "from `template` `t` " +
					            "where `t`.`application_id` = ? " +
					            "order by `t`.`id` desc " +
					            "limit 0,1;",
					            this.applicationId);
				}
				else if (this.id != null) {
					result = db.executeQuery(
			            DatabaseConnection.SELECT,
			            "   	  select `t`.`id`, `t`.`application_id`, `t`.`version`, `t`.`name`, `t`.`description`, `t`.`definition`, `t`.`thumbnail`, `t`.`tags`" +
			            "from `template` `t` " +
			            "where `t`.`id` = ? " +
			            "order by `t`.`id` desc " +
			            "limit 0,1;",
			            this.id);
				}
				else {
					return false;
				}
				
				if (result.size() > 1) {
					int j = 1;
					Iterator<ArrayList<Object>> i = result.iterator();
					i.next(); // remove headers from result
					
					ArrayList<Object> r = i.next();
					this.id = (Integer) r.get(0);
					this.applicationId = (Integer) r.get(1);
					this.version = (Integer) r.get(2);
					this.name = (String) r.get(3);
					this.description = (String) r.get(4);
					this.definition = (String) r.get(5);
					this.thumbnail = (String) r.get(6);
					this.tags = (String) r.get(7);
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
		if (userIsAuthorized(userId)) {
			
			this.version += 1;
			
		   // TODO Auto-generated method stub
			DatabaseConnection db;
			try {

				System.out.println(this.tags);
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				db.executeQuery(
				            DatabaseConnection.INSERT,
				            "insert into `template` (`application_id`, `version`,`name`, `description`,`definition`,`thumbnail`, `tags`)" +
				            "select ?,?,?,?,?,?,?;", this.applicationId, this.version, this.name, this.description, this.definition, this.thumbnail, this.tags);
				this.id = db.getGeneratedKey();
				
				String[] arrTags = {};
				if(this.tags != null) {
					arrTags = this.tags.split(",");
				}
				
				if (arrTags.length > 0) {
					String query = "insert into `template_tag` (`template_id`, `tag`) select " + this.id.toString() + ", ?";
					for(int i = 1; i < arrTags.length; i++) {
						query += " union all select " + this.id.toString() + ", ?";
					}
					
					db.executeQuery(DatabaseConnection.INSERT, query, (Object[]) arrTags);
				}
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


	public boolean dbInsert(Integer userId) {
	   // TODO Auto-generated method stub
		DatabaseConnection db;
		try {
			this.version = 1;
			
			db = new DatabaseConnection(DB.getDataSource("charticle"));
			db.executeQuery(
			            DatabaseConnection.INSERT,
			            "insert into `template` (`version`, `name`, `description`)" +
			            "select 1,?,?;", this.name, this.description);
			this.id = db.getGeneratedKey();
			this.definition = "{\"id\":\"" + this.id.toString() + "\"}";
			
			db.executeQuery(
	            DatabaseConnection.INSERT,
	            "insert into `user_template` (`user_id`, `template_id`)" +
	            "select ?, ?;", userId, this.id);

			db.executeQuery(
	            DatabaseConnection.UPDATE,
	            "update `template` " +
	            " `application_id` = ?, `definition` = ?" +
	            "where id = ?;", this.id, this.definition, this.id);
			this.id = db.getGeneratedKey();
			
			this.applicationId = this.id;
			
			return true;
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
	            "delete from `user_template` " +
	            "where `template_id` = ?;", this.applicationId);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean dbPublish(Integer userId, int publicationTypeId) {
		if (userIsAuthorized(userId)) {
			DatabaseConnection db;
			try {
				db = new DatabaseConnection(DB.getDataSource("charticle"));
//				db.executeQuery(DatabaseConnection.DELETE, "delete from `template_publication`" +
//						"where exists (" +
//						"	select *" +
//						"	from `template` `t`" +
//						"		where `t`.`application_id` = ?" +
//						"			and `t`.`id` = `template_publication`.`template_id`" +
//						")", this.application_id);
//				
				db.executeQuery(
				            DatabaseConnection.INSERT,
				            "insert into `template_publication` (`template_id`, `publication_id`)" +
				            "select ?,?;", this.id, publicationTypeId);
				
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

	public boolean isPublished(Integer userId) {
		if (this.applicationId == null) {
			return false;
		}
		DatabaseConnection db;
		try {
			ArrayList<ArrayList<Object>> result;
			
			db = new DatabaseConnection(DB.getDataSource("charticle"));
			result = db.executeQuery(
			      DatabaseConnection.SELECT,
			      "select fnIsTemplatePublishedForUser(?,?);",
			      this.applicationId, userId);
			if (result.size() > 1) {
				this.id = (Integer) result.get(1).get(0);
				return true;
			}
			else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
   }
}
