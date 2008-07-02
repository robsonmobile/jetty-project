// ========================================================================
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.servlet;

import junit.framework.TestCase;

public abstract class AbstractSessionTest extends TestCase
{
    public static final String __host1 = "localhost";
    public static final String __host2 = __host1;
    public static final String __port1 = "8010";
    public static final String __port2 = "8011";
    SessionTestServer _server1;
    SessionTestServer _server2;

    public abstract SessionTestServer newServer1 ();
    
    public abstract SessionTestServer newServer2();
    
    public void setUp () throws Exception
    {
        _server1 = newServer1();
        _server2 = newServer2();
        _server1.start();      
        _server2.start();
    }
    
    public void tearDown () throws Exception
    {
        if (_server1 != null)
            _server1.stop();
        if (_server2 != null)
            _server2.stop();
        
        _server1=null;
        _server2=null;
    }
    
    
    public void testSessions () throws Exception
    {
        SessionTestClient client1 = new SessionTestClient("http://"+__host1+":"+__port1);
        SessionTestClient client2 = new SessionTestClient("http://"+__host2+":"+__port2);
        // confirm that user has no session
        assertFalse(client1.send("/contextA", null));
        String cookie1 = client1.newSession("/contextA");
        assertNotNull(cookie1);
        System.err.println("cookie1: " + cookie1);
        
        // confirm that client2 has the same session attributes as client1
        assertTrue(client1.setAttribute("/contextA", cookie1, "foo", "bar"));        
        assertTrue(client2.hasAttribute("/contextA", cookie1, "foo", "bar"));
        
        // confirm that /contextA would share same sessionId as /contextB        
        assertTrue(client1.send("/contextA/dispatch/forward/contextB", cookie1));        
        assertTrue(client2.send("/contextA/dispatch/forward/contextB", cookie1));
        assertTrue(client1.send("/contextB", cookie1));
        
        String cookie2 = client2.newSession("/contextA");
        assertNotNull(cookie2);
        System.err.println("cookie2: " + cookie2);
        
        // confirm that client1 has same session attributes as client2
        assertTrue(client2.setAttribute("/contextA", cookie2, "hello", "world"));
        assertTrue(client1.hasAttribute("/contextA", cookie2, "hello", "world"));
                
        // confirm that /contextA would share same sessionId as /contextB        
        assertTrue(client1.send("/contextA/dispatch/forward/contextB", cookie2));        
        assertTrue(client2.send("/contextA/dispatch/forward/contextB", cookie2));
        assertTrue(client1.send("/contextB", cookie2));
    }
}