package controllers;

import models.UserIdentity;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

public class Documentation extends Controller {
   public static Result index() {
      return redirect(controllers.routes.Documentation.visualisation()); //
    }
	
    @SecureSocial.UserAwareAction
    public static Result visualisation() {
      UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);
      return ok(views.html.help.visualisation.render(user)); //
    }

     @SecureSocial.UserAwareAction
    public static Result template() {
      UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);
      return ok(views.html.help.template.render(user)); //
    }
}
