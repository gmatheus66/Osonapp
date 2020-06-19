package osonapp;

import java.io.File;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import com.google.api.client.http.FileContent;

public class Monitor {

	private Thread threadMonitor;
	private String path;
	private File arquivos[];
	private ArrayList<File> oldfiles = new ArrayList<>();
	private ArrayList<com.google.api.services.drive.model.File> filesdrive = new ArrayList<>();
	private GDrive drive = new GDrive();
	JsonFile jsonFile = new JsonFile();
	private Drive service;

	public File[] getArquivos() {
		return arquivos;
	}

	public Monitor(String path, Drive service) throws IOException {
		this.setPath(path);
		this.setService(service);
		this.ListFiles();
	}

	public Drive getService() {
		return service;
	}

	public void setService(Drive service) {
		this.service = service;
	}

	public void setArquivos(File[] arquivos) {
		this.arquivos = arquivos;
	}

	public void disconect(File filedeleted) {
		if (filedeleted.exists()) {
			filedeleted.delete();
		}
	}

	public boolean VerifyExistsFolder(String path) {
		File file = new File(path);
		return file.exists();
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void init() throws IOException {
		this.VerifyFolder(this.getPath());

	}

	public void VerifyFolder(String path) {
		File file = new File(path);

		try {
			Boolean isFolder = (Boolean) Files.getAttribute(file.toPath(),
					"basic:isDirectory", NOFOLLOW_LINKS);
			if (!isFolder) {
				throw new IllegalArgumentException("Path: " + path
						+ " is not a folder");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("Escutando na pasta: " + path);
		FileSystem fs = file.toPath().getFileSystem();

		try (WatchService service = fs.newWatchService()) {

			file.toPath().register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

			WatchKey key = null;
			while (true) {
				key = service.take();
				Kind<?> kind = null;
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					kind = watchEvent.kind();
					if (OVERFLOW == kind) {
						continue;
					} else if (ENTRY_CREATE == kind) {
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						System.out.println("Adicionando o Arquivo");
						FileUser fileUser = new FileUser();
						Path dir = (Path) key.watchable();
						Path fullPath = dir.resolve(watchEvent.context().toString());
						com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
						fileMetadata.setName(newPath.toString());
						Path source = Paths.get(newPath.toString());
						java.io.File filePath = new java.io.File(fullPath.toString());
						FileContent mediaContent = new FileContent(Files.probeContentType(source), filePath);
						com.google.api.services.drive.model.File filed = this.getService().files().create(fileMetadata, mediaContent)
								.setFields("id")
								.execute();
						fileUser.setName(newPath.toString());
						fileUser.setPath(fullPath.toString());
						fileUser.setLength(filePath.length());
						fileUser.setId(filed.getId());
						jsonFile.AddtoJson(fileUser);
						System.out.println("Arquivo criado: " + newPath);

					} else if (ENTRY_MODIFY == kind) {
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						Path dir = (Path) key.watchable();
						Path fullPath = dir.resolve(watchEvent.context().toString());
						Path source = Paths.get(newPath.toString());
						FileUser ufile = jsonFile.SearchFile(newPath.toString(), fullPath.toString() );
						com.google.api.services.drive.model.File Gfile = this.getFileDrive(ufile);
						System.out.println( ufile.getLength());
						System.out.println(fullPath.toFile().length());
						if( ufile.getLength() != fullPath.toFile().length() && ufile.getPath().equals(fullPath.toFile().getPath())){
						System.out.println("Adicionando Modificação do arquivo");
							this.getService().files().delete(ufile.getId()).execute();
							//jsonFile.DeleteFile(ufile);
							java.io.File filePath = new java.io.File(fullPath.toString());
							FileContent mediaContent = new FileContent(Files.probeContentType(source), filePath);
							com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
							fileMetadata.setName(newPath.toString());
							com.google.api.services.drive.model.File filed = this.getService().files().create(fileMetadata,mediaContent).execute();
							FileUser fileUser = new FileUser();
							fileUser.setId(filed.getId());
							fileUser.setPath(fullPath.toString());
							fileUser.setLength(fullPath.toFile().length());
							fileUser.setName(newPath.toString());
							jsonFile.AddtoJson(fileUser);
							System.out.println("Arquivo  Modificado: " + newPath);
						}


					} else if (ENTRY_DELETE == kind) {
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						System.out.println("Removendo o Arquivo");
						Path dir = (Path) key.watchable();
						Path fullPath = dir.resolve(watchEvent.context().toString());
						FileUser ufile = jsonFile.SearchFile(newPath.toString(), fullPath.toString() );
						jsonFile.DeleteFile(ufile);
						this.getService().files().delete(ufile.getId()).execute();
						System.out.println("Arquivo deletado: " + newPath);
					}
				}

				if (!key.reset()) {
					break;
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

	}
	public void AddFileGDrive(Path pt, WatchEvent watchEvent) throws IOException {
		WatchKey key = null;
		FileUser fileUser = new FileUser();
		Path dir = (Path) key.watchable();
		Path fullPath = dir.resolve(watchEvent.context().toString());
		com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
		fileMetadata.setName(pt.toString());
		Path source = Paths.get(pt.toString());
		java.io.File filePath = new java.io.File(fullPath.toString());
		FileContent mediaContent = new FileContent(Files.probeContentType(source), filePath);
		com.google.api.services.drive.model.File filed = this.getService().files().create(fileMetadata, mediaContent)
				.setFields("id")
				.execute();
		fileUser.setName(pt.toString());
		fileUser.setPath(fullPath.toString());
		fileUser.setId(filed.getId());
		jsonFile.AddtoJson(fileUser);
	}



	public void ListFiles() throws IOException {
		String pageToken = null;
		ArrayList<com.google.api.services.drive.model.File> Files = new ArrayList<>();
		do {
			FileList result = service.files().list()
					.setSpaces("drive")
					.setFields("nextPageToken, files(id, name)")
					.setPageToken(pageToken)
					.execute();
			this.filesdrive.addAll(result.getFiles());

			pageToken = result.getNextPageToken();

		} while (pageToken != null);

	}
	public com.google.api.services.drive.model.File getFileDrive(FileUser file) throws IOException {
		com.google.api.services.drive.model.File filedrive = service.files().get(file.getId()).execute();
		if (filedrive != null) {
				return filedrive;
		}
		return null;
	}
	public com.google.api.services.drive.model.File SearchFile(ArrayList<com.google.api.services.drive.model.File> files, String filename) {
		for (com.google.api.services.drive.model.File file : files) {
			if (filename.equals(file.getName())) {
				System.out.println(file.getName());
				return file;
			}
		}
		return null;
	}

}