/**
 * Echo server on Liberty
 */
package com.tjf;

import com.tjf.PATCH;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
import com.cloudant.client.org.lightcouch.NoDocumentException;

/**
 * @author frommeyer, tim
 *
 */
@Path("/echos")
public class EchoResource {
	private String databaseName = "echo_messages_db";
	private String account = "48bf69da-8e5e-4751-bec2-44afc39e5ae6-bluemix";
	private String user = "48bf69da-8e5e-4751-bec2-44afc39e5ae6-bluemix";
	private String password = "956f47b780d2fa05e2839a1fb56448f2049160780c86f9f566d3c80ecd2f918c";
	//the following credentials (API Key) do not work and I am not sure why
	//private String user = "chaderharthundstalkingst";
	//private String password = "85249dcf1278b4e18b6178229f4cff8863566d75";
	private CloudantClient cloudant = null;
	private Database db = null;
	private static String ERROR = "error";
	private String mySecret = "85249dcf1278b4e18b6178229f4cff8863566d75";
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEchoMessages() {
	//public List<EchoMessage> getEchoMessages() {
		//EchoMessage[] list = { new EchoMessage("777", "Hello"), new EchoMessage("888", "World"), new EchoMessage("999", "From Tim") };

		List<EchoMessage> list = null;
		ErrorMessage err = null;
		
		try {
			System.out.println("In GET list");
			list = this.findEchoMessageList();
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
			err = new ErrorMessage("999", "Major issue here>.  Call your admin. Exception: " + ex);
			return Response.status(500).entity(err).build();			
		}
		if(list == null){ //a problem has occurred
			err = new ErrorMessage("999", "Major issue here.  Call your admin.");
			return Response.status(500).entity(err).build();
		}
		else if(list.size() == 0){ //no documents
			err = new ErrorMessage("998", "No documents found.");
			return Response.status(404).entity(err).build();
		}
		else {  //good case
			return Response.status(200).entity(list).build();
		}
		//return list;
	}

