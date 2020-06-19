package osonapp;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class JsonFile {
    private String path = "files.json";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public JsonFile() {
        try{
            File json = new File(this.getPath());
            if(json.createNewFile()){
                System.out.println("File: " + json.getName());
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void AddtoJson(FileUser objuser) throws IOException {

        try{
            Gson gson = new Gson();
            Type userListType = new TypeToken<ArrayList<FileUser>>(){}.getType();
            BufferedReader br = new BufferedReader(new FileReader(this.getPath()));
            ArrayList<FileUser> fileArrayList = gson.fromJson(br, userListType);
            fileArrayList.add(objuser);
            String userjson = gson.toJson(fileArrayList);
            FileWriter writer =  new FileWriter(path);
            writer.write(userjson);
            writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public FileUser SearchFile(String name, String pathfile){
        try{
            Gson gson = new Gson();
            BufferedReader br = new BufferedReader(new FileReader(this.getPath()));
            Type userListType = new TypeToken<ArrayList<FileUser>>(){}.getType();
            ArrayList<FileUser> fileArrayList = gson.fromJson(br, userListType);

            for (FileUser file: fileArrayList) {
                if(file.getName().equals(name) && file.getPath().equals(pathfile)){
                    return file;
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public void DeleteFile(FileUser userfile){
        try{
            Gson gson = new Gson();
            BufferedReader br = new BufferedReader(new FileReader(this.getPath()));
            Type userListType = new TypeToken<ArrayList<FileUser>>(){}.getType();
            ArrayList<FileUser> fileArrayList = gson.fromJson(br, userListType);
            ArrayList<FileUser> fileArrayListcopy = gson.fromJson(br, userListType);
            for (int t = 0; t < fileArrayList.size(); t++){
                if(fileArrayList.get(t).getName().equals(userfile.getName()) && fileArrayList.get(t).getName().equals(userfile.getName()) ){
                    fileArrayList.remove(t);
                    String userjson = gson.toJson(fileArrayList);
                    FileWriter writer =  new FileWriter(this.getPath());
                    writer.write(userjson);
                    writer.close();
                }
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
