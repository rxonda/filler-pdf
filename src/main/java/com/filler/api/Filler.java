package com.filler.api;

import com.filler.form.Principal;
import com.filler.form.Referencia.Referencia;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xonda on 22/10/2015.
 */
@RestController
@RequestMapping("/filler")
public class Filler {

    private static Logger LOGGER = LoggerFactory.getLogger(Filler.class);

    private Resource resource = new ClassPathResource("carta_oposicao_CA1.pdf");

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Referencia create(@RequestBody Principal principal) {
        try {

            InputStream in = resource.getInputStream();
            File tempFile = File.createTempFile("~Filler", ".pdf");
            tempFile.deleteOnExit();

            LOGGER.debug("Creating file " + tempFile.getAbsolutePath());

            OutputStream out = new FileOutputStream(tempFile);

            PdfReader pdfReader = new PdfReader(in);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, out);
            PdfContentByte canvas = pdfStamper.getOverContent(1);
            List<Conteudo> dadosAndLocation = new ArrayList<>();
            Calendar hoje = Calendar.getInstance();
            dadosAndLocation.add(new Conteudo(String.valueOf(hoje.get(Calendar.DAY_OF_MONTH)), 424.5f, 664.0f));
            dadosAndLocation.add(new Conteudo(month(hoje.get(Calendar.MONTH)), 443.0f, 664.0f));
            dadosAndLocation.add(new Conteudo(principal.getNome(), 115.0f, 540.0f));
            dadosAndLocation.add(new Conteudo(principal.getTelefone(), 105.0f, 527.0f));
            dadosAndLocation.add(new Conteudo(principal.getEmail(), 242.0f, 527.0f));
            dadosAndLocation.add(new Conteudo(principal.getEmpresa(), 191.0f, 515.0f));
            dadosAndLocation.add(new Conteudo(principal.getCargo(), 170.0f, 502.0f));
            dadosAndLocation.add(new Conteudo(principal.getEndereco(), 112.0f, 489.0f));

            dadosAndLocation.stream().forEach((conteudo) -> ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(conteudo.texto), conteudo.x, conteudo.y, 0));

            pdfStamper.close();
            pdfReader.close();

            Referencia r = new Referencia();

            r.setFilePath(tempFile.getAbsolutePath());

            return r;
        } catch (IOException e) {
            LOGGER.error("Erro de IO", e);
            throw new RuntimeException("Erro de IO",e);
        } catch (DocumentException e) {
            LOGGER.error("Erro ao manipular o arquivo PDF",e);
            throw new RuntimeException("Erro ao manipular o arquivo PDF",e);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public void retrieve(@RequestParam(value = "FilePath") String filePath,HttpServletResponse response) {
        try {
            InputStream in = new FileInputStream(filePath);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"Carta_Oposicao_Reforco_Sindical.pdf\"");
            OutputStream out = response.getOutputStream();
            IOUtils.copy(in, out);
        } catch (IOException e) {
            LOGGER.error("Arquivo nao foi encontrado!", e);
            throw new RuntimeException("Aquivo nao foi encontrado");
        }
    }

    private String month(int m) {
        Map<Integer, String> months = new HashMap<>();
        months.put(new Integer(0), "janeiro");
        months.put(new Integer(1), "fevereiro");
        months.put(new Integer(2), "março");
        months.put(new Integer(3), "abril");
        months.put(new Integer(4), "maio");
        months.put(new Integer(5), "junho");
        months.put(new Integer(6), "julho");
        months.put(new Integer(7), "agosto");
        months.put(new Integer(8), "setembro");
        months.put(new Integer(9), "outubro");
        months.put(new Integer(10), "novembro");
        months.put(new Integer(11), "dezembro");
        return months.get(new Integer(m));
    }

    private class Conteudo {
        String texto;
        float x;
        float y;

        public Conteudo(String texto, float x, float y) {
            this.texto = texto;
            this.x = x;
            this.y = y;
        }
    }

}
