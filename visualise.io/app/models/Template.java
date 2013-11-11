package models;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import play.db.*;
import commons.database.DatabaseConnection;

public class Template {

	public Integer id,
		application_id,
		version;
	
	public String Name,
		Description,
		definition,
		thumbnail,
		tags;

	/**
    * @return the tags
    */
   public String getTags() {
   	return tags;
   }

	/**
    * @param tags the tags to set
    */
   public void setTags(String tags) {
   	this.tags = tags;
   }

	public Template () {
		
	}
	
	public Template (Integer id) {
		setApplication_id(id);
	}
	
	/**
    * @return the thumbnail
    */
   public String getThumbnail() {
   	return thumbnail;
   }

	/**
    * @param thumbnail the thumbnail to set
    */
   public void setThumbnail(String thumbnail) {
   	this.thumbnail = thumbnail;
   }

	public Template(String name, String description) {
	   setName(name);
	   setDescription(description);
   }

	/**
    * @return the id
    */
   public Integer getId() {
   	return id;
   }

	/**
    * @param id the id to set
    */
   public void setId(Integer id) {
   	this.id = id;
   }

	/**
    * @return the parent_id
    */
   public Integer getApplication_id() {
   	return application_id;
   }

	/**
    * @param parent_id the parent_id to set
    */
   public void setApplication_id(Integer parent_id) {
   	this.application_id = parent_id;
   }

	/**
    * @return the version
    */
   public Integer getVersion() {
   	if (version == null) version = 0;
   	return version;
   }

	/**
    * @param version the version to set
    */
   public void setVersion(Integer version) {
   	this.version = version;
   }

	/**
    * @return the name
    */
   public String getName() {
   	return Name;
   }

	/**
    * @param name the name to set
    */
   public void setName(String name) {
   	Name = name;
   }

	/**
    * @return the description
    */
   public String getDescription() {
   	return Description;
   }

	/**
    * @param description the description to set
    */
   public void setDescription(String description) {
   	Description = description;
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
    * @return the jSONString
    */
   public String getDefinition() {
   	return this.definition;
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

   /**
    * @param definition the jSONString to set
    */
   public void setDefinition(String definition) {
     this.definition = definition;
   }
   
	public Boolean userIsAuthorized(Integer userId) {

		
		if (getApplication_id() == null) {
			return false;
		}
		DatabaseConnection db;
		try {
			ArrayList<ArrayList<Object>> resultSet;
			
			db = new DatabaseConnection(DB.getDataSource("charticle"));
			resultSet = db.executeQuery(
			      DatabaseConnection.SELECT,
			      "select fnIsUserAuthorizedForTemplate(?,?);",
			      userId, getApplication_id());
			if (resultSet.size() > 1) {
				return (Boolean) resultSet.get(1).get(0);
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
				ArrayList<ArrayList<Object>> resultSet;
	
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				if (getApplication_id() != null) {
					resultSet = db.executeQuery(
					            DatabaseConnection.SELECT,
					            "   	  select `t`.`id`, `t`.`application_id`, `t`.`version`, `t`.`name`, `t`.`description`, `t`.`definition`, `t`.`thumbnail`, `t`.`tags`" +
					            "from `template` `t` " +
					            "where `t`.`application_id` = ? " +
					            "order by `t`.`id` desc " +
					            "limit 0,1;",
					            getApplication_id());
				}
				else if (getId() != null) {
					resultSet = db.executeQuery(
			            DatabaseConnection.SELECT,
			            "   	  select `t`.`id`, `t`.`application_id`, `t`.`version`, `t`.`name`, `t`.`description`, `t`.`definition`, `t`.`thumbnail`, `t`.`tags`" +
			            "from `template` `t` " +
			            "where `t`.`id` = ? " +
			            "order by `t`.`id` desc " +
			            "limit 0,1;",
			            getId());
				}
				else {
					return false;
				}
				
				if (resultSet.size() > 1) {
					int j = 1;
					Iterator<ArrayList<Object>> i = resultSet.iterator();
					i.next(); // remove headers from resultset
					
					ArrayList<Object> r = i.next();
					setId((Integer) r.get(0));
					setApplication_id((Integer) r.get(1));
					setVersion((Integer) r.get(2));
					setName((String) r.get(3));
					setDescription((String) r.get(4));
					setDefinition((String) r.get(5));
					setThumbnail((String) r.get(6));
					setTags((String) r.get(7));
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
			
			setVersion(getVersion() + 1);
			
		   // TODO Auto-generated method stub
			DatabaseConnection db;
			try {

				System.out.println(getTags());
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				db.executeQuery(
				            DatabaseConnection.INSERT,
				            "insert into `template` (`application_id`, `version`,`name`, `description`,`definition`,`thumbnail`, `tags`)" +
				            "select ?,?,?,?,?,?,?;", getApplication_id(), getVersion(), getName(), getDescription(), getDefinition(), getThumbnail(), getTags());
				setId(db.getGeneratedKey());
				
				String[] Tags = {};
				if(getTags() != null) {
					Tags = getTags().split(",");
				}
				
				if (Tags.length > 0) {
					String query = "insert into `template_tag` (`template_id`, `tag`) select " + getId().toString() + ", ?";
					for(int i = 1; i < Tags.length; i++) {
						query += " union all select " + getId().toString() + ", ?";
					}
					
					db.executeQuery(DatabaseConnection.INSERT, query, (Object[]) Tags);
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
			setVersion(1);
			
			db = new DatabaseConnection(DB.getDataSource("charticle"));
			db.executeQuery(
			            DatabaseConnection.INSERT,
			            "insert into `template` (`version`, `name`, `description`)" +
			            "select 1,?,?;", getName(), getDescription());
			setId(db.getGeneratedKey());
			setDefinition("{\"id\":\"" + getId().toString() + "\"}");
			
			db.executeQuery(
	            DatabaseConnection.INSERT,
	            "insert into `user_template` (`user_id`, `template_id`)" +
	            "select ?, ?;", userId, getId());

			db.executeQuery(
	            DatabaseConnection.UPDATE,
	            "update `template` " +
	            "set `application_id` = ?, `definition` = ?" +
	            "where id = ?;", getId(), getDefinition(), getId());
			setId(db.getGeneratedKey());
			
			setApplication_id(getId());
			
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
	            "where `template_id` = ?;", getApplication_id());
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
//						")", getApplication_id());
//				
				db.executeQuery(
				            DatabaseConnection.INSERT,
				            "insert into `template_publication` (`template_id`, `publication_id`)" +
				            "select ?,?;", getId(), publicationTypeId);
				
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
		if (getApplication_id() == null) {
			return false;
		}
		DatabaseConnection db;
		try {
			ArrayList<ArrayList<Object>> resultSet;
			
			db = new DatabaseConnection(DB.getDataSource("charticle"));
			resultSet = db.executeQuery(
			      DatabaseConnection.SELECT,
			      "select fnIsTemplatePublishedForUser(?,?);",
			      getApplication_id(), userId);
			if (resultSet.size() > 1) {
				setId((Integer) resultSet.get(1).get(0));
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
