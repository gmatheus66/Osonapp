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
    public JsonFile() {
        try{
            File json = new File(path);
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
            BufferedReader br = new BufferedReader(new FileReader(path));
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
            BufferedReader br = new BufferedReader(new FileReader(path));
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
            BufferedReader br = new BufferedReader(new FileReader(path));
            Type userListType = new TypeToken<ArrayList<FileUser>>(){}.getType();
            ArrayList<FileUser> fileArrayList = gson.fromJson(br, userListType);
            for (FileUser file: fileArrayList) {
                if(file.getName().equals(userfile.getName()) && file.getPath().equals(userfile.getPath()) && file.getId() == file.getId()){
                    fileArrayList.remove(file);
                }
            }
            String userjson = gson.toJson(fileArrayList);
            FileWriter writer =  new FileWriter(path);
            writer.write(userjson);
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
