/**
 *
 */
package controllers.usermanagement;

import akka.util.Timeout;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.BaseController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.sunbird.common.models.util.ActorOperations;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.request.ExecutionContext;
import org.sunbird.common.request.HeaderParam;
import org.sunbird.common.request.Request;
import org.sunbird.common.request.RequestValidator;

import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Result;

/**
 * This controller will handle all the request and responses for user management.
 *
 * @author Manzarul
 */
public class UserController extends BaseController {

  /**
   * This method will do the registration process. registered user data will be store inside
   * cassandra db.
   *
   * @return Promise<Result>
   */
  public Promise<Result> createUser() {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" get user registration request data = " + requestData,
          LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      RequestValidator.validateCreateUser(reqObj);
      
      if(ProjectUtil.isStringNullOREmpty((String) reqObj.getRequest().get(JsonKey.PROVIDER))){
        reqObj.getRequest().put(JsonKey.EMAIL_VERIFIED, false);
        reqObj.getRequest().put(JsonKey.PHONE_VERIFIED, false);
      }
      reqObj.setOperation(ActorOperations.CREATE_USER.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      ProjectUtil.updateMapSomeValueTOLowerCase(reqObj);
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }

  /**
   * This method will update user profile data. user can update all the data except email.
   *
   * @return Promise<Result>
   */
  public Promise<Result> updateUserProfile() {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" get user update profile data = " + requestData, LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      RequestValidator.validateUpdateUser(reqObj);
      reqObj.setOperation(ActorOperations.UPDATE_USER.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      
      if(ProjectUtil.isStringNullOREmpty((String) reqObj.getRequest().get(JsonKey.PROVIDER))){
        reqObj.getRequest().put(JsonKey.EMAIL_VERIFIED, false);
        reqObj.getRequest().put(JsonKey.PHONE_VERIFIED, false);
      }
      innerMap.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }


  /**
   * This method will do the user authentication based on login type key. login can be done with
   * following ways (simple login , Google plus login , Facebook login , Aadhaar login)
   *
   * @return Promise<Result>
   */
  public Promise<Result> login() {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" get user login data=" + requestData, LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      RequestValidator.validateUserLogin(reqObj);
      reqObj.setOperation(ActorOperations.LOGIN.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }

  /**
   * This method will invalidate user auth token .
   *
   * @return Promise<Result>
   */
  public Promise<Result> logout() {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" get user logout data = " + requestData, LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      reqObj.setOperation(ActorOperations.LOGOUT.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      innerMap.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      innerMap.put(JsonKey.AUTH_TOKEN,
          request().getHeader(HeaderParam.X_Authenticated_Userid.getName()));
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }

  /**
   * This method will allow user to change their password.
   *
   * @return Promise<Result>
   */
  public Promise<Result> changePassword() {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" get user change password data = " + requestData, LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      RequestValidator.validateChangePassword(reqObj);
      reqObj.setOperation(ActorOperations.CHANGE_PASSWORD.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      reqObj.getRequest().put(JsonKey.USER_ID,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      innerMap.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }

  /**
   * This method will provide user profile details based on requested userId.
   *
   * @return Promise<Result>
   */
  public Promise<Result> getUserProfile(String userId) {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" get user profile data by id =" + requestData, LoggerEnum.INFO.name());
      Request reqObj = new Request();
      reqObj.setOperation(ActorOperations.GET_PROFILE.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      reqObj.getRequest().put(JsonKey.USER_ID, userId);
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      innerMap.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }

  /**
   * This method will provide complete role details list.
   *
   * @return Promise<Result>
   */
  public Promise<Result> getRoles() {

    try {
      Request reqObj = new Request();
      reqObj.setOperation(ActorOperations.GET_ROLES.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }


  /**
   * Method to verify user existence in our DB.
   *
   * @return Promise<Result>
   */
  public Promise<Result> getUserDetailsByLoginId() {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" verify user details by loginId data =" + requestData,
          LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      RequestValidator.validateVerifyUser(reqObj);
      reqObj.setOperation(ActorOperations.GET_USER_DETAILS_BY_LOGINID.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      ProjectUtil.updateMapSomeValueTOLowerCase(reqObj);
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      innerMap.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }
  
  /**
   * This method will download user details for particular org or all organizations 
   *
   * @return Promise<Result>
   */
  public Promise<Result> downloadUsers() {
    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" Downlaod user data request =" + requestData,
          LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      reqObj.setOperation(ActorOperations.DOWNLOAD_USERS.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      ProjectUtil.updateMapSomeValueTOLowerCase(reqObj);
      innerMap.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
    }


  /**
   * This method will provide user profile details based on requested userId.
   *
   * @return Promise<Result>
   */
  public Promise<Result> blockUser() {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" blockuser =" + requestData,
          LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      reqObj.setOperation(ActorOperations.BLOCK_USER.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      innerMap.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }
  
  /**
   * This method will assign either user role directly or user org role.
   * @return Promise<Result>
   */
  public Promise<Result> assignRoles() {
    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" Assign roles api request body =" + requestData,
          LoggerEnum.INFO.name());
      Request reqObj =
          (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      RequestValidator.validateAssignRole(reqObj);
      reqObj.setOperation(ActorOperations.ASSIGN_ROLES.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      reqObj.getRequest().put(JsonKey.REQUESTED_BY, getUserIdByAuthToken(
          request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null,
          request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }

  /**
   * This method will changes user status from block to unblock
   *
   * @return Promise<Result>
   */
  public Promise<Result> unBlockUser() {

    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log(" unblockuser =" + requestData,
          LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      reqObj.setOperation(ActorOperations.UNBLOCK_USER.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      HashMap<String, Object> innerMap = new HashMap<>();
      innerMap.put(JsonKey.USER, reqObj.getRequest());
      innerMap.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      reqObj.setRequest(innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }

  /**
   * This method will do the user search for Elastic search.
   * this will internally call composite search api.
   *
   * @return Promise<Result>
   */
  public Promise<Result> search() {
    try {
      JsonNode requestData = request().body().asJson();
      ProjectLogger.log("User search api call =" + requestData,
          LoggerEnum.INFO.name());
      Request reqObj = (Request) mapper.RequestMapper.mapRequest(requestData, Request.class);
      reqObj.setOperation(ActorOperations.COMPOSITE_SEARCH.getValue());
      reqObj.setRequest_id(ExecutionContext.getRequestId());
      reqObj.setEnv(getEnvironment());
      reqObj.put(JsonKey.REQUESTED_BY,
          getUserIdByAuthToken(request().getHeader(HeaderParam.X_Authenticated_Userid.getName())));
      HashMap<String, Object> innerMap = new HashMap<>();
      innerMap.put(JsonKey.OBJECT_TYPE, new ArrayList<>().add("user"));
      reqObj.getRequest().put(JsonKey.FILTERS, innerMap);
      Timeout timeout = new Timeout(Akka_wait_time, TimeUnit.SECONDS);
      return actorResponseHandler(getRemoteActor(), reqObj, timeout, null, request());
    } catch (Exception e) {
      return Promise.<Result>pure(createCommonExceptionResponse(e, request()));
    }
  }
  
  
}