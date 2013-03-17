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
                try {
                    sub.psubscribe(pubsub, patterns);
                } catch (redis.clients.jedis.exceptions.JedisConnectionException jce) {
                    // go sweetly
                }
            }
        });
        t.start();
    }
    
    final JedisPubSub pubsub = new JedisPubSub() {
        public void onMessage(String channel, String message) {
            send(buildMsg(channel, message));
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
            send(buildMsg(channel, message));
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
    
    private void send(String message) {
        try {
            synchronized(conn) {
                if (conn.isFlushAndClose()) return;
                conn.send(message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
