package controllers;

import com.fasterxml.jackson.databind.JsonNode;

import models.UserIdentity;
import models.Visualisation;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import play.api.mvc.Request;
import play.api.mvc.RequestHeader;
import scala.Option;
import securesocial.core.java.SecureSocial;
import securesocial.core.providers.UsernamePasswordProvider;
import play.api.data.Form;

public class Template extends Controller{
	
	 
	@SecureSocial.SecuredAction
	public static Result create() {
   	UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);
   	DynamicForm requestData = DynamicForm.form().bindFromRequest();
   	models.Template template = new models.Template(
   				requestData.get("templateName"),
   				requestData.get("templateDescription"));
   	boolean result = template.dbInsert(user.getId());
   	
   	if(result) return redirect(controllers.routes.Template.edit(template.id));
   	else return internalServerError();
	}
	
	@SecureSocial.SecuredAction
	public static Result edit(Integer templateId) {
		UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);
		models.Template template = new models.Template(templateId);
		boolean result = template.dbSelect(user.getId());
		if(result) return ok(views.html.template.edit.render(user, template, play.Configuration.root().getString("charticle.sandbox.url")));
		else return forbidden();
	}
	
	@SecureSocial.SecuredAction
	public static Result update(Integer templateId){
   	UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);

		com.fasterxml.jackson.databind.JsonNode json = request().body().asJson();

		models.Template template = new models.Template(templateId);
	   	boolean result = template.dbSelect(user.getId());
	   	template.name = json.findPath("name").textValue();
	   	template.description = json.findPath("description").textValue();
	   	template.setEncodedDefinition(json.findPath("definition").toString());
	   	template.thumbnail = json.findPath("thumbnail").textValue();
	   	template.tags = json.findPath("tags").textValue();
	   	if(result) {
	   		result = template.dbUpdate(user.getId());
	   		if(result) return ok();
	   		else return forbidden();
	   	}
	   	else return forbidden();
	}
	
	@SecureSocial.SecuredAction
	public static Result publish(Integer templateId){
		UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);
		JsonNode json = request().body().asJson();
		int publicationTypeId = json.findPath("type").asInt();

	   	models.Template template = new models.Template(templateId);
	   	boolean result = template.dbSelect(user.getId());
	   	template.name = json.findPath("name").textValue();
	   	template.description = json.findPath("description").textValue();
	   	template.setEncodedDefinition(json.findPath("definition").toString());
	   	template.thumbnail = json.findPath("thumbnail").textValue();
	   	template.tags = json.findPath("tags").textValue();
	   	if(result) result = template.dbUpdate(user.getId());
	   	if(result) result = template.dbPublish(user.getId(), publicationTypeId);
	   	if(result) return ok();
	   	else return forbidden();
	}
	
	@SecureSocial.SecuredAction
	public static Result delete(Integer templateId) {
		UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);
		models.Template template = new models.Template(templateId);
		boolean result = template.dbDelete(user.getId());
		if(result) return ok();
	   	else return forbidden();
	}

}
