package com.celamanzi.liferay.portlets.rails286;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Locale;
import java.util.Map;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.*;
import org.apache.commons.httpclient.cookie.*;

import org.springframework.mock.web.portlet.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;

import javax.portlet.*;

import com.celamanzi.liferay.portlets.rails286.Rails286Portlet;

/** Tests handling of cookies set by Rails and how they mix with portlet cookies.
 */ 
public class RemoteCookiesTest {
    
    private final String host    = PortletTest.host;
    private final String servlet = PortletTest.servlet;
    
    private final String railsTestBenchRoute = PortletTest.railsTestBenchRoute;
    private final String railsJUnitRoute = PortletTest.railsJUnitRoute;
    private final String railsJUnitURL = PortletTest.railsJUnitURL;
    
    private String sessionSecret = PortletTest.sessionSecret;

    private Rails286Portlet portlet = new Rails286Portlet();
    private PortletConfig portletConfig = null;
    private PortletContext portletContext = new MockPortletContext();
    private PortletSession session = null;
    private String portletName = "__TEST__";

    private XPath xpath = null;
    private XPathExpression expr = null;
    private NodeList nodes = null;


    private void debugHeaders(Header[] headers) {
        for (Header h : headers)
            System.out.print(h.toString());
    }  


    @Before
    public void setup()
    throws MalformedURLException
    {
        assertNotNull(portlet);
        assertNotNull(portletContext);
        MockPortletConfig _portletConfig = new MockPortletConfig(portletContext,portletName);
        assertNotNull(_portletConfig);
        portletConfig = (PortletConfig)_portletConfig;
        
        session = new MockPortletSession();
        assertNotNull(session);
      
        // set session as set by RenderFilter
        session.setAttribute(
                             "railsBaseUrl",
                             new URL(host+"/"+servlet),
                             PortletSession.PORTLET_SCOPE);
        
        session.setAttribute(
                             "servlet",
                             servlet,
                             PortletSession.PORTLET_SCOPE);
        
        session.setAttribute(
                             "railsRoute",
                             null,
                             PortletSession.PORTLET_SCOPE);
        
        session.setAttribute(
                             "requestMethod",
                             null,
                             PortletSession.PORTLET_SCOPE);

        session.setAttribute(
                             "httpReferer",
                             null,
                             PortletSession.PORTLET_SCOPE);
        
        xpath = XPathFactory.newInstance().newXPath();
        expr = null;
        nodes = null;

    }


    @Test
    /** High level portlet session cookie handling.
     */
    public void test_portlet_session_cookie()
    throws 
    Exception, IOException, PortletException,
    ParserConfigurationException,
    XPathExpressionException
    {
        portlet.init(portletConfig);
    
        session.setAttribute("railsRoute",railsJUnitRoute+"/session_cookie");
        
        String sessionId = null;
                
        MockRenderRequest _request = new MockRenderRequest(PortletMode.VIEW);
        _request.setSession(session);
        RenderRequest request = (RenderRequest)_request;
        assertNotNull(request);
        
        RenderResponse response = new MockRenderResponse();
        assertNotNull(response);
        
        portlet.render(request,response);
      
        // assert that cookies were stored
        Cookie[] cookies = (Cookie[])session.getAttribute("cookies");
        assertNotNull(cookies);
        assertEquals(1,cookies.length); // security cookie not added
        
        // re-cast
        MockRenderResponse _response = (MockRenderResponse)response;
        
        String xml = _response.getContentAsString();
        //System.out.println(xml);
        Document doc = TestHelpers.html2doc(xml);
        assertNotNull(doc);
        
        expr = xpath.compile("//id/text()");
        nodes = TestHelpers.evalExpr(expr, doc);
        assertEquals(1,nodes.getLength());
        sessionId = nodes.item(0).getNodeValue();
        assertNotNull(sessionId);
        
        // render again and assert that the session remains the same

        response = new MockRenderResponse();
        assertNotNull(response);
        
        portlet.render(request,response);
        
        // re-cast to use mock method getContentAsString()
        _response = (MockRenderResponse)response;
        
        xml = _response.getContentAsString();
        //System.out.println(xml);
        doc = TestHelpers.html2doc(xml);
        assertNotNull(doc);
        
        expr = xpath.compile("//id/text()");
        nodes = TestHelpers.evalExpr(expr, doc);
        assertEquals(1,nodes.getLength());
        String _sessionId = nodes.item(0).getNodeValue();
        //System.out.println(sessionId);
        //System.out.println(_sessionId);
        
        assertEquals(sessionId,_sessionId);
    }


    @Test
    /** Low level session cookie handling.
     */
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
 

