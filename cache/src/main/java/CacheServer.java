import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

public class CacheServer {
    private static final String bucketId = "globalBucket";
    public static void main(String[] args) {
        Config config = new Config();
        config.setClusterName("bucketCache");
        Hazelcast.newHazelcastInstance(config);
    }
}
