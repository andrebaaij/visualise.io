package controllers;

import models.UserIdentity;
import com.fasterxml.jackson.databind.JsonNode;

import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;
import com.repaqt.commons.convertor.*;


public class Visualisation extends Controller {
    @SecureSocial.SecuredAction
    public static Result create() {
        UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);

        DynamicForm requestData = DynamicForm.form().bindFromRequest();
        models.Visualisation visualisation = new models.Visualisation(
            requestData.get("visualisationName"),
            requestData.get("visualisationDescription"),
            Integer.parseInt(requestData.get("templateId")));

        boolean result = visualisation.dbInsert(user.getId(), Integer.parseInt(requestData.get("templatePublicationId")));

        if(result) return redirect(controllers.routes.Visualisation.edit(visualisation.getId()));
        else return internalServerError();
    }

    @SecureSocial.SecuredAction
    public static Result delete(int visualisationid) {
        UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);

        models.Visualisation visualisation = new models.Visualisation(visualisationid);

        boolean result = visualisation.dbDelete(user.getId());

        if(result) return ok();
        else return forbidden();
    }

    @SecureSocial.SecuredAction
    public static Result edit(int visualisationId) {
        UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);

        models.Visualisation visualisation = new models.Visualisation(visualisationId);

        boolean result = visualisation.dbSelect(user.getId());
        if(result) return ok(views.html.visualisation.edit.render(visualisationId, user, visualisation,play.Configuration.root().getString("charticle.sandbox.url")));
        else return forbidden();
    }

    @SecureSocial.SecuredAction
    public static Result update(int visualisationId) {
        UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);

        JsonNode json = request().body().asJson();

        models.Visualisation visualisation = new models.Visualisation(visualisationId);

        visualisation.setName(json.findPath("name").textValue());
        visualisation.setDescription(json.findPath("description").textValue());
        visualisation.setThumbnail(json.findPath("thumbnail").textValue());
        visualisation.setEncodedDefinition(json.findPath("definition").toString());

        boolean result = visualisation.dbUpdate(user.getId());
        if(result) return ok();
        else return forbidden();
    }
}