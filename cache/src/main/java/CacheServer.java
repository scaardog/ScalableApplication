import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.grid.RecoveryStrategy;

import java.time.Duration;

public class CacheServer {
    private static final String bucketId = "globalBucket";

    public static void main(String[] args) {
        Config config = new Config();
        config.setClusterName("bucketCache");
        Bucket4j.extension(io.github.bucket4j.grid.hazelcast.Hazelcast.class).builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofSeconds(1))))
                .build(Hazelcast.newHazelcastInstance(config).getMap("bucket"), bucketId, RecoveryStrategy.RECONSTRUCT);
    }
}
