package webchannel;

import java.util.*;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

public class WebChannelClient {
    List<Jedis> connections = new ArrayList<Jedis>();
    
    //Jedis jedis = new Jedis("localhost");
    
}
