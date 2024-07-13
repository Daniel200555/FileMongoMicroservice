package com.example.mongofilesmicroservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("shared")
@Getter
@Setter
public class SharedUser {

    public SharedUser() {

    }

    private String owner;
    private String userShare;

}
