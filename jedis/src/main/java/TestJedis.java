import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @Description
 * @Created by fun4ai
 * @Date 2020/11/18 18:19
 */
public class TestJedis {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("47.108.203.19",6379);
        System.out.println(jedis.ping());
        jedis.flushDB();

        jedis.lpush("pc", "apple","huawei","samsung");
        List<String> list = jedis.lrange("pc", 0, -1);
        for (String l: list) {
            System.out.println(l);
        }


    }
}
