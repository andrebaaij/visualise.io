package controllers;

import models.UserIdentity;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;
import views.html.index;

import com.repaqt.commons.Database;

public class Application extends Controller {
	
	@SecureSocial.UserAwareAction
    public static Result index() {

		UserIdentity user = (UserIdentity) ctx().args.get(SecureSocial.USER_KEY);
      return ok(index.render(user));
    }
}
