package com.celamanzi.liferay.portlets.rails286;

import java.util.*;
import java.net.URL;
import java.io.IOException;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.w3c.dom.Document;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.*;
import org.apache.commons.httpclient.cookie.*;

import com.celamanzi.liferay.portlets.rails286.Rails286Portlet;

/** Test Rails sessions with HttpClient.
 */
public class RailsSessionTest
{
    
    private final String host    = "http://localhost:3000";
    private final String servlet = "";
        
    private final String railsJUnitRoute = "/caterpillar/test_bench/junit";
    
    private final String railsJUnitURL = host+servlet+railsJUnitRoute;
    
    private Pattern pattern = null;
    private Matcher matcher = null;
    
    XPath xpath = null;
	XPathExpression expr = null;
	org.w3c.dom.NodeList nodes = null;
    
    
    @Before
    public void setup() {
        pattern = null;
        matcher = null;
        // assert railsJUnitURL responds
		xpath = XPathFactory.newInstance().newXPath();
        expr = null;
		nodes = null;
    }
    
    @After
    public void teardown() {
    }
    
  
    /*assertEquals("Server: WEBrick/1.3.1 (Ruby/1.8.7/2009-06-12)",
     method.getResponseHeader("Server"));*/
    
    
    @Test
    public void test_session_cookie()
    throws
        javax.xml.parsers.ParserConfigurationException,
        javax.xml.xpath.XPathExpressionException,
        Exception
    {
        HttpClient client = new HttpClient();
        assertNotNull(client);
        
        String url =  railsJUnitURL+"/session_cookie";
        
        GetMethod method = new GetMethod(url);
        assertNotNull(method);
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        
        Cookie[] sessionCookies = null;
        String sessionId = null;
        
        /* Get the session cookie. */
        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);
            assertEquals(200,statusCode);
            
            
            Header[] responseHeaders = method.getResponseHeaders();
            //debugHeaders(responseHeaders);
            assertEquals(8,responseHeaders.length);
                        
            sessionCookies = client.getState().getCookies();
            assertEquals(1,sessionCookies.length);
            
            /** Read session data from response body (example):
             
             <?xml version="1.0" encoding="UTF-8"?>
             <hash>
             <domain nil="true"></domain>
             <path>/</path>
             <key>_session_id</key>
             <expire-after nil="true"></expire-after>
             <httponly type="boolean">true</httponly>
             <id>500c90794e6a71149d7e49d4b4d7545e</id>
             </hash>
             
             */
            byte[] responseBody = method.getResponseBody();
            String xml = new String(responseBody);
            //System.out.println(xml);
            Document doc = TestHelpers.html2doc(xml);
            assertNotNull(doc);
            
            expr = xpath.compile("//id/text()");
            nodes = TestHelpers.evalExpr(expr, doc);
            assertEquals(1,nodes.getLength());
            sessionId = nodes.item(0).getNodeValue();
 
