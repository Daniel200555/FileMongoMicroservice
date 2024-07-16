package com.example.mongofilesmicroservice.service.mongo;

import com.example.mongofilesmicroservice.dto.FileData;
import com.example.mongofilesmicroservice.dto.SharedUser;
import com.example.mongofilesmicroservice.dto.User;
import com.example.mongofilesmicroservice.format.GetFormat;
import com.example.mongofilesmicroservice.parsing.FileLink;
import com.example.mongofilesmicroservice.service.kafka.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class ActionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionService.class);

    private KafkaService kafkaService;
    private List<FileData> files;
    private String dir;
    private FileData file;

    public ActionService(List<FileData> files, FileData file, String dir) {
        this.files = files;
        this.dir = dir;
        this.file = file;
    }

    public ActionService(List<FileData> files, FileData file, String dir, KafkaService kafkaService) {
        this.files = files;
        this.dir = dir;
        this.file = file;
        this.kafkaService = kafkaService;
    }

    public ActionService() {

    }

    public List<FileData> addFiles(List<FileData> fs) {
        LOGGER.info(String.format("Added file"));
        for (FileData f: fs) {
            if (FileLink.parseLink(dir).isEmpty()) {
                this.files.add(f);
            } else {
                FileData targetFileData = file;
                System.out.println(targetFileData.getFilename());
                if (targetFileData.getFiles() == null)
                    targetFileData.setFiles(new ArrayList<>());
                targetFileData.getFiles().add(f);
            }
            LOGGER.info(String.format("Added file with name -> %s", f.getFilename()));
        }
        return files;
    }

    public List<FileData> filesToUpload(MultipartFile[] multipartFiles, String dir, String nickname) {
        List<FileData> result = new ArrayList<>();
        System.out.println("In method filesToUpload");
        for (MultipartFile multipartFile: multipartFiles) {
            if (!FileLink.parseLink(dir).isEmpty())
                result.add(addFile(multipartFile, dir, nickname, file.getFiles()));
            else
                result.add(addFile(multipartFile, dir, nickname, files));

        }
        return result;
    }

    public void share(User user, FileData file, String shareNickname, String owner) {
        addShared(user, file);
        setFileShared(shareNickname, owner);
    }

    public User addShared(User user, FileData file) {
        List<FileData> shared = user.getSharedFiles();
        shared.add(file);
        user.setSharedFiles(shared);
        return user;
    }

    public List<FileData> setFileShared(String sharedName, String owner) {
        List<SharedUser> shared = this.file.share();
        SharedUser temp = new SharedUser();
        temp.setOwner(owner);
        temp.setUserShare(sharedName);
        shared.add(temp);
        this.file.setShare(shared);
        return this.files;
    }

