package com.example.mongofilesmicroservice.service.mongo;

import com.example.mongofilesmicroservice.dto.FileData;
import com.example.mongofilesmicroservice.parsing.FileLink;
import com.example.mongofilesmicroservice.service.kafka.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoService.class);

    public static ActionService action(List<FileData> files, String dir) {
        return new ActionService(files, findFileData(files, FileLink.parseLink(dir)), dir);
    }

    public static ActionService action(List<FileData> files, String dir, KafkaService kafkaService) {
        return new ActionService(files, findFileData(files, FileLink.parseLink(dir)), dir, kafkaService);
    }

    public  static ActionService action() {
        System.out.println("In action");
        return new ActionService();
    }

    public static FileData findFileData(List<FileData> fileDataList, List<String> ids) {
        if (ids.isEmpty())
            return null;
        String idToFind = ids.get(0);
        System.out.println(Integer.parseInt(idToFind));
        System.out.println(ids.size());
        for (FileData fileData : fileDataList) {
            if (fileData.getFile_id() == Integer.parseInt(idToFind)) {
                if (ids.size() == 1) {
                    LOGGER.info("File founded");
                    return fileData;
                } else {
                    FileData foundFile = findFileData(fileData.getFiles(), ids.subList(1, ids.size()));
                    if (foundFile != null) {
                        return foundFile;
                    }
                }
            }
        }
        for(FileData fileData: fileDataList) {
            FileData foundFile = findFileData(fileData.getFiles(), ids.subList(1, ids.size()));
            if (foundFile != null) {
                System.out.println(foundFile.getFilename());
                return foundFile;
            }
        }
        LOGGER.info("File not found");
        return null;
    }

}