            //System.out.println(sessionId);
            
        } catch (HttpException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (org.xml.sax.SAXException e) {
            fail(e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        
        /** Prepare a new request with the old cookie. */
        assertNotNull(sessionCookies);
        assertNotNull(sessionId);
        client = new HttpClient();
        assertNotNull(client);

        method = new GetMethod(url);
        assertNotNull(method);
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        
        /* set the session cookies
         */
        HttpState initialState = new HttpState();
        client.setState(initialState);
        initialState.addCookies(sessionCookies);
        
        // manual option
        //method.addRequestHeader("Cookie",sessionCookies[0].toExternalForm());
        
        /* System.out.println("New request headers (with cookie), going to Rails");
        debugHeaders(method.getRequestHeaders());
        System.out.println(sessionCookie.toExternalForm());
         */
        
        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);
            assertEquals(200,statusCode);
            
            Cookie[] _sessionCookies = client.getState().getCookies();
            assertEquals(1,_sessionCookies.length);
            assertEquals(sessionCookies[0].toExternalForm(),
                         _sessionCookies[0].toExternalForm());
            // cookies match!
            
            Header[] responseHeaders = method.getResponseHeaders();
            //System.out.println("..and response headers from Rails");
            //debugHeaders(responseHeaders);
            
            byte[] responseBody = method.getResponseBody();
            String xml = new String(responseBody);
            //System.out.println(xml);
            Document doc = TestHelpers.html2doc(xml);
            assertNotNull(doc);
            
            expr = xpath.compile("//id/text()");
            nodes = TestHelpers.evalExpr(expr, doc);
            assertEquals(1,nodes.getLength());
            String _sessionId = nodes.item(0).getNodeValue();
            
            // FINALLY test that the session matches
            assertEquals(sessionId,_sessionId);
                        
            
        } catch (HttpException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        
    }
    
    @Test
    public void test_no_cookie()
    throws
        javax.xml.parsers.ParserConfigurationException,
        javax.xml.xpath.XPathExpressionException,
        Exception
    {
        HttpClient client = new HttpClient();
        assertNotNull(client);
        
        String url =  railsJUnitURL+"/session_cookie";
        
        GetMethod method = new GetMethod(url);
        assertNotNull(method);
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        
        Cookie[] sessionCookies = null;
        String sessionId = null;
        
        /* Assert Set-Cookie header and get the session cookie. */
        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);
            assertEquals(200,statusCode);
            
            Header[] responseHeaders = method.getResponseHeaders();
            //debugHeaders(responseHeaders);
            assertEquals(8,responseHeaders.length);
                        
            sessionCookies = client.getState().getCookies();
            assertEquals(1,sessionCookies.length);

            byte[] responseBody = method.getResponseBody();
            String xml = new String(responseBody);
            //System.out.println(xml);
            Document doc = TestHelpers.html2doc(xml);
            assertNotNull(doc);
            
            expr = xpath.compile("//id/text()");
            nodes = TestHelpers.evalExpr(expr, doc);
            assertEquals(1,nodes.getLength());
            sessionId = nodes.item(0).getNodeValue();
            
            //System.out.println(sessionId);
            
        } catch (HttpException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (org.xml.sax.SAXException e) {
            fail(e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
                
        /** Prepare a new request. */
        assertNotNull(sessionCookies);
        assertNotNull(sessionId);
        client = new HttpClient();
        assertNotNull(client);

        method = new GetMethod(url);
        assertNotNull(method);
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        
        
        /* do NOT set any cookies!
         */
        HttpState initialState = new HttpState();
        client.setState(initialState);

        //System.out.println("New request headers (without cookies), going to Rails");
        //debugHeaders(method.getRequestHeaders());
        
        
        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);
            assertEquals(200,statusCode);
            
            Cookie[] _sessionCookies = client.getState().getCookies();
            assertEquals(1,_sessionCookies.length);
            assertFalse(sessionCookies[0].toExternalForm().equals(
                         _sessionCookies[0].toExternalForm()));
            // cookies match!
            
            Header[] responseHeaders = method.getResponseHeaders();
            //System.out.println("..and response headers from Rails");
            //debugHeaders(responseHeaders);
            
            byte[] responseBody = method.getResponseBody();
            String xml = new String(responseBody);
            //System.out.println(xml);
            Document doc = TestHelpers.html2doc(xml);
            assertNotNull(doc);
            
            expr = xpath.compile("//id/text()");
            nodes = TestHelpers.evalExpr(expr, doc);
            assertEquals(1,nodes.getLength());
            String _sessionId = nodes.item(0).getNodeValue();
            
            // test that the session does not match
            assertFalse(sessionId.equals(_sessionId));
            
            
        } catch (HttpException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        
    }
    
    private void debugHeaders(Header[] headers) {
        for (Header h : headers)
            System.out.print(h.toString());
    }   
}
