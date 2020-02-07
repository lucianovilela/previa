package br.com.vilela.previa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
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

 
    public UploadedFile getFile() {
        return file;
    }
 
    public void setFile(UploadedFile file) {
        this.file = file;
    }
 

 
    public void upload() throws  IOException {
    	
    	
        if (file != null) {
        	
        	File arquivo = saveUpload();
        	File saida = createTempDir();
        	File saidaFile = new File(createTempDir(), "previa.zip");
        	leitor = new LeitorPrevia();
            leitor.run(arquivo.getAbsolutePath(), saida.getAbsolutePath());
            
            ZipUtils appZip = new ZipUtils(saida.getAbsolutePath());
            appZip.generateFileList(saida);
            appZip.zipIt(saidaFile.getAbsolutePath());

            DefaultStreamedContent fileOutput = new DefaultStreamedContent();
            fileOutput.setName("previa.zip");
            fileOutput.setContentType("application/zip");
            fileOutput.setStream( FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream( saidaFile.getAbsolutePath()));
                  
            
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Arquivo processado "+ fileOutput.getContentLength() )	);

        }
    }
    
    private File saveUpload() throws IOException {
    	File result = null;
    	if(file != null) {
    		InputStream in = file.getInputstream();
    		result = new File(createTempDir(), file.getFileName());
    		FileOutputStream out = new FileOutputStream(result);
    		byte buffer[] = new byte[2048];
    		int i=0;
    		while((i=in.read(buffer)) > 0 ) {
    			out.write(buffer, 0, i);
    		}
    		out.close();
    	}
    	return result;
    }
    
    private StreamedContent fileOutput;
    
        
 
    public StreamedContent getFileOutput() {
        return fileOutput;
    }    
    
    
    private  File createTempDir() throws IOException
    {
        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        File newTempDir;
        final int maxAttempts = 9;
        int attemptCount = 0;
        do
        {
            attemptCount++;
            if(attemptCount > maxAttempts)
            {
                throw new IOException(
                        "The highly improbable has occurred! Failed to " +
                        "create a unique temporary directory after " +
                        maxAttempts + " attempts.");
            }
            String dirName = UUID.randomUUID().toString();
            newTempDir = new File(sysTempDir, dirName);
        } while(newTempDir.exists());

        if(newTempDir.mkdirs())
        {
            return newTempDir;
        }
        else
        {
            throw new IOException(
                    "Failed to create temp dir named " +
                    newTempDir.getAbsolutePath());
        }
    }     

}