package service

import securesocial.controllers.TemplatesPlugin

import play.api.mvc.{RequestHeader, Request}
import play.api.templates.{Html, Txt}
import play.api.{Logger, Plugin, Application}
import securesocial.core.{Identity, SecuredRequest, SocialUser}
import play.api.data.Form
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo

class SecureSocialViews(application: play.Application) extends TemplatesPlugin 
{
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
                               msg: Option[String] = None): Html =
  {
    views.html.secure_social.login(request, form, msg)
  }

  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    views.html.secure_social.Registration.signUp(request,form, token)
  }

  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.secure_social.Registration.startSignUp(request,form)
  }

  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.secure_social.Registration.startResetPassword(request,form)
  }

  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    views.html.secure_social.Registration.resetPasswordPage(request,form, token)
  }

  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]):Html = {
    views.html.secure_social.passwordChange(request,form)
  }

  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    views.html.secure_social.notAuthorized()
  }

  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.secure_social.mails.signUpEmail(request,token)))
  }

  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.secure_social.mails.alreadyRegisteredEmail(request,user)))
  }

  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.secure_social.mails.welcomeEmail(request,user)))
  }

  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.secure_social.mails.unknownEmailNotice(request)))
  }

  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.secure_social.mails.passwordResetEmail(request,user, token)))
  }

  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.secure_social.mails.passwordChangedNotice(request,user)))
  }
}