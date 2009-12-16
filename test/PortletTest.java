package com.celamanzi.liferay.portlets.rails286;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.Locale;
import java.util.Map;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.Cookie;

import org.springframework.mock.web.portlet.*;

import javax.portlet.*;

import com.celamanzi.liferay.portlets.rails286.Rails286Portlet;


public class PortletTest {
    
    private Rails286Portlet portlet = new Rails286Portlet();
    private PortletConfig portletConfig = null;
    private PortletContext portletContext = new MockPortletContext();
    private PortletSession session = null;
    private String portletName = "__TEST__";
  
    private final String host    = "http://localhost:3000";
    private final String servlet = "";
    private final String route   = "/";

    protected final static String railsJUnitRoute = "/caterpillar/test_bench/junit";
    private final String railsJUnitURL = host+servlet+railsJUnitRoute;

    
    @Before
    public void setup()
    throws MalformedURLException
    {
        assertNotNull(portlet);
        assertNotNull(portletContext);
        MockPortletConfig _portletConfig = new MockPortletConfig(portletContext,portletName);
        assertNotNull(_portletConfig);
      
        _portletConfig.addInitParameter("host", host);
        _portletConfig.addInitParameter("servlet", servlet);
        _portletConfig.addInitParameter("route", route);
      
        portletConfig = (PortletConfig)_portletConfig;
        
        session = new MockPortletSession();
        assertNotNull(session);
      
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
                             route,
                             PortletSession.PORTLET_SCOPE);
        
        session.setAttribute(
                             "requestMethod",
                             null,
                             PortletSession.PORTLET_SCOPE);
        
        session.setAttribute(
                             "httpReferer",
                             null,
                             PortletSession.PORTLET_SCOPE);
        
      
    }
    
    
    @Test
    public void test_init()
    throws PortletException
    {
        assertNotNull(portlet);
        portlet.init(portletConfig);
    }
    
    
    @Test
    public void test_render()
    throws PortletException, IOException
    {
        portlet.init(portletConfig);
      
        MockRenderRequest _request = new MockRenderRequest(PortletMode.VIEW);
        _request.setSession(session);
        RenderRequest request = (RenderRequest)_request;
        assertNotNull(request);
        
        RenderResponse response = new MockRenderResponse();
        assertNotNull(response);
        
        portlet.render(request,response);

        URL _baseUrl = (URL)session.getAttribute("railsBaseUrl");
        assertNotNull(_baseUrl);
        assertEquals(new URL(host+"/"+servlet),_baseUrl);
        // TODO: test the thing with different combinations
        
        String _servlet = (String)session.getAttribute("servlet");
        assertNotNull(_servlet);
        assertEquals(servlet,_servlet);
        
        String _route = (String)session.getAttribute("railsRoute");
        assertNotNull(_route);
        assertEquals(route,_route);
        
        String _method = (String)session.getAttribute("requestMethod");
        assertNull(_method);
        
        assertNull(session.getAttribute("httpReferer"));
  }
    
    
  @Test
	public void test_processAction()
  throws PortletException, IOException
  {
    portlet.init(portletConfig);
    
    MockActionRequest _request = new MockActionRequest(PortletMode.VIEW);
    _request.setSession(session);
    ActionRequest request = (ActionRequest)_request;
    assertNotNull(request);
    
    ActionResponse response = new MockActionResponse();
    assertNotNull(response);
    
    //portletRequest.addParameter("param1", "value1");
    
    portlet.processAction(request,response);
    // TODO: re-design and test
	}
  
  @Test
  public void test_redirect()
  throws IOException, PortletException, MalformedURLException
  {
    portlet.init(portletConfig);
    
    session.setAttribute("railsBaseUrl",new URL(host));
    session.setAttribute("servlet",servlet);
    session.setAttribute("railsRoute",railsJUnitRoute+"/redirect");
    
    String targetURI = railsJUnitRoute+"/redirect_target";
    
    MockRenderRequest _request = new MockRenderRequest(PortletMode.VIEW);
    _request.setSession(session);
    RenderRequest request = (RenderRequest)_request;
    assertNotNull(request);
    
    RenderResponse response = new MockRenderResponse();
    assertNotNull(response);
    
    portlet.render(request,response);

    // re-cast to read response body;
    // the body contains the correct value to match with
    MockRenderResponse _response = (MockRenderResponse)response;
    String _targetURI = _response.getContentAsString().trim();
    
    assertEquals(targetURI,_targetURI);
  }
    
  @Test
  /** Test that Cookie[] can be stored to PortletSession correctly.
   */
  public void test_CookiesInPortletSession()
  {
    PortletSession session = new MockPortletSession();
    assertNotNull(session);
    
    Cookie cookie1 = new Cookie("_domain","_name1","_value1"); 
    Cookie cookie2 = new Cookie("_domain","_name2","_value2"); 
    Cookie[] cookies = {cookie1,cookie2};
    assertEquals(2,cookies.length);
        
    session.setAttribute("cookies",
                         cookies,
                         PortletSession.PORTLET_SCOPE);
    
    Cookie[] _cookies = (Cookie[])session.getAttribute("cookies");
    assertEquals(2,_cookies.length);
    
    for (int i=0 ; i < cookies.length ; i++) {
      assertEquals(cookies[i],_cookies[i]);
    }
  }
  

}
