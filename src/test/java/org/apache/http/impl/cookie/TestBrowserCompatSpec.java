/*
 * $HeadURL$
 * $Revision$
 * $Date$
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.cookie;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.message.BasicHeader;


/**
 * Test cases for BrowserCompatSpec
 *
 * @author BC Holmes
 * @author Rod Waldhoff
 * @author dIon Gillard
 * @author <a href="mailto:JEvans@Cyveillance.com">John Evans</a>
 * @author Marc A. Saegesser
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @version $Revision$
 */
public class TestBrowserCompatSpec extends TestCase {

    // ------------------------------------------------------------ Constructor

    public TestBrowserCompatSpec(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestBrowserCompatSpec.class);
    }

    public void testConstructor() throws Exception {
        new BrowserCompatSpec();
        new BrowserCompatSpec(null);
        new BrowserCompatSpec(new String[] { DateUtils.PATTERN_RFC1036 });
    }
    
    /**
     * Tests whether domain attribute check is case-insensitive.
     */
    public void testDomainCaseInsensitivity() throws Exception {
        Header header = new BasicHeader("Set-Cookie", 
            "name=value; path=/; domain=.whatever.com");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.WhatEver.com", 80, "/", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        assertEquals(".whatever.com", parsed[0].getDomain());
    }
    
    /**
     * Test basic parse (with various spacings
     */
    public void testParse1() throws Exception {
        String headerValue = "custno = 12345; comment=test; version=1," +
            " name=John; version=1; max-age=600; secure; domain=.apache.org";

        Header header = new BasicHeader("set-cookie", headerValue);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.apache.org", 80, "/", false);
        Cookie[] cookies = cookiespec.parse(header, origin);
        for (int i = 0; i < cookies.length; i++) {
            cookiespec.validate(cookies[i], origin);
        }
        assertEquals(2, cookies.length);

        assertEquals("custno", cookies[0].getName());
        assertEquals("12345", cookies[0].getValue());
        assertEquals("test", cookies[0].getComment());
        assertEquals(0, cookies[0].getVersion());
        assertEquals("www.apache.org", cookies[0].getDomain());
        assertEquals("/", cookies[0].getPath());
        assertFalse(cookies[0].isSecure());

        assertEquals("name", cookies[1].getName());
        assertEquals("John", cookies[1].getValue());
        assertEquals(null, cookies[1].getComment());
        assertEquals(0, cookies[1].getVersion());
        assertEquals(".apache.org", cookies[1].getDomain());
        assertEquals("/", cookies[1].getPath());
        assertTrue(cookies[1].isSecure());
    }

    /**
     * Test no spaces
     */
    public void testParse2() throws Exception {
        String headerValue = "custno=12345;comment=test; version=1," +
            "name=John;version=1;max-age=600;secure;domain=.apache.org";

        Header header = new BasicHeader("set-cookie", headerValue);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.apache.org", 80, "/", false);
        Cookie[] cookies = cookiespec.parse(header, origin);
        for (int i = 0; i < cookies.length; i++) {
            cookiespec.validate(cookies[i], origin);
        }

        assertEquals(2, cookies.length);

        assertEquals("custno", cookies[0].getName());
        assertEquals("12345", cookies[0].getValue());
        assertEquals("test", cookies[0].getComment());
        assertEquals(0, cookies[0].getVersion());
        assertEquals("www.apache.org", cookies[0].getDomain());
        assertEquals("/", cookies[0].getPath());
        assertFalse(cookies[0].isSecure());

        assertEquals("name", cookies[1].getName());
        assertEquals("John", cookies[1].getValue());
        assertEquals(null, cookies[1].getComment());
        assertEquals(0, cookies[1].getVersion());
        assertEquals(".apache.org", cookies[1].getDomain());
        assertEquals("/", cookies[1].getPath());
        assertTrue(cookies[1].isSecure());
    }


    /**
     * Test parse with quoted text
     */
    public void testParse3() throws Exception {
        String headerValue =
            "name=\"Doe, John\";version=1;max-age=600;secure;domain=.apache.org";
        Header header = new BasicHeader("set-cookie", headerValue);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.apache.org", 80, "/", false);
        Cookie[] cookies = cookiespec.parse(header, origin);
        for (int i = 0; i < cookies.length; i++) {
            cookiespec.validate(cookies[i], origin);
        }

        assertEquals(1, cookies.length);

        assertEquals("name", cookies[0].getName());
        assertEquals("Doe, John", cookies[0].getValue());
        assertEquals(null, cookies[0].getComment());
        assertEquals(0, cookies[0].getVersion());
        assertEquals(".apache.org", cookies[0].getDomain());
        assertEquals("/", cookies[0].getPath());
        assertTrue(cookies[0].isSecure());
    }


    // see issue #5279
    public void testQuotedExpiresAttribute() throws Exception {
        String headerValue = "custno=12345;Expires='Thu, 01-Jan-2070 00:00:10 GMT'";

        Header header = new BasicHeader("set-cookie", headerValue);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.apache.org", 80, "/", true);
        Cookie[] cookies = cookiespec.parse(header, origin);
        for (int i = 0; i < cookies.length; i++) {
            cookiespec.validate(cookies[i], origin);
        }
        assertNotNull("Expected some cookies",cookies);
        assertEquals("Expected 1 cookie",1,cookies.length);
        assertNotNull("Expected cookie to have getExpiryDate",cookies[0].getExpiryDate());
    }

    public void testSecurityError() throws Exception {
        String headerValue = "custno=12345;comment=test; version=1," +
            "name=John;version=1;max-age=600;secure;domain=jakarta.apache.org";
        Header header = new BasicHeader("set-cookie", headerValue);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.apache.org", 80, "/", true);
        try {
            Cookie[] cookies = cookiespec.parse(header, origin);
            for (int i = 0; i < cookies.length; i++) {
                cookiespec.validate(cookies[i], origin);
            }
            fail("MalformedCookieException exception should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    public void testParseSimple() throws Exception {
        Header header = new BasicHeader("Set-Cookie","cookie-name=cookie-value");
        
        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/path/path", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertTrue("Comment",null == parsed[0].getComment());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent",!parsed[0].isPersistent());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/path",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].isSecure());
        assertEquals("Version",0,parsed[0].getVersion());
    }
 
    public void testParseSimple2() throws Exception {
        Header header = new BasicHeader("Set-Cookie", "cookie-name=cookie-value");
    
        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/path", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.", 1, parsed.length);
        assertEquals("Name", "cookie-name", parsed[0].getName());
        assertEquals("Value", "cookie-value", parsed[0].getValue());
        assertTrue("Comment", null == parsed[0].getComment());
        assertTrue("ExpiryDate", null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent", !parsed[0].isPersistent());
        assertEquals("Domain", "127.0.0.1", parsed[0].getDomain());
        assertEquals("Path", "/", parsed[0].getPath());
        assertTrue("Secure", !parsed[0].isSecure());
        assertEquals("Version", 0, parsed[0].getVersion());
    }
 
    public void testParseNoName() throws Exception {
        Header header = new BasicHeader("Set-Cookie","=stuff; path=/");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", false);
        try {
            Cookie[] parsed = cookiespec.parse(header, origin);
            for (int i = 0; i < parsed.length; i++) {
                cookiespec.validate(parsed[i], origin);
            }
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }
 
    public void testParseNoValue() throws Exception {
        Header header = new BasicHeader("Set-Cookie","cookie-name=");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value", "", parsed[0].getValue());
        assertTrue("Comment",null == parsed[0].getComment());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent",!parsed[0].isPersistent());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].isSecure());
        assertEquals("Version",0,parsed[0].getVersion());
    }

    public void testParseWithWhiteSpace() throws Exception {
        Header header = new BasicHeader("Set-Cookie"," cookie-name  =    cookie-value  ");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].isSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithQuotes() throws Exception {
        Header header = new BasicHeader("Set-Cookie"," cookie-name  =  \" cookie-value \" ;path=/");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1",80, "/", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value"," cookie-value ",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].isSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithPath() throws Exception {
        Header header = new BasicHeader("Set-Cookie","cookie-name=cookie-value; Path=/path/");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/path/path", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/path/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].isSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithDomain() throws Exception {
        Header header = new BasicHeader("Set-Cookie","cookie-name=cookie-value; Domain=127.0.0.1");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].isSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithSecure() throws Exception {
        Header header = new BasicHeader("Set-Cookie","cookie-name=cookie-value; secure");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", true);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",parsed[0].isSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithComment() throws Exception {
        Header header = new BasicHeader("Set-Cookie",
            "cookie-name=cookie-value; comment=\"This is a comment.\"");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", true);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].isSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertEquals("Comment","This is a comment.",parsed[0].getComment());
    }

    public void testParseWithExpires() throws Exception {
        Header header = new BasicHeader("Set-Cookie",
            "cookie-name=cookie-value;Expires=Thu, 01-Jan-1970 00:00:10 GMT");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", true);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].isSecure());
        assertEquals(new Date(10000L),parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithAll() throws Exception {
        Header header = new BasicHeader("Set-Cookie",
            "cookie-name=cookie-value;Version=1;Path=/commons;Domain=.apache.org;" + 
            "Comment=This is a comment.;secure;Expires=Thu, 01-Jan-1970 00:00:10 GMT");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.apache.org", 80, "/commons/httpclient", true);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain",".apache.org",parsed[0].getDomain());
        assertEquals("Path","/commons",parsed[0].getPath());
        assertTrue("Secure",parsed[0].isSecure());
        assertEquals(new Date(10000L),parsed[0].getExpiryDate());
        assertEquals("Comment","This is a comment.",parsed[0].getComment());
        assertEquals("Version",0,parsed[0].getVersion());
    }

    public void testParseMultipleDifferentPaths() throws Exception {
        Header header = new BasicHeader("Set-Cookie",
            "name1=value1;Version=1;Path=/commons,name1=value2;Version=1;" +
            "Path=/commons/httpclient;Version=1");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.apache.org", 80, "/commons/httpclient", true);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Wrong number of cookies.",2,parsed.length);
        assertEquals("Name","name1",parsed[0].getName());
        assertEquals("Value","value1",parsed[0].getValue());
        assertEquals("Name","name1",parsed[1].getName());
        assertEquals("Value","value2",parsed[1].getValue());
    }

    public void testParseRelativePath() throws Exception {
        Header header = new BasicHeader("Set-Cookie", "name1=value1;Path=whatever");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.apache.org", 80, "whatever", true);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertEquals("Found 1 cookies.",1,parsed.length);
        assertEquals("Name","name1",parsed[0].getName());
        assertEquals("Value","value1",parsed[0].getValue());
        assertEquals("Path","whatever",parsed[0].getPath());
    }

    public void testParseWithWrongDomain() throws Exception {
        Header header = new BasicHeader("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; version=1");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.2", 80, "/", false);
        try {
            Cookie[] parsed = cookiespec.parse(header, origin);
            for (int i = 0; i < parsed.length; i++) {
                cookiespec.validate(parsed[i], origin);
            }
            fail("MalformedCookieException exception should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    public void testParseWithPathMismatch() throws Exception {
        Header header = new BasicHeader("Set-Cookie",
            "cookie-name=cookie-value; path=/path/path/path");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/path", false);
        try {
            Cookie[] parsed = cookiespec.parse(header, origin);
            for (int i = 0; i < parsed.length; i++) {
                cookiespec.validate(parsed[i], origin);
            }
            fail("MalformedCookieException should have been thrown.");
        } catch (MalformedCookieException e) {
            // expected
        }
    }
    
    public void testParseWithPathMismatch2() throws Exception {
        Header header = new BasicHeader("Set-Cookie",
            "cookie-name=cookie-value; path=/foobar");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/foo", false);
        try {
            Cookie[] parsed = cookiespec.parse(header, origin);
            for (int i = 0; i < parsed.length; i++) {
                cookiespec.validate(parsed[i], origin);
            }
            fail("MalformedCookieException should have been thrown.");
        } catch (MalformedCookieException e) {
            // expected
        }
    }

    /**
     * Tests if cookie constructor rejects cookie name containing blanks.
     */
    public void testCookieNameWithBlanks() throws Exception {
        Header header = new BasicHeader("Set-Cookie", "invalid name=");
        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
    }

    /**
     * Tests if cookie constructor rejects cookie name containing blanks.
     */
    public void testCookieNameBlank() throws Exception {
        Header header = new BasicHeader("Set-Cookie", "=stuff");
        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", false);
        try {
            Cookie[] parsed = cookiespec.parse(header, origin);
            for (int i = 0; i < parsed.length; i++) {
                cookiespec.validate(parsed[i], origin);
            }
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    /**
     * Tests if cookie constructor rejects cookie name starting with $.
     */
    public void testCookieNameStartingWithDollarSign() throws Exception {
        Header header = new BasicHeader("Set-Cookie", "$invalid_name=");
        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("127.0.0.1", 80, "/", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
    }


    /**
     * Tests if malformatted expires attribute is parsed correctly.
     */
    public void testCookieWithComma() throws Exception {
        Header header = new BasicHeader("Set-Cookie", "name=value; expires=\"Thu, 01-Jan-1970 00:00:00 GMT");

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("localhost", 80, "/", false);
        try {
            Cookie[] parsed = cookiespec.parse(header, origin);
            for (int i = 0; i < parsed.length; i++) {
                cookiespec.validate(parsed[i], origin);
            }
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }
    

    /**
     * Tests several date formats.
     */
    public void testDateFormats() throws Exception {
        //comma, dashes
        checkDate("Thu, 01-Jan-70 00:00:10 GMT");
        checkDate("Thu, 01-Jan-2070 00:00:10 GMT");
        //no comma, dashes
        checkDate("Thu 01-Jan-70 00:00:10 GMT");
        checkDate("Thu 01-Jan-2070 00:00:10 GMT");
        //comma, spaces
        checkDate("Thu, 01 Jan 70 00:00:10 GMT");
        checkDate("Thu, 01 Jan 2070 00:00:10 GMT");
        //no comma, spaces
        checkDate("Thu 01 Jan 70 00:00:10 GMT");
        checkDate("Thu 01 Jan 2070 00:00:10 GMT");
        //weird stuff
        checkDate("Wed, 20-Nov-2002 09-38-33 GMT");


        try {
            checkDate("this aint a date");
            fail("Date check is bogous");
        } catch(Exception e) {
            /* must fail */
        }
    }

    private void checkDate(String date) throws Exception {
        Header header = new BasicHeader("Set-Cookie", "custno=12345;Expires='"+date+"';");
        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("localhost", 80, "/", false);
        Cookie[] parsed = cookiespec.parse(header, origin);
        for (int i = 0; i < parsed.length; i++) {
            cookiespec.validate(parsed[i], origin);
        }
    }

    /**
     * Tests if invalid second domain level cookie gets accepted in the
     * browser compatibility mode.
     */
    public void testSecondDomainLevelCookie() throws Exception {
        Cookie cookie = new BasicCookie("name", null);
        cookie.setDomain(".sourceforge.net");
        cookie.setDomainAttributeSpecified(true);
        cookie.setPath("/");
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("sourceforge.net", 80, "/", false);
        cookiespec.validate(cookie, origin);
    }

    public void testSecondDomainLevelCookieMatch1() throws Exception {
        Cookie cookie = new BasicCookie("name", null);
        cookie.setDomain(".sourceforge.net");
        cookie.setDomainAttributeSpecified(true);
        cookie.setPath("/");
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("sourceforge.net", 80, "/", false);
        assertTrue(cookiespec.match(cookie, origin));
    }

    public void testSecondDomainLevelCookieMatch2() throws Exception {
        Cookie cookie = new BasicCookie("name", null);
        cookie.setDomain("sourceforge.net");
        cookie.setDomainAttributeSpecified(true);
        cookie.setPath("/");
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("www.sourceforge.net", 80, "/", false);
        assertTrue(cookiespec.match(cookie, origin));
    }

    public void testSecondDomainLevelCookieMatch3() throws Exception {
        Cookie cookie = new BasicCookie("name", null);
        cookie.setDomain(".sourceforge.net");
        cookie.setDomainAttributeSpecified(true);
        cookie.setPath("/");
        cookie.setPathAttributeSpecified(true);

         CookieSpec cookiespec = new BrowserCompatSpec();
         CookieOrigin origin = new CookieOrigin("www.sourceforge.net", 80, "/", false);
         assertTrue(cookiespec.match(cookie, origin));
    }
         
    public void testInvalidSecondDomainLevelCookieMatch1() throws Exception {
        Cookie cookie = new BasicCookie("name", null);
        cookie.setDomain(".sourceforge.net");
        cookie.setDomainAttributeSpecified(true);
        cookie.setPath("/");
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("antisourceforge.net", 80, "/", false);
        assertFalse(cookiespec.match(cookie, origin));
    }

    public void testInvalidSecondDomainLevelCookieMatch2() throws Exception {
        Cookie cookie = new BasicCookie("name", null);
        cookie.setDomain("sourceforge.net");
        cookie.setDomainAttributeSpecified(true);
        cookie.setPath("/");
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("antisourceforge.net", 80, "/", false);
        assertFalse(cookiespec.match(cookie, origin));
    }

    public void testMatchBlankPath() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host");
        cookie.setPath("/");
        CookieOrigin origin = new CookieOrigin("host", 80, "  ", false);
        assertTrue(cookiespec.match(cookie, origin));
    }

    public void testMatchNullCookieDomain() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setPath("/");
        CookieOrigin origin = new CookieOrigin("host", 80, "/", false);
        assertFalse(cookiespec.match(cookie, origin));
    }

    public void testMatchNullCookiePath() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host");
        CookieOrigin origin = new CookieOrigin("host", 80, "/", false);
        assertTrue(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMatch1() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host");
        cookie.setPath("/");
        CookieOrigin origin = new CookieOrigin("host", 80, "/", false);
        assertTrue(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMatch2() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain(".whatever.com");
        cookie.setPath("/");
        CookieOrigin origin = new CookieOrigin(".whatever.com", 80, "/", false);
        assertTrue(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMatch3() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain(".whatever.com");
        cookie.setPath("/");
        CookieOrigin origin = new CookieOrigin(".really.whatever.com", 80, "/", false);
        assertTrue(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMatch4() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host");
        cookie.setPath("/");
        CookieOrigin origin = new CookieOrigin("host", 80, "/foobar", false);
        assertTrue(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMismatch1() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host1");
        cookie.setPath("/");
        CookieOrigin origin = new CookieOrigin("host2", 80, "/", false);
        assertFalse(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMismatch2() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain(".aaaaaaaaa.com");
        cookie.setPath("/");
        CookieOrigin origin = new CookieOrigin(".bbbbbbbb.com", 80, "/", false);
        assertFalse(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMismatch3() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host");
        cookie.setPath("/foobar");
        CookieOrigin origin = new CookieOrigin("host", 80, "/foo", false);
        assertFalse(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMismatch4() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host");
        cookie.setPath("/foobar");
        CookieOrigin origin = new CookieOrigin("host", 80, "/foobar/", false);
        assertTrue(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMatch5() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host");
        cookie.setPath("/foobar/r");
        CookieOrigin origin = new CookieOrigin("host", 80, "/foobar/", false);
        assertFalse(cookiespec.match(cookie, origin));
    }
    
    public void testCookieMismatch6() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        Cookie cookie = new BasicCookie("name", "value");
        cookie.setDomain("host");
        cookie.setPath("/foobar");
        cookie.setSecure(true);
        CookieOrigin origin = new CookieOrigin("host", 80, "/foobar", false);
        assertFalse(cookiespec.match(cookie, origin));
    }
    
    public void testInvalidMatchDomain() throws Exception {
        Cookie cookie = new BasicCookie("name", null); 
        cookie.setDomain("beta.gamma.com");
        cookie.setDomainAttributeSpecified(true);
        cookie.setPath("/");
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("alpha.beta.gamma.com", 80, "/", false); 
        cookiespec.validate(cookie, origin);
        assertTrue(cookiespec.match(cookie, origin));
    }

    /**
     * Tests generic cookie formatting.
     */
    public void testGenericCookieFormatting() throws Exception {
        Header header = new BasicHeader("Set-Cookie", 
            "name=value; path=/; domain=.mydomain.com");
        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("myhost.mydomain.com", 80, "/", false); 
        Cookie[] cookies = cookiespec.parse(header, origin);
        cookiespec.validate(cookies[0], origin);
        Header[] headers = cookiespec.formatCookies(cookies);
        assertNotNull(headers);
        assertEquals(1, headers.length);
        assertEquals("name=value", headers[0].getValue());
    }    

    /**
     * Tests if null cookie values are handled correctly.
     */
    public void testNullCookieValueFormatting() {
        Cookie cookie = new BasicCookie("name", null);
        cookie.setDomain(".whatever.com");
        cookie.setDomainAttributeSpecified(true);
        cookie.setPath("/");
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new BrowserCompatSpec();
        Header[] headers = cookiespec.formatCookies(new Cookie[]{ cookie });
        assertNotNull(headers);
        assertEquals(1, headers.length);
        assertEquals("name=", headers[0].getValue());
    }

    /**
     * Tests generic cookie formatting.
     */
    public void testFormatSeveralCookies() throws Exception {
        Header header = new BasicHeader("Set-Cookie", 
            "name1=value1; path=/; domain=.mydomain.com, name2 = value2 ; path=/; domain=.mydomain.com");
        CookieSpec cookiespec = new BrowserCompatSpec();
        CookieOrigin origin = new CookieOrigin("myhost.mydomain.com", 80, "/", false); 
        Cookie[] cookies = cookiespec.parse(header, origin);
        Header[] headers = cookiespec.formatCookies(cookies);
        assertNotNull(headers);
        assertEquals(1, headers.length);
        assertEquals("name1=value1; name2=value2", headers[0].getValue());
    }    

    public void testKeepCloverHappy() throws Exception {
        new MalformedCookieException(); 
        new MalformedCookieException("whatever"); 
        new MalformedCookieException("whatever", null); 
    }

    public void testInvalidInput() throws Exception {
        CookieSpec cookiespec = new BrowserCompatSpec();
        try {
            cookiespec.parse(null, null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            cookiespec.parse(new BasicHeader("Set-Cookie", "name=value"), null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            cookiespec.validate(null, null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            cookiespec.validate(new BasicCookie("name", null), null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            cookiespec.match(null, null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            cookiespec.match(new BasicCookie("name", null), null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            cookiespec.formatCookies(null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            cookiespec.formatCookies(new BasicCookie[] {});
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }
    
}
