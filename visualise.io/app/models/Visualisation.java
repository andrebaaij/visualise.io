package models;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import commons.database.DatabaseConnection;
import play.db.*;

public class Visualisation {
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
		setName(name);
		setDescription(description);
		setTemplate(new Template(templateId));
	}
	
	public Visualisation(int id) {
		setId(id);
	}

	public Boolean isAuthorized(Integer userId) {

		try {
			ArrayList<ArrayList<Object>> resultSet;
			
			DatabaseConnection db = new DatabaseConnection(DB.getDataSource("charticle"));
			resultSet = db.executeQuery(
					DatabaseConnection.SELECT,
			      "select fnIsUserAuthorizedForVisualisation(?,?)",
			      userId, getId());
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
			      getId());
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
    * @return the name
    */
   public String getName() {
   	return name;
   }

	/**
    * @param name the name to set
    */
   public void setName(String name) {
   	this.name = name;
   }

	/**
    * @return the description
    */
   public String getDescription() {
   	return description;
   }

	/**
    * @param description the description to set
    */
   public void setDescription(String description) {
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
    * @return the type
    */
   public String getType() {
   	return type;
   }

	/**
    * @param type the type to set
    */
   public void setType(String type) {
   	this.type = type;
   }

	/**
    * @return the jSONString
    */
   public String getDefinition() {
      return definition;
   }

	/**
    * @param jSONString the jSONString to set
    */
   public void setDefinition(String definition) {
   	this.definition = definition;
   }

	/**
    * @return the data
    */
   public String getData() {
   	return data;
   }

	/**
    * @param data the data to set
    */
   public void setData(String Data) {
   	this.data = Data;
   }

	/**
    * @return the template
    */
   public Template getTemplate() {
   	return template;
   }

	/**
    * @param template the template to set
    */
   public void setTemplate(Template template) {
   	this.template = template;
   }

	/**
    * @return the image
    */
   public String getThumbnail() {
   	return thumbnail;
   }

	/**
    * @param image the image to set
    */
   public void setThumbnail(String thumbnail) {
   	this.thumbnail = thumbnail;
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
				            getId());
				
				if (resultSet.size() > 1) {
					int j = 1;
					Iterator<ArrayList<Object>> i = resultSet.iterator();
					i.next(); // remove headers from resultset
					
					ArrayList<Object> r = i.next();
					setName((String) r.get(0));
					setDescription((String) r.get(1));
					setDefinition((String) r.get(2));
					setData((String) r.get(3));
					
					setTemplate( new models.Template((Integer) r.get(4)));
					getTemplate().setName((String) r.get(5));
					getTemplate().setDescription((String) r.get(6));
					getTemplate().setDefinition((String) r.get(7));
					
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
				            getName(), getDescription(), getDefinition(), getData(),getThumbnail(), getId());
				
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
			if(getTemplate().isPublished(userId)) {
				getTemplate().dbSelect(userId);
				
				db = new DatabaseConnection(DB.getDataSource("charticle"));
				db.executeQuery(
						DatabaseConnection.INSERT,
				            "insert into `visualisation` (`name`, `description`, `thumbnail`)" +
				            "select ?,?,?;", getName(), getDescription(), getTemplate().getThumbnail());
				setId(db.getGeneratedKey());
				setDefinition("{\"id\":\"" + getId().toString() + "\"}");
				
				db.executeQuery(
						DatabaseConnection.INSERT,
		            "insert into `user_hasAuthorisation_visualisation` (`user_id`, `visualisation_id`, `authorisationRole_id`)" +
		            "select ?, ?, 1;", userId, getId());
	
				db.executeQuery(
						DatabaseConnection.INSERT,
		            "insert into `visualisation_template` (`visualisation_id`, `template_id`)" +
		            "select ?, ?", getId(), templateId);
				
				db.executeQuery(
						DatabaseConnection.UPDATE,
		            "update `visualisation` " +
		            "set `JSON` = ?" +
		            "where id = ?;", getDefinition(), getId());
				setId(db.getGeneratedKey());
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
	            "where `visualisation_id` = ?;", getId());
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
			            " where `v`.`id` = ?;", getId());
			setId(db.getGeneratedKey());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
   }
}
