package nosql.docdb.doc_parser;

import lombok.SneakyThrows;
import nosql.docdb.doc_parser.object_model.Document;
import nosql.docdb.doc_parser.object_model.*;
import nosql.docdb.file_utils.FileUtills;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DocumentConverter {
    @SneakyThrows
    public static void main(String[] args) {
        Document document=importFromDoc("dsp.docx", FileUtills.readAllBytes("documents/TsOS_lab_1.docx"));
        System.out.println(document);

    }


    public static Document importFromDoc(String name, byte[] bytes) throws IOException, InvalidFormatException {
        XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(new ByteArrayInputStream(bytes)));
        List<XWPFParagraph> par=xdoc.getParagraphs();
        List<IBodyElement> elems=xdoc.getBodyElements();

        final int pageCount=xdoc.getProperties().getExtendedProperties().getUnderlyingProperties().getPages();

        List<Table> tables=new ArrayList<>();
        List<Paragraph> paragraphs=new ArrayList<>();
        List<Picture> pictures=new ArrayList<>();
        String currentParagraphName="Титульный лист";
        StringBuilder sb=new StringBuilder();

        for(int i=0;i<elems.size();i++){
            if(!(elems.get(i) instanceof XWPFTable))continue;
            XWPFTable table= (XWPFTable) elems.get(i);
            Stream.iterate(i,ind->ind-1)
                    .limit(i+1)
                    .map(elems::get)
                    .filter(e->e instanceof XWPFParagraph)
                    .map(e->(XWPFParagraph)e)
                    .map(XWPFParagraph::getText)
                    .findFirst()
                    .filter(s->!s.isEmpty())
                    .ifPresent(tableName->tables.add(
                            new Table(
                                    tableName,
                                    table.getNumberOfRows(),
                                    table.getRow(0).getTableICells().size()
                            ))
                    );
        }


        for(int i=0;i<par.size();i++){
            XWPFParagraph p=par.get(i);
            if(i<par.size()-1) {
                XWPFParagraph nextParagraph=par.get(i+1);
                p.getRuns().stream()
                        .map(XWPFRun::getEmbeddedPictures)
                        .flatMap(Collection::stream)
                        .findFirst()
                        .ifPresent(pic->{
                            pictures.add(new Picture(nextParagraph.getText(),pic.getPictureData().suggestFileExtension()));
                        });
            }

            if("11".equals(p.getStyle())){
                paragraphs.add(new Paragraph(currentParagraphName,sb.toString()));
                sb=new StringBuilder();
                currentParagraphName=p.getText();
            }else {
                sb.append(p.getText()).append("\n");
            }
        }
        paragraphs.add(new Paragraph(currentParagraphName,sb.toString()));

        List<DocumentObject> documentObjects=Stream.concat(pictures.stream(),tables.stream()).collect(Collectors.toList());

        return new Document(name, Instant.now(), pageCount, bytes.length, bytes, paragraphs, documentObjects);
    }
}
