import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;

public class CacheServer {
    public static void main(String[] args) {
        Config config = new Config();
        config.setClusterName("bucketCache");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        IAtomicReference<Integer> bucket = instance.getCPSubsystem().getAtomicReference("bucket");
        bucket.set(5);
    }
}
