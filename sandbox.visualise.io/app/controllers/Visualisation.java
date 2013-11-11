package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Visualisation extends Controller {
  
    public static Result edit() {
   	 return ok(views.html.visualisation.edit.render());
    }
  
}
