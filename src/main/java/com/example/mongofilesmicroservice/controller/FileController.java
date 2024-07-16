package com.example.mongofilesmicroservice.controller;
import com.example.mongofilesmicroservice.dto.FileData;
import com.example.mongofilesmicroservice.dto.User;
import com.example.mongofilesmicroservice.repository.MongoRepository;
import com.example.mongofilesmicroservice.service.mongo.MongoService;
import com.example.mongofilesmicroservice.service.kafka.KafkaService;
import com.example.mongofilesmicroservice.service.mongo.sequence.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/mongo")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private MongoService mongoService;

    public FileController() {

    }

    @PostMapping(value = "/add/user")
    public User addUser(@RequestParam String nickname) {
        List<FileData> files = new ArrayList<>();
        List<FileData> shared = new ArrayList<>();

        User user = new User();
        user.setFiles(files);
        user.setSharedFiles(shared);
        user.setNickname(nickname);
        user.setId(sequenceGeneratorService.getNextSequence("database_sequence"));
        return mongoRepository.save(user);
    }

    @PostMapping( value="/add/file")
    public List<FileData> updateUser(@RequestParam("nickname") String nickname, @RequestParam("dir") String dir, @RequestParam("file") MultipartFile[] file) throws IOException {
        LOGGER.info(String.format("Request add file to user with nickname -> %s", nickname));
        User usertemp = mongoRepository.findByNickname(nickname);
        List<FileData> files = usertemp.getFiles();
        List<FileData> filesToUpload = mongoService.action(usertemp.getFiles(), dir).filesToUpload(file, dir, nickname);
        files = mongoService.action(usertemp.getFiles(), dir, this.kafkaService).addFiles(filesToUpload);
        usertemp.setFiles(files);
        int i = 0;
        for (FileData fileData: filesToUpload) {
            kafkaService.createFile(fileData.getDir(), file[i].getBytes());
            i++;
        }
        return mongoRepository.save(usertemp).getFiles();
    }

//    @PostMapping("/mngo/add/shared")
//    public User addSharedFile(@RequestParam("nickname") String nickname, @RequestBody  FileData file) {
//        return mongoRepository.save(MongoService.action().addShared(mongoRepository.findByNickname(nickname), file));
//    }

    @PostMapping("/add/folder")
    public List<FileData> addFolder(@RequestParam("nickname") String nickname, @RequestParam("dir") String dir, @RequestParam("name") String name) {
        User usertemp = mongoRepository.findByNickname(nickname);
        List<FileData> files = mongoService.action(usertemp.getFiles(), dir, kafkaService).add(name, dir, nickname);
        usertemp.setFiles(files);
        return mongoRepository.save(usertemp).getFiles();
    }

    @GetMapping(value = "/get")
    public boolean getUser(@RequestParam String name) {
        return (mongoRepository.findByNickname(name) != null) ? true : false;
//        return mongoRepository.findByNickname(name);
    }

    @GetMapping("/delete")
    public void deleteFile(@RequestParam("nickname") String nickname, @RequestParam("dir") String dir) {
        LOGGER.info(String.format("Request delete file from user with nickname -> %s", nickname));
        User user = mongoRepository.findByNickname(nickname);
        List<FileData> files = mongoService.action(user.getFiles(), dir).remove();
        user.setFiles(files);
        kafkaService.deleteFile(dir);
        mongoRepository.save(user);
    }

    @PostMapping("/rename")
    public User renameFile(@RequestParam("nickname") String nickname, @RequestParam("dir") String dir, @RequestParam("name") String name) {
        User user = mongoRepository.findByNickname(nickname);
        List<FileData> files = mongoService.action(user.getFiles(), dir).rename(name);
        user.setFiles(files);
        return mongoRepository.save(user);
    }

    @GetMapping("/show")
    public List<FileData> showFiles(@RequestParam("nickname") String nickname, @RequestParam("dir") String dir) {
        LOGGER.info(String.format("Request to show files from user with nickname -> %s", nickname));
        User user = mongoRepository.findByNickname(nickname);
        return mongoService.action(user.getFiles(), dir).showFile();
    }

    @GetMapping("/find")
    public List<FileData> findFiles(@RequestParam("nickname") String nickname, @RequestParam("name") String name) {
        LOGGER.info(String.format("Request to find files from user with nickname -> %s", nickname));
        User user = mongoRepository.findByNickname(nickname);
        return mongoService.action().findFile(user.getFiles(), name);
    }

    @GetMapping("/star")
    public List<FileData> starFile(@RequestParam("nickname") String nickname, @RequestParam("dir") String dir) {
        User user = mongoRepository.findByNickname(nickname);
        return mongoService.action(user.getFiles(), dir).star();
    }

    @GetMapping("/unstar")
    public List<FileData> unStarFile(@RequestParam("nickname") String nickname, @RequestParam("dir") String dir) {
        User user = mongoRepository.findByNickname(nickname);
        return mongoService.action(user.getFiles(), dir).unStar();
    }

}
