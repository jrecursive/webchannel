/*
 * Copyright 2013 John Muellerleile
 *
 * This file is licensed to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package webchannel;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

public class WebChannel extends WebSocketServer {

    private ConcurrentHashMap<WebSocket, WebChannelClient> clients =
        new ConcurrentHashMap<WebSocket, WebChannelClient>();
    JSONParser jsonParser = new JSONParser();
    
	public static void main(String args[]) throws Exception {
		System.out.println("webchannel");

        WebSocketImpl.DEBUG = false;
        int port;
        
        try {
            port = new Integer( args[ 0 ] );
        } catch ( Exception e ) {
            System.out.println( "No port specified. Defaulting to 9003" );
            port = 9003;
        }
        
        System.out.println("starting websocket server");
        new WebChannel(port, new Draft_17() ).start();
	}
	
	// websocket impl
	
    private int port;
    public WebChannel(int port, Draft draft) throws UnknownHostException {
        super(new InetSocketAddress(port), Collections.singletonList(draft));
    }
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
        WebChannelClient client = new WebChannelClient(conn, new String[] { "" });
        clients.put(conn, client);
	    System.out.println("websocket: opened connection");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
	    clients.get(conn).disconnect();
	    clients.remove(conn);
	    System.out.println("websocket: closed connection: " + 
	        code + ": " + 
	        reason + ": " + 
	        remote);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
	    System.out.println("websocket: error: ");
	    ex.printStackTrace();
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
	    try {
    	    System.out.println("onMessage: " + conn + ": " + message);
    	    WebChannelClient client = clients.get(conn);
    	    
            JSONObject msg = (JSONObject) jsonParser.parse(message);
            System.out.println(msg.toString());
            
            if (((String)msg.get("op")).equals("publish")) {
                String ch = (String) msg.get("channel");
                String data = (String) msg.get("message");
                client.publish(ch, data);
                System.out.println(conn + ": published: " + ch + ": " + data);
                
            } else if (((String)msg.get("op")).equals("subscribe")) {
                String ch = (String) msg.get("channel");
                client.psubscribe(ch);
                System.out.println(conn + ": subscribed: " + ch + ": " + msg);
                    
            } else if (((String)msg.get("op")).equals("unsubscribe")) {
                String ch = (String) msg.get("channel");
                client.punsubscribe(ch);
                System.out.println(conn + ": unsubscribed: " + ch + ": " + msg);
                    
            } else {
                
                System.out.println(conn + ": unknown command? " + msg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	@Override
	public void onMessage(WebSocket conn, ByteBuffer blob) {
	   System.out.println("onMessage: " + conn + ": " + blob);
	   conn.send(blob);
	}

	@Override
	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
	   FrameBuilder builder = (FrameBuilder) frame;
	   builder.setTransferemasked(false);
	   conn.sendFrame(frame);
	}
	
	/*
	 * helpers
    */
    
    private String buildOk() {
        JSONObject obj = new JSONObject();
        obj.put("result","ok");
        return obj.toString();
    }
}
