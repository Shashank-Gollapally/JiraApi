import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;

import  static io.restassured.RestAssured.*;

import java.io.File;

public class JiraPractice {
	
	
	public static void main(String[] args)
	{
		RestAssured.baseURI="http://localhost:8080";
		//here we can sue session filter object to maintain the session throughout the script 
		SessionFilter session = new SessionFilter();
		//creating session id login scenaroi
		String reponse =given().log().all().relaxedHTTPSValidation().header("Content-Type","application/json")
		.body("{ \"username\": \"shanksha2\", \"password\": \"jira12345\" }")
		.filter(session)
		.when().log().all().post("/rest/auth/1/session")
		.then().log().all().assertThat().statusCode(200).extract().response().asString();
		
		String ExpectedComment = "addsd hello in the new issue";
		
		
		//Comments addition
		 String AddCommnetresponse =given().log().all().pathParam("id", "10003").header("Content-Type","application/json").body("{\r\n"
				+ "    \"body\": \""+ExpectedComment+"\",\r\n"
				+ "    \"visibility\": {\r\n"
				+ "        \"type\": \"role\",\r\n"
				+ "        \"value\": \"Administrators\"\r\n"
				+ "    }\r\n"
				+ "}")
		.filter(session)
		.when().log().all().post("/rest/api/2/issue/{id}/comment").then().assertThat().statusCode(201).extract().response().asString();
		 
		 JsonPath js = new JsonPath(AddCommnetresponse);
		String ID =  js.get("id");
		
		//adding attachment - use multipartmethod
		given().log().all().header("X-Atlassian-Token","no-check").filter(session)
		.pathParam("id", "10003").header("Content-Type","multipart/form-data").multiPart("file", new File("Jira.txt"))
		.when().log().all().post("/rest/api/2/issue/{id}/attachments")
		.then().log().all().assertThat().statusCode(200);
		
		//get issue 
	String issuedetails =	given().log().all().pathParam("id", "10003").queryParam("fields","comment").filter(session)
		.when().get("/rest/api/2/issue/{id}")
		.then().assertThat().statusCode(200).extract().response().asString();
	System.out.println(issuedetails);
	JsonPath js1 = new JsonPath(issuedetails);
	
int commentsize = js1.getInt("fields.comment.comments.size()");

for( int i = 0 ; i < commentsize;i++)
{
	 String commentid = js1.get("fields.comment.comments["+i+"].id");
	 System.out.println(commentid);
	 if( commentid.equalsIgnoreCase(ID))
	 {
		String  ActualComment= (js1.getString("fields.comment.comments["+i+"].body"));
		System.out.println(ActualComment);
		Assert.assertEquals(ActualComment, ExpectedComment,"the commnet is not copied ");
	 }
}

	
	}

}