		@Test
    /** High level portlet handling of Rails cookies.
    		First get them from /foobarcookies, then get /foobarcookiestxt.
    	*/
		public void test_portlet_foobarcookies()
    throws
        javax.xml.parsers.ParserConfigurationException,
        javax.xml.xpath.XPathExpressionException,
        Exception
    {
        portlet.init(portletConfig);
    
        session.setAttribute("railsRoute",railsJUnitRoute+"/foobarcookies");
    
        MockRenderRequest _request = new MockRenderRequest(PortletMode.VIEW);
        _request.setSession(session);
        RenderRequest request = (RenderRequest)_request;
        assertNotNull(request);
        
        RenderResponse response = new MockRenderResponse();
        assertNotNull(response);

        portlet.render(request,response);
        assertEquals(200,portlet.responseStatusCode);
    
        // assert that cookies were stored
        Cookie[] cookies = (Cookie[])session.getAttribute("cookies");
        assertNotNull(cookies);
        assertEquals(3,cookies.length);

        session.setAttribute("railsRoute",railsJUnitRoute+"/foobarcookiestxt");
        _request.setSession(session);
        request = (RenderRequest)_request;
        assertNotNull(request);

        portlet.render(request,response);
        assertEquals(200,portlet.responseStatusCode);

        // re-cast
        MockRenderResponse _response = (MockRenderResponse)response;
        _response.setCharacterEncoding("UTF-8");
    
        String body = _response.getContentAsString().trim();
        //System.out.println(body);
        // some problems with the output..

				Pattern p = Pattern.compile("__g00d____yrcl____3ver__",Pattern.MULTILINE);
        Matcher txt = p.matcher(body);
				assert(txt.find());
    }


		@Test
    /** High level portlet handling of Rails cookies.
    		First get them from /foobarcookies_auth, then get /foobarcookiestxt_auth.
    		Include secret cookie to the request.
    	*/
		public void test_portlet_foobarcookies_auth()
    throws
        javax.xml.parsers.ParserConfigurationException,
        javax.xml.xpath.XPathExpressionException,
        Exception
    {
        MockPortletConfig _portletConfig = new MockPortletConfig(portletContext,portletName);
        assertNotNull(_portletConfig);
        _portletConfig.addInitParameter("secret", PortletTest.sessionSecret);
        portletConfig = (PortletConfig)_portletConfig;
    
        portlet.init(portletConfig);
    
        session.setAttribute("railsRoute",railsJUnitRoute+"/foobarcookies_auth");
    
        MockRenderRequest _request = new MockRenderRequest(PortletMode.VIEW);
        _request.setSession(session);
        RenderRequest request = (RenderRequest)_request;
        assertNotNull(request);
        
        RenderResponse response = new MockRenderResponse();
        assertNotNull(response);

        portlet.render(request,response);
        assertEquals(200,portlet.responseStatusCode);
    
        // assert that cookies were stored
        Cookie[] cookies = (Cookie[])session.getAttribute("cookies");
        assertNotNull(cookies);
        assertEquals(4,cookies.length);

        session.setAttribute("railsRoute",railsJUnitRoute+"/foobarcookiestxt_auth");
        _request.setSession(session);
        request = (RenderRequest)_request;
        assertNotNull(request);

        portlet.render(request,response);
        assertEquals(200,portlet.responseStatusCode);

        // re-cast
        MockRenderResponse _response = (MockRenderResponse)response;
        _response.setCharacterEncoding("UTF-8");
    
        String body = _response.getContentAsString().trim();
        //System.out.println(body);
        // some problems with the output..

				Pattern p = Pattern.compile("__g00d____yrcl____3ver__",Pattern.MULTILINE);
        Matcher txt = p.matcher(body);
				assert(txt.find());
    }


		@Test
    /** Low level handling of Rails cookies.
    		First get them from /foobarcookies, then get /foobarcookiestxt.
    	*/
		public void test_foobarcookies()
    throws
        javax.xml.parsers.ParserConfigurationException,
        javax.xml.xpath.XPathExpressionException,
        Exception
    {
        HttpClient client = new HttpClient();
        assertNotNull(client);
        
        String url =  railsJUnitURL+"/foobarcookies";
        
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

            //Header[] responseHeaders = method.getResponseHeaders();
            //debugHeaders(responseHeaders);
            //assertEquals(8,responseHeaders.length);
                        
            sessionCookies = client.getState().getCookies();
            assertEquals(3,sessionCookies.length);
            
        } catch (HttpException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        
        // See if the cookies get there
				try {
        
          url =  railsJUnitURL+"/foobarcookiestxt";
          method = new GetMethod(url);
          assertNotNull(method);
          method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

          // magic line
          client.getParams().setParameter("http.protocol.single-cookie-header", true);

          // Add cookies to the state
          HttpState state = new HttpState();
          state.addCookies(sessionCookies);
    			client.setState(state);
  
          int statusCode = client.executeMethod(method);
          assertEquals(200,statusCode);
          
          byte[] responseBody = method.getResponseBody();
          String body = new String(responseBody);
          //System.out.println(body);
          assertEquals("__g00d____yrcl____3ver__",body);

        } catch (HttpException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            method.releaseConnection();
        }
    }

}