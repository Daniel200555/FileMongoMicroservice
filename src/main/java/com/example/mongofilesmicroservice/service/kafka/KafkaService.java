package com.example.mongofilesmicroservice.service.kafka;

import com.example.mongofilesmicroservice.dto.FileData;
import com.example.mongofilesmicroservice.dto.User;
import com.example.mongofilesmicroservice.repository.MongoRepository;
import com.example.mongofilesmicroservice.service.mongo.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class KafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaFile;

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @KafkaListener(topics = "databaseuser", groupId = "group-id")
    public void consume(String messages) throws IOException {
        LOGGER.info(String.format("Message received -> %s", messages));
        addUser(messages);
    }

    public void  createFile(String dir, byte[] file) throws IOException {
        String UID = UUID.randomUUID().toString();
        int chunkSize = 1024 * 1024 * 10;
        int numChunks = (int) Math.ceil((double) file.length / chunkSize);
        int start, end;
        byte[] temp;
        LOGGER.info("Sending file");
        for (int i = 0; i < numChunks; i++) {
            start = i * chunkSize;
            end = Math.min(start + chunkSize, file.length);
            temp = Arrays.copyOfRange(file, start, end);
            Map<String, Object> chunk = new HashMap<>();
            chunk.put("dir", dir);
            chunk.put("temp", temp);
            chunk.put("index", i);
            chunk.put("totalChunk", numChunks);
            chunk.put("uid", UID);
            kafkaFile.send("createfile", chunk);
        }
    }

    public void deleteFile(String dir) {
        LOGGER.info(String.format("Send delete file with dir -> %s", dir));
        this.kafkaTemplate.send("deletefile", dir);
    }

    public void createDir(String dir) {
        LOGGER.info(String.format("Send create folder with dir -> %s", dir));
        this.kafkaTemplate.send("createdir", dir);
    }


    private void addUser(String name) {
        List<FileData> files = new ArrayList<>();
        List<FileData> shared = new ArrayList<>();

        User user = new User();
        user.setFiles(files);
        user.setSharedFiles(shared);
        user.setNickname(name);
        user.setId(sequenceGeneratorService.getNextSequence("database_sequence"));
        mongoRepository.save(user);
        LOGGER.info(String.format("Save user in mongo db with nickname -> %s", name.toString()));
    }

}
