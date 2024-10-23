package org.example.springboot_redis_active.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("users")
@Data
public class User {
    @Id
    private String id;
    private String name;
}


