package com.example.mongofilesmicroservice.repository;

import com.example.mongofilesmicroservice.dto.User;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoRepository extends org.springframework.data.mongodb.repository.MongoRepository<User, String> {
    User findByNickname(String nickname);
}
