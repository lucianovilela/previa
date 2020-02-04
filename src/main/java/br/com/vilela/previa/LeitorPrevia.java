package br.com.vilela.previa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;



public class LeitorPrevia {


	private File arquivoEntrada;
	private File dirSaida = new File("/home/luciano/temp/previa");
	private final int INICIAL = 1;
	private final int FINAL = 2;
	private int status = INICIAL;
	private Pattern ptRegional;
	private Pattern ptFinaliza;
	// ProgressBarDemo newContentPane;
	// JFrame frame;
	PDDocument previa;

	public void run(String arqEnt) throws IOException, COSVisitorException {

		arquivoEntrada = new File(arqEnt);
		// ptRegional = Pattern.compile(
		// "^LOTACAO:\\s+\\d+\\s+\\-\\s+([\\w\\s\\/\\,\\.\\-]+)\\sMUNICIPIO.*$",
		// Pattern.MULTILINE);

		ptRegional = Pattern.compile(".*LOTAC\\s-\\s\\d+\\s+([\\w\\s\\/\\,\\.\\-]+)$", Pattern.MULTILINE);
		ptFinaliza = Pattern.compile("^.*INATIVOS.*CEDIDOS.*REQUISITADOS.*$", Pattern.MULTILINE);
		// ptFinaliza = Pattern.compile("^.*TOTAL DA SOLICITACAO POR LOTACAO.*$",
		// Pattern.MULTILINE);

		previa = PDDocument.load(arquivoEntrada);
		List pagList = previa.getDocumentCatalog().getAllPages();
		PDFTextStripper ts = new PDFTextStripper();
		String regional = "";
		int pgInicial = 0;
		int pgFinal = 0;

		// createAndShowGUI(previa.getNumberOfPages());
		for (int i = 1; i < previa.getNumberOfPages(); i++) {
			// newContentPane.setValue(i);
			ts.setStartPage(i);
			ts.setEndPage(i);
			String texto = ts.getText(previa);
			switch (status) {
			case INICIAL:
				Matcher m = ptRegional.matcher(texto);
				if (m.find()) {
					regional = m.group(1).trim();
					pgInicial = i;
					status = FINAL;
				}

				// break;
			case FINAL:
				Matcher mf = ptFinaliza.matcher(texto);
				if (mf.find()) {
					pgFinal = i;
					geraArquivo(pagList, regional, pgInicial, pgFinal);
					status = INICIAL;
				}
				break;

			}

		}

		previa.close();

	}

	private void geraArquivo(List doc, String regional, int pgInicial, int pgFinal)
			throws IOException, COSVisitorException {
		regional = regional.replaceAll("[\\s\\/\\,\\-]", "_");
		PDDocument newDoc = new PDDocument();
		for (int i = pgInicial; i <= pgFinal; i++) {
			PDPage pgOld = (PDPage) doc.get(i - 1);
			PDPage pg = newDoc.importPage(pgOld);
			pg.setCropBox(pgOld.findCropBox());
			pg.setMediaBox(pgOld.findMediaBox());
			pg.setRotation(pgOld.findRotation());
			// pg.setMediaBox(PDPage.PAGE_SIZE_A4);

		}
		if (new File(dirSaida.getAbsolutePath() + File.separator + regional + ".pdf").exists()) {
			regional = regional + "_2";
		}
		FileOutputStream s = new FileOutputStream(dirSaida.getAbsolutePath() + File.separator + regional + ".pdf");
		// EntradaPrevia entPrevia = new EntradaPrevia();
		// entPrevia.setUnidadePrevia(regional);
		// entPrevia.setPaginaInicial(pgInicial);
		// entPrevia.setPaginaFinal(pgFinal);
		//
		// ByteArrayOutputStream s = new ByteArrayOutputStream();
		newDoc.save(s);

		// entPrevia.setArquivoBin(s.toByteArray());

		s.close();
		newDoc.close();
		// publicaEvento(entPrevia);

		// newContentPane.addMessage(regional + " " + pgInicial + " " + pgFinal+"\n");

	}

}
