package com.filler.api;

import com.filler.form.Principal;
import com.filler.form.Referencia.Referencia;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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

    @Value("${template.pdf.cartaoposicao}")
    private String filePath;

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Referencia create(@RequestBody Principal principal) {
        try {

            InputStream in = new ClassPathResource(filePath).getInputStream();
            File tempFile = File.createTempFile("~Filler", ".pdf");
            tempFile.deleteOnExit();

            LOGGER.debug("Creating file " + tempFile.getAbsolutePath());

            OutputStream out = new FileOutputStream(tempFile);

            PdfReader pdfReader = new PdfReader(in);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, out);
            final PdfContentByte canvas = pdfStamper.getOverContent(1);

            Visitor fillerVisitor = new Visitor() {
                @Override
                public void visitConteudoSimples(ConteudoSimples conteudo) {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(conteudo.texto, new Font(Font.FontFamily.HELVETICA, 10)), conteudo.x, conteudo.y, 0);
                }

                @Override
                public void visitConteudoComFont(ConteudoComFont conteudo) {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(conteudo.texto, new Font(Font.FontFamily.HELVETICA, conteudo.size)), conteudo.x, conteudo.y, 0);
                }
            };

            carta2017(principal).stream().forEach((conteudo) -> conteudo.accept(fillerVisitor));

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

    interface Conteudo {
        void accept(Visitor visitor);
    }

    private class ConteudoSimples implements Conteudo {
        String texto;
        float x;
        float y;

        public ConteudoSimples(String texto, float x, float y) {
            this.texto = texto;
            this.x = x;
            this.y = y;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitConteudoSimples(this);
        }
    }

    private class ConteudoComFont extends ConteudoSimples {
        private int size;
        public ConteudoComFont(String texto, float x, float y, int size) {
            super(texto, x, y);
            this.size = size;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitConteudoComFont(this);
        }
    }

    interface Visitor {
        void visitConteudoSimples(ConteudoSimples conteudoSimples);
        void visitConteudoComFont(ConteudoComFont conteudoComFont);
    }

    private List<Conteudo> carta2015(Principal principal) {
        List<Conteudo> dadosAndLocation = new ArrayList<>();
        Calendar hoje = Calendar.getInstance();

        dadosAndLocation.add(new ConteudoSimples(String.valueOf(hoje.get(Calendar.DAY_OF_MONTH)), 424.5f, 664.0f));
        dadosAndLocation.add(new ConteudoSimples(month(hoje.get(Calendar.MONTH)), 443.0f, 664.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getNome(), 115.0f, 540.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getTelefone(), 105.0f, 527.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getEmail(), 242.0f, 527.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getEmpresa(), 191.0f, 515.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getCargo(), 170.0f, 502.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getEndereco(), 112.0f, 489.0f));

        return dadosAndLocation;
    }

    private List<Conteudo> carta2016(Principal principal) {
        List<Conteudo> dadosAndLocation = new ArrayList<>();
        Calendar hoje = Calendar.getInstance();

        dadosAndLocation.add(new ConteudoSimples(String.valueOf(hoje.get(Calendar.DAY_OF_MONTH)), 424.5f, 664.0f));
        dadosAndLocation.add(new ConteudoSimples(month(hoje.get(Calendar.MONTH)), 443.0f, 664.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getNome(), 115.0f, 540.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getTelefone(), 380.0f, 540.0f));
        dadosAndLocation.add(new ConteudoComFont(principal.getEmail(), 89.0f, 528.0f, 6));
        dadosAndLocation.add(new ConteudoSimples(principal.getEmpresa(), 341.0f, 527.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getCargo(), 164.0f, 515.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getEndereco(), 58.0f, 502.0f));

        return dadosAndLocation;
    }

    private List<Conteudo> carta2017(Principal principal) {
        List<Conteudo> dadosAndLocation = new ArrayList<>();
        Calendar hoje = Calendar.getInstance();

        int smallFontSize = 6;

        dadosAndLocation.add(new ConteudoSimples(String.valueOf(hoje.get(Calendar.DAY_OF_MONTH)), 424.7f, 664.0f));
        dadosAndLocation.add(new ConteudoSimples(month(hoje.get(Calendar.MONTH)), 443.0f, 664.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getNome(), 115.0f, 540.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getTelefone(), 380.0f, 540.0f));
        dadosAndLocation.add(new ConteudoComFont(principal.getEmail(), 89.0f, 528.0f, smallFontSize));
        dadosAndLocation.add(new ConteudoSimples(principal.getEmpresa(), 341.0f, 527.0f));
        dadosAndLocation.add(new ConteudoSimples(principal.getCargo(), 164.0f, 515.0f));
        dadosAndLocation.add(new ConteudoComFont(principal.getEndereco(), 58.0f, 502.0f, smallFontSize));

        return dadosAndLocation;
    }

}
