package br.com.vilela.previa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

@ManagedBean
@SessionScoped
public class FileUploadView {

	private LeitorPrevia leitor;

	private UploadedFile file;

	private String fileNameOutput;

	private StreamedContent fileOutput;

	private File fileInput;

	private boolean emProcessamento = false;
	//@ManagedProperty("#{param.fileNameInput}")
	private String fileNameInput;

	private String time;

	public void handleFileUpload(FileUploadEvent event) {
		FacesMessage msg = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
		try {
			file = event.getFile();
			upload();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public StreamedContent getFileOutput() {
		if (fileNameOutput != null) {
			System.out.println("download: " + fileNameOutput);
//			InputStream stream = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream(fileNameOutput);
			InputStream stream = null;
			try {
				stream = new FileInputStream(fileNameOutput);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			fileOutput = new DefaultStreamedContent(stream, "application/zip", "previa.zip");
			System.out.println(fileOutput);
		}
		return fileOutput;
	}

	private File saidaFile;

	public File getSaidaFile() {
		return saidaFile;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public void download() {
		System.out.println("download: " + fileNameOutput);
		fileOutput = new DefaultStreamedContent(
				FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(fileNameOutput),
				"application/zip", "previa.zip");

		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Download "));

	}

	public String upload() throws IOException {
		fileInput = saveUpload();
		this.fileNameInput = fileInput.getAbsolutePath();
		return null;
	}

	public void setEmProcessamento(boolean emProcessamento) {
		this.emProcessamento = emProcessamento;
	}

	public void processa() throws IOException {
		System.out.println("Em processamento");
		emProcessamento = true;

		if (fileNameInput != null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						File saida = createTempDir();
						saidaFile = new File(createTempDir(), "previa.zip");
						leitor = new LeitorPrevia();
						leitor.run(fileNameInput, saida.getAbsolutePath());

						ZipUtils appZip = new ZipUtils(saida.getAbsolutePath());
						appZip.generateFileList(saida);
						appZip.zipIt(saidaFile.getAbsolutePath());
						fileNameOutput = saidaFile.getAbsolutePath();
					} catch (Exception e) {
						System.out.println(e);
					} finally {
						emProcessamento = false;
					}
				}
			}).start();

		}

	}

	private File saveUpload() throws IOException {
		File result = null;
		if (file != null) {
			InputStream in = file.getInputStream();
			result = new File(createTempDir(), file.getFileName());
			FileOutputStream out = new FileOutputStream(result);
			byte buffer[] = new byte[2048];
			int i = 0;
			while ((i = in.read(buffer)) > 0) {
				out.write(buffer, 0, i);
			}
			out.close();
		}
		return result;
	}

	private File createTempDir() throws IOException {
		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		File newTempDir;
		final int maxAttempts = 9;
		int attemptCount = 0;
		do {
			attemptCount++;
			if (attemptCount > maxAttempts) {
				throw new IOException("The highly improbable has occurred! Failed to "
						+ "create a unique temporary directory after " + maxAttempts + " attempts.");
			}
			String dirName = UUID.randomUUID().toString();
			newTempDir = new File(sysTempDir, dirName);
		} while (newTempDir.exists());

		if (newTempDir.mkdirs()) {
			return newTempDir;
		} else {
			throw new IOException("Failed to create temp dir named " + newTempDir.getAbsolutePath());
		}
	}

	public void finalizar() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
	}
	
	public boolean getEmProcessamento() {
		System.out.println(new Date() + " " + emProcessamento);
		return emProcessamento;
	}

	public File getFileInput() {
		return fileInput;
	}

	public String getFileNameOutput() {
		return fileNameOutput;
	}

	public void setFileNameOutput(String fileNameOutput) {
		this.fileNameOutput = fileNameOutput;
	}

	public String getFileNameInput() {
		return fileNameInput;
	}

	public void setFileNameInput(String fileNameInput) {
		this.fileNameInput = fileNameInput;
	}

	public String getTime() {
		return Calendar.getInstance().getTime().toString();
	}

	public void setTime(String time) {
		this.time = time;
	}

}