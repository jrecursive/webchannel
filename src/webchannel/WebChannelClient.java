package webchannel;

import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.java_websocket.WebSocket;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

public class WebChannelClient {
    final private Jedis sub;
    final private Jedis pub;
    final private WebSocket conn;
    
    public WebChannelClient(WebSocket conn, final String[] patterns) {
        pub = new Jedis("localhost");
        sub = new Jedis("localhost");
        this.conn = conn;
        
        Thread t = new Thread(new Runnable() {
            public void run() {
                sub.psubscribe(pubsub, patterns);
            }
        });
        t.start();
    }
    
    final JedisPubSub pubsub = new JedisPubSub() {
        public void onMessage(String channel, String message) {
            conn.send(buildMsg(channel, message));
            System.out.println("pubsub: onMessage: " + channel + ": " + message);
        }
    
        public void onSubscribe(String channel, int subscribedChannels) {
        }
    
        public void onUnsubscribe(String channel, int subscribedChannels) {
        }
    
        public void onPSubscribe(String pattern, int subscribedChannels) {
        }
    
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }
    
        public void onPMessage(String pattern, String channel,
                String message) {
            conn.send(buildMsg(channel, message));
            System.out.println("pubsub: onPMessage: " + 
                pattern + ": " + 
                channel + ": " + message);
        }
    };
    
    public void publish(String channel, String message) {
        pub.publish(channel, message);
    }
    
    public void subscribe(String channel) {
        pubsub.subscribe(channel);
    }
    
    public void unsubscribe(String channel) {
        pubsub.unsubscribe(channel);
    }

    public void psubscribe(String channel) {
        pubsub.psubscribe(channel);
    }

    public void punsubscribe(String channel) {
        pubsub.punsubscribe(channel);
    }
    
    public void disconnect() {
        sub.disconnect();
        pub.disconnect();
    }
    
    /*
     * helpers
    */
    
    private String buildMsg(String channel, String message) {
        JSONObject obj = new JSONObject();
        obj.put("channel", channel);
        obj.put("message", message);
        return obj.toString();
    }

}
