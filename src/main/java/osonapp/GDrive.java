package osonapp;

import java.io.IOException;
import java.util.ArrayList;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GDrive {

    private Drive service;


    public Drive getService() {
        return service;
    }

    public void setService(Drive service) {
        this.service = service;
    }

    public void CreateFolder(String folder) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folder);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        File file = getService().files().create(fileMetadata)
                .setFields("id")
                .execute();
        System.out.println("Folder ID: " + file.getId());
    }

    public void AddFiles(String name, String path) throws IOException {
        File fileMetadata = new File();
        System.out.println(name);
        System.out.println(path);
        fileMetadata.setName(name);
        Path source = Paths.get(name);
        //System.out.println(Files.probeContentType(source));
        java.io.File filePath = new java.io.File(path);
        FileContent mediaContent = new FileContent(Files.probeContentType(source), filePath);
        File file = this.getService().files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        //System.out.println("File ID: " + file.getId());


    }

    public ArrayList ListFiles() throws IOException {
        String pageToken = null;
        ArrayList<File> Files = new ArrayList<>();
        do {
			FileList result = service.files().list()
					.setSpaces("drive")
					.setFields("nextPageToken, files(id, name, size, modifiedTime, mimeType)")
					.setPageToken(pageToken)
					.execute();

			for (File file : result.getFiles()) {
			    Files.add(file);
				System.out.printf("Found file: %s (%s)\n",
						file.getName(), file.getId(), file.getModifiedTime(), file.getMimeType());
			}
			pageToken = result.getNextPageToken();

        } while (pageToken != null);

        return Files;
    }

    public boolean CheckFiles(File file1 , File file2){
        if(file1 == file2){
            return true;
        }
        else{
            return false;
        }
    }

}