//    public void add(MultipartFile f, String dir, String nickname) {
//        if (FileLink.parseLink(dir).isEmpty()) {
//            this.files.add(addFile(f, dir, nickname, this.files));
//        } else {
//            FileData targetFileData = file;
//            System.out.println(targetFileData.getFilename());
//            if (targetFileData.getFiles() == null)
//                targetFileData.setFiles(new ArrayList<>());
//            FileData uploadedFile = addFile(f, dir, nickname, targetFileData.getFiles());
//            targetFileData.getFiles().add(uploadedFile);
//        }
//        LOGGER.info(String.format("Added file -> %s", f.getOriginalFilename()));
//    }

    public List<FileData> add(String name, String dir, String nickname) {
        LOGGER.info(String.format("Add directory with name -> %s", name));
        if (file == null) {
            this.files.add(addDirectory(name, dir, nickname, this.files));
            return this.files;
        } else {
            FileData targetFileData = file;
            System.out.println(targetFileData.getFilename());
            if (targetFileData.getFiles() == null)
                targetFileData.setFiles(new ArrayList<>());
            FileData uploadedFile = addDirectory(name, dir, nickname, targetFileData.getFiles());
            targetFileData.getFiles().add(uploadedFile);
            return this.files;
        }
    }

    public FileData addFile(MultipartFile f, String dir, String nickname, List<FileData> fs) {
        LOGGER.info(String.format("Add file with name -> %s", f.getOriginalFilename()));
        long id = idForFile(fs);
        String d = dir + (id) + "." + GetFormat.getType(f.getOriginalFilename(), '.');
        return new FileData.Builder()
                .setFiles(new ArrayList<>())
                .setFileName(GetFormat.getOnlyName(f.getOriginalFilename()))
                .setId(id)
                .setDir(d)
                .isFile(GetFormat.checkIsFile(f.getOriginalFilename()))
                .setFormat(GetFormat.getType(f.getOriginalFilename(), '.'))
                .setType(GetFormat.formatFile(GetFormat.getType(f.getOriginalFilename(), '.')))
                .build();
    }

    private FileData addDirectory(String name, String dir, String nickname, List<FileData> fs) {
        LOGGER.info(String.format("Add directory with name -> %s", name));
        long id = idForFile(fs);
        String d = nickname + dir + (id) + "/";
        this.kafkaService.createDir(d);
        return new FileData.Builder()
                .setFiles(new ArrayList<>())
                .setFileName(name)
                .setId(id)
                .setDir(d)
                .isFile(false)
                .setFormat("NULL")
                .setType("NULL")
                .build();
    }

    public List<FileData> remove() {
        LOGGER.info(String.format("Remove file with name -> %s", this.file.getFilename()));
        List<String> ids = FileLink.parseLink(dir);
        if (ids.size() == 0) {
            this.files.remove(file);
            return this.files;
        } else {
            String id = ids.get(ids.size() - 1);
        ids.remove(ids.size() - 1);
        if (ids.isEmpty()) {
            List<String> i = new ArrayList<>();
            i.add(id);
            this.files.remove(MongoService.findFileData(files, i));
            return this.files;
        } else {
            FileData targetFileData = MongoService.findFileData(this.files, ids);
            if (targetFileData == null)
                return null;
            if (targetFileData.getFiles() == null)
                targetFileData.setFiles(new ArrayList<>());
            targetFileData.getFiles().remove(file);
            return this.files;
        }
    }
    }

    public List<FileData> showFile() {
        if (file == null) {
            LOGGER.info(String.format("File is null -> %s", files.size()));
            return files;
        }
        else {
            LOGGER.info("file is not null");
            return file.getFiles();
        }
    }

    public List<FileData> findFile(List<FileData> files, String name){
        List<FileData> result = new ArrayList<>();;
        for (FileData f : files) {
            if (f.getFilename().toLowerCase().contains(name.toLowerCase()))
                result.add(f);
            if (f.getFiles() != null && !f.getFiles().isEmpty())
                result.addAll(findFile(f.getFiles(), name));
        }
        LOGGER.info(String.format("Find files with name -> %s, and size of files -> %s", name, result.size()));
        return result;
    }

    public List<FileData> rename(String newName) {
        LOGGER.info(String.format("Rename file with original name -> %s, to name -> %s", this.file.getFilename(), newName));
        this.file.setFilename(newName + "." + this.file.getFormat());
        return this.files;
    }

    public List<FileData> star() {
        LOGGER.info(String.format("Star file with name -> %s", this.file.getFilename()));
        this.file.setStar(true);
        return this.files;
    }

    public List<FileData> unStar() {
        LOGGER.info(String.format("Un star file with name -> %s", this.file.getFilename()));
        this.file.setStar(false);
        return this.files;
    }

    private boolean checkIfIdExist(long id, List<FileData> fs) {
        for (FileData f : fs) {
            if (f.getFile_id() == id)
                return true;
        }
        return false;
    }

    public long idForFile(List<FileData> fs) {
        if (fs == null) {
            return 1;
        }
        long id = fs.size();
        boolean b = true;
        if (fs.size() != 0) {
            while (b) {
                id = id + 1;
                b = checkIfIdExist(id, fs);
            }
        } else {
            return 1;
        }
        return id;
    }

}
