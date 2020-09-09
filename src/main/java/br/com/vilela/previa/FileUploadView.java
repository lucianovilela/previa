package br.com.vilela.previa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@ManagedBean
@SessionScoped
public class FileUploadView {

	private LeitorPrevia leitor;

	private UploadedFile file;

	private String fileNameOutput;
	private StreamedContent fileOutput;
	
	private File fileInput;

	private boolean emProcessamento=false;
	
	public StreamedContent getFileOutput() {
		if (fileNameOutput != null) {
			System.out.println("download: " + fileNameOutput);
//			InputStream stream = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream(fileNameOutput);
			InputStream stream=null;
			try {
				stream = new FileInputStream(fileNameOutput);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			fileOutput = new DefaultStreamedContent(
					stream,
					"application/zip", "previa.zip");
			System.out.println(fileOutput);
		}
		return fileOutput;
	}

	public String getFileNameOutput() {
		return fileNameOutput;
	}

	public void setFileNameOutput(String fileNameOutput) {
		this.fileNameOutput = fileNameOutput;
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

	public String getFileOutputName() {
		return saidaFile == null ? "sem arquivo de saida" : saidaFile.getAbsolutePath();

	}
	
	public void upload() throws IOException {
		fileInput  = saveUpload();
	}

	public void processa() throws IOException {
		emProcessamento=true;
		try {
			if (file != null) {
	
				
				File saida = createTempDir();
				saidaFile = new File(createTempDir(), "previa.zip");
				leitor = new LeitorPrevia();
				leitor.run(fileInput.getAbsolutePath(), saida.getAbsolutePath());
	
				ZipUtils appZip = new ZipUtils(saida.getAbsolutePath());
				appZip.generateFileList(saida);
				appZip.zipIt(saidaFile.getAbsolutePath());
				fileNameOutput = saidaFile.getAbsolutePath();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Arquivo processado "));
	
			}
		}finally {
			emProcessamento=false;
		}
		
	}

	private File saveUpload() throws IOException {
		File result = null;
		if (file != null) {
			InputStream in = file.getInputstream();
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

	public boolean getEmProcessamento() {
		return emProcessamento;
	}

	public void setEmProcessamento(boolean emProcessamento) {
		this.emProcessamento = emProcessamento;
	}

}