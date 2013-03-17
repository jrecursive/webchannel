package webchannel;

import org.json.simple.JSONObject;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.*;

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
    
	public static void main(String args[]) throws Exception {
		System.out.println("webchannel");
		
		// json object
		
        JSONObject obj=new JSONObject();
        obj.put("name","foo");
        obj.put("num",new Integer(100));
        obj.put("balance",new Double(1000.21));
        obj.put("is_vip",new Boolean(true));
        obj.put("nickname",null);
        System.out.println(obj);
        
        // websocket server
        
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
	   System.out.println("websocket: opened connection");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
	   System.out.println("websocket: closed connection");
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
	   System.out.println("websocket: error: ");
	   ex.printStackTrace();
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
	   System.out.println("onMessage: " + conn + ": " + message);
	   conn.send(message);
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
}