	@GET
	@Path("/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEchoMessageById
	//public EchoMessage getEchoMessageById
	(@PathParam("param") String inStr) {
	
		EchoMessage msg = null;
		ErrorMessage err = null;
		
		try {
			System.out.println("In GET single, param from path: " + inStr);
			msg = this.findEchoMessage(inStr);
			System.out.println("From Cloudant:" + msg);
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
			err = new ErrorMessage("999", "Major internal server issue locating " + inStr + ". " + ex);
			return Response.status(500).entity(err).build();
		}
		
		if(msg == null){ //no document found
			err = new ErrorMessage("998", "Document not found for msgId " + inStr + ".");
			return Response.status(404).entity(err).build();
		}
		else {  //good case
			return Response.status(200).entity(msg).build();
		}	
		//return msg;
	}

	@GET
	@Path("/pingyou/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPingYou(@PathParam("param") String param,
	@QueryParam("name") @DefaultValue("Santa Claus") String name,  //DefaultValue doesn't seem to work
	@QueryParam("greeting") @DefaultValue("HoHoHo") String greeting) {
	
		EchoMessage msg = new EchoMessage();
		ErrorMessage err = null;
		String retStr = greeting + ", " + name + " " + param + ".";
		
		try {
			System.out.println("In GET ping " + retStr);
			msg.setMessageId(name);
			msg.setMessage(retStr);
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
			err = new ErrorMessage("999", "Major internal server issue locating " + retStr + ". " + ex);
			return Response.status(500).entity(err).build();
		}

		return Response.status(200).entity(msg).build();
	}
	
	@POST
	@Path("/{param}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postEchoMessage(EchoMessage requestBody, @PathParam("param") String inStr) {
		String str = "In POST, from request body " + inStr + ": " + requestBody.getMessage();
		System.out.println(str);
		//EchoMessage msg = new EchoMessage(inStr, "Response Body printMessage echo: " + str);
		EchoMessage msg = null;
		ErrorMessage err = null;
		boolean retVal = false;
		
		try {		
			msg = new EchoMessage(inStr, requestBody.getMessage());
			System.out.println(msg);
			retVal = this.storeEchoMessage(msg);
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
			err = new ErrorMessage("997", "Could not create a document for " + inStr + ". " + ex);
			return Response.status(500).entity(err).build();
		}
		
		if(retVal == true) { //good result document created
			return Response.status(201).entity(msg).build();
		}
		else {
			err = new ErrorMessage("997", "Could not create a document for " + inStr + ".");
			return Response.status(500).entity(err).build();
		}
	}

	//example of a algorithm REST service;uses POST and a verb (not a noun but noun could be acceptable to some)
	//param is UC (upper case), LC (lower case), etc.
	@POST
	@Path("/ConvertText/{param}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postConvertMessage(EchoMessage requestBody, @PathParam("param") String param) {
		String str = "In POST, from request body " + param + ": " + requestBody.getMessage();
		System.out.println(str);
		//EchoMessage msg = new EchoMessage(param, "Response Body printMessage echo: " + str);
		EchoMessage msg = null;
		ErrorMessage err = null;
		String retVal = null;
		
		try {		
			msg = new EchoMessage(param, requestBody.getMessage());
			System.out.println(msg);
			if(param.equalsIgnoreCase("LC") == true) //convert to lower case
			{
				retVal = this.convertToLowerCase(msg.getMessage());
				msg.setMessage(retVal);
			}
			else if(param.equalsIgnoreCase("UC") == true) //convert to upper case
			{
				retVal = this.convertToUpperCase(msg.getMessage());
				msg.setMessage(retVal);
			}
			else if(param.equalsIgnoreCase("ENC") == true) //encrypt string
			{
				retVal = this.encrypt(msg.getMessage());
				msg.setMessage(retVal);
			}
			else if(param.equalsIgnoreCase("DEC") == true) //decrypt string
			{
				retVal = this.decrypt(msg.getMessage());
				msg.setMessage(retVal);
			}
			else if(param.equalsIgnoreCase("OH") == true) //oneway hash using msg digest
			{
				retVal = this.onewayHash(msg.getMessage());
				msg.setMessage(retVal);
			}
			else if(param.equalsIgnoreCase("LEN") == true) //length of a string
			{
				retVal = this.lengthOfString(msg.getMessage());
				msg.setMessage(retVal+"hi fro");
			}
			else
			{
				//invalid param action
				System.out.println("Invalid action: " + param + " for message: " + msg.getMessage());
			}
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
			err = new ErrorMessage("997", "Could not convert a document to " + param + ". " + ex);
			return Response.status(500).entity(err).build();
		}
		
		if(retVal != null) { //good result 
			return Response.status(200).entity(msg).build();
		}
		else {
			err = new ErrorMessage("997", "Could not convert a document to " + param + ".");
			return Response.status(500).entity(err).build();
		}
	}
	
	@DELETE
	@Path("/{param}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteEchoMessage(EchoMessage requestBody, @PathParam("param") String inStr) {
		String str = "In DELETE, from path variable: " + inStr;
		System.out.println(str);
		EchoMessage msg = null;
		ErrorMessage err = null;
		
		try {
			msg = this.removeEchoMessage(inStr);
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
			err = new ErrorMessage("996", "Document not deleted for msgId " + inStr + ". " + ex);
			return Response.status(500).entity(err).build();
		}
		
		if(msg != null) { //good result document deleted
			return Response.status(204).entity(msg).build();
		}
		else {
			return Response.status(404).entity(err).build();
		}
		
		//return Response.status(200).entity(msg).build();
	}
	
	@PUT
	@Path("/{param}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putEchoMessage(EchoMessage requestBody, @PathParam("param") String inStr) {
		String str = "In PUT. from request body " + inStr + ": " + requestBody.getMessage();
		System.out.println(str);
		EchoMessage msg = null;
		ErrorMessage err = null;
		
		try {
			//EchoMessage msg = requestBody;//new EchoMessage(inStr, "Response Body printMessage echo: " + str);
			//System.out.println(msg.getMessage());
			requestBody.set_id(inStr);
			System.out.println("The changed EchoMessage is " + requestBody);
			msg = this.updateEchoMessage(requestBody);
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
			err = new ErrorMessage("995", "Document not updated for msgId " + inStr + ". " + ex);
			return Response.status(500).entity(err).build();
		}
		
		if(msg != null) { //good result document changed
			return Response.status(201).entity(msg).build();
		}
		else {
			err = new ErrorMessage("995", "Document not updated for msgId " + inStr + ".");
			return Response.status(404).entity(err).build();
		}
	}
	
	//March 9, 2017 - PATCH seems to not be supported in Bluemix Liberty. Never bothered to change the body
	//of the code so it is the same as the PUT update which actually should be the same code since EchoMessage
	//has just one field to change plus the key/ID
	//Get the following error from ARC:
	//{
	//"httpCode": "405",
	//"httpMessage": "Method Not Allowed",
	//"moreInformation": "The method is not allowed for the requested URL"
	//}
	@PATCH
	@Path("/{param}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response patchEchoMessage(EchoMessage requestBody, @PathParam("param") String inStr) {
		String str = "In PATCH. from request body " + inStr + ": " + requestBody.getMessage();
		System.out.println(str);
		EchoMessage msg = null;
		ErrorMessage err = null;
		
		try {
			//EchoMessage msg = requestBody;//new EchoMessage(inStr, "Response Body printMessage echo: " + str);
			//System.out.println(msg.getMessage());
			requestBody.set_id(inStr);
			System.out.println("The patched EchoMessage is " + requestBody);
			msg = this.replaceEchoMessage(requestBody);
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
			err = new ErrorMessage("995", "Document not patched for msgId " + inStr + ". " + ex);
			return Response.status(500).entity(err).build();
		}
		
		if(msg != null) { //good result document changed
			return Response.status(201).entity(msg).build();
		}
		else {
			err = new ErrorMessage("995", "Document not patched for msgId " + inStr + ".");
			return Response.status(404).entity(err).build();
		}
	}
	
	private boolean storeEchoMessage(EchoMessage msg){	
		this.initCloudantConnection();

		boolean retVal = true;
		
		try {
			msg.set_id(msg.getMessageId());
			System.out.println("Creating new doc: " + msg);
			com.cloudant.client.api.model.Response dbResponse = this.db.save(msg);
			System.out.println("DB response to store: " + dbResponse);
			if(dbResponse.getError() == null) {
				retVal = true;
			}
			else {
				retVal = false;
			}
		}
		catch(DocumentConflictException ex) {
			System.err.println("Exception: " + ex);
			retVal = false;
		}
		
		return retVal;
	}

	private EchoMessage updateEchoMessage(EchoMessage newMsg){	
		this.initCloudantConnection();
 
		EchoMessage prevMsg = null;

		try {
			prevMsg = findEchoMessageBy_id(newMsg.get_id());
			
			if (prevMsg != null) {
				System.out.println("Updating doc from: " + prevMsg + " to: " + newMsg);
				prevMsg.setMessage(newMsg.getMessage());
				com.cloudant.client.api.model.Response dbResponse = this.db.update(prevMsg);
				System.out.println("Updated doc revision: " + dbResponse.getRev());
				System.out.println("DB response to update: " + dbResponse);
			}
			else
			{
				System.out.println("Did NOT update: " + prevMsg);
			}
		}
		catch(DocumentConflictException ex) {
			System.err.println("Exception: " + ex);
			prevMsg = null;
		}
		
		return prevMsg;
	}

	private EchoMessage replaceEchoMessage(EchoMessage newMsg){	
		this.initCloudantConnection();
 
		EchoMessage prevMsg = null;

		try {
			prevMsg = findEchoMessageBy_id(newMsg.get_id());
			
			if (prevMsg != null) {
				System.out.println("Replacing doc from: " + prevMsg + " to: " + newMsg);
				prevMsg.setMessage(newMsg.getMessage());
				com.cloudant.client.api.model.Response dbResponse = this.db.update(prevMsg);
				System.out.println("Replaced doc revision: " + dbResponse.getRev());
				System.out.println("DB response to replace: " + dbResponse);
			}
			else
			{
				System.out.println("Did NOT replace: " + prevMsg);
			}
		}
		catch(DocumentConflictException ex) {
			System.err.println("Exception: " + ex);
			prevMsg = null;
		}
		
		return prevMsg;
	}
	
	private EchoMessage removeEchoMessage(String msgId){	
		this.initCloudantConnection();
		
		EchoMessage msg = null;

		try {
			msg = findEchoMessage(msgId);
			
			if (msg != null) {
				System.out.println("Deleting doc: " + msg);
				com.cloudant.client.api.model.Response dbResponse = this.db.remove(msg.get_id(), msg.get_rev());
				System.out.println("DB response to delete: " + dbResponse);
			}
			else
			{
				System.out.println("Did NOT delete: " + msg);
				msg = null;
			}
		}
		catch(NoDocumentException ex) {
			System.err.println("Did NOT delete: " + msg);
			System.err.println("Exception: " + ex);
			msg = null;
		}
		
		return msg;
	}

	private EchoMessage findEchoMessage(String msgId){	
		this.initCloudantConnection();
		
		EchoMessage msg = null;
		
		try {
			//EchoMessage msg =  db.find(EchoMessage.class, _id);  //this works for using Cloudant _id
			String selector = "{\"selector\": {\"messageId\": {\"$eq\":\"" + msgId + "\"}}}";
			System.out.println("The selector used: " + selector);
			List<EchoMessage> list = db.findByIndex(selector, EchoMessage.class);
			if(list.size() > 0) {
				msg = list.get(0);
				System.out.println("Found: " + msg);
			}
			else
			{
				//msg = new EchoMessage(EchoResource.ERROR, msgId + " not found");
				System.out.println(msg);
				msg = null;
			}
		}
		catch(NoDocumentException ex) {
			System.err.println("Exception: " + ex);
			msg = null;
		}
		
		return msg;
	}
	
	private EchoMessage findEchoMessageBy_id(String id){	
		this.initCloudantConnection();
		
		EchoMessage msg = null;
		
		try {
			System.out.println("id is " + id);
			/* this work as well
			String selector = "{\"selector\": {\"_id\": {\"$eq\":\"" + id + "\"}}}";
			System.out.println("The selector used: " + selector);
			List<EchoMessage> list = db.findByIndex(selector, EchoMessage.class);
			EchoMessage msg = list.get(0);
			*/
			//but this one is better
			msg =  db.find(EchoMessage.class, id);
			if(msg != null) {
				System.out.println("Found: " + msg);
			}
			else {
				System.out.println("Not Found for " + id);
			}
		}
		catch(NoDocumentException ex) {
			System.err.println("Exception: " + ex);
			msg = null;
		}
		
		return msg;
	}

	private List<EchoMessage> findEchoMessageList(){	
		this.initCloudantConnection();
		
		List<EchoMessage> list = null;
		String selector = "{\"selector\": {\"_id\": {\"$gt\": 0}},\"sort\": [{\"_id\": \"asc\"}]}";
		System.out.println("The selector: " + selector);
		
		try {
			list = db.findByIndex(selector, EchoMessage.class);
			if(list.size() > 0) {
				System.out.println("Found [" + list.size() + "] example: " + list.get(0));
			}
			else {
				System.out.println("No records found");
				list = null;
			}
			
		}
		catch(NoDocumentException ex) {
			System.err.println("Exception: " + ex);
			list = null;
		}
		
		return list;
	}
	
	private void initCloudantConnection() {
		System.out.println("Initializing Cloudant");
		/*CloudantClient client = ClientBuilder.url(new URL("https://yourCloudantLocalAddress.example")) 
				                           .username(this.user) 
				                           .password(this.password) 
				                           .build();
		*/
		try {		
			this.cloudant = ClientBuilder.account(this.account)
					.username(this.user)
					.password(this.password)
					.build();
			
			this.db = cloudant.database(databaseName, true);
		}
		catch(Exception ex) {
			System.err.println("Exception: " + ex);
		}		
		
		return;
	}

	private String convertToLowerCase(String inStr) {
		String retVal = inStr.toLowerCase();
		
		return retVal;
	}
	
	private String convertToUpperCase(String inStr) {
		String retVal = inStr.toUpperCase();
		
		return retVal;
	}
	
	private String encrypt(String inStr) {
		AesCryptoTool aes = new AesCryptoTool(this.mySecret);
		
		String retVal = aes.encrypt(inStr);
		
		return retVal;
	}
	
	private String decrypt(String inStr) {
		AesCryptoTool aes = new AesCryptoTool(this.mySecret);
		
		String retVal = aes.decrypt(inStr);
		
		return retVal;
	}
	
	private String onewayHash(String inStr) {
		OnewayHashTool hasher = new OnewayHashTool(inStr);
		
		String retVal = hasher.owHash(OnewayHashTool.SHA256);
		
		return retVal;
	}
	
	private String lengthOfString(String inStr) {
		
		String retVal = String.valueOf(inStr.length());
		
		return retVal;
	}
}
