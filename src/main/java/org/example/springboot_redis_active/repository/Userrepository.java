package org.example.springboot_redis_active.repository;

import org.example.springboot_redis_active.model.User;
import org.springframework.data.repository.CrudRepository;

public interface Userrepository extends CrudRepository<User, String> {
}
