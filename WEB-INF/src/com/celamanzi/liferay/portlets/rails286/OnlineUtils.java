/**
 * Copyright (c) 2009 Reinaldo Silva
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.celamanzi.liferay.portlets.rails286;

import javax.portlet.RenderRequest;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** Static helpers */
public class OnlineUtils {

	private static final Log log     = LogFactory.getLog(OnlineUtils.class);

	/** 
	 * @author Reinaldo Silva 
	 */
	protected static Cookie[] getRequestCookies(RenderRequest request, java.net.URL url)
	{
		javax.servlet.http.Cookie[] sr_cookies;
		org.apache.commons.httpclient.Cookie[] cookies;

		sr_cookies = request.getCookies();
		cookies = new org.apache.commons.httpclient.Cookie[sr_cookies.length];

		log.debug("Servlet request cookies -------v");

		for (int i = 0; i < sr_cookies.length; i++) {
			cookies[i] = new org.apache.commons.httpclient.Cookie(
					url.getHost(),
					sr_cookies[i].getName(),
					sr_cookies[i].getValue(),
					url.getPath(),
					sr_cookies[i].getMaxAge(),
					sr_cookies[i].getSecure()
			);

			log.debug("Servlet-Cookie: "
					+ cookies[i].toString()
					+ ", original-domain=" + sr_cookies[i].getDomain()
					+ ", domain=" + url.getHost()
					+ ", original-path=" + sr_cookies[i].getPath()
					+ ", path=" + cookies[i].getPath()
					+ ", max-age=" + cookies[i].getExpiryDate()
					+ ", secure=" + cookies[i].getSecure());
		}

		return cookies;
	}

}