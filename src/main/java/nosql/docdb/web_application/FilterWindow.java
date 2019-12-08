package nosql.docdb.web_application;

import com.vaadin.ui.*;
import lombok.Value;
import nosql.docdb.database.MongoDB;
import nosql.docdb.web_application.views.Slider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;


public class FilterWindow extends Window{
    private final ComboBox<MongoDB.Query.DocFormat> docFormatCombobox;
    private final Slider limitCountOfPagesSliderLayout;
    private final Slider limitWeightSliderLayout;
    private final MongoDB mongoDB;

    public FilterWindow(Consumer<FilterWindow> changeListener, MongoDB mongoDB) {
        super("Фильтры"); // Set window caption
        this.mongoDB=mongoDB;

        limitWeightSliderLayout = new Slider(0,Integer.MAX_VALUE,"Объем документа, KB");
        limitCountOfPagesSliderLayout = new Slider(0,Integer.MAX_VALUE,"Количество страниц");

        center();
        setClosable(true);
        setModal(false);
        setVisible(false);
        setWidth("40%");

        docFormatCombobox =new ComboBox<MongoDB.Query.DocFormat>("Режим поиска"){{
            setItemCaptionGenerator(MongoDB.Query.DocFormat::getDescription);
            setItems(MongoDB.Query.DocFormat.values());
            setSelectedItem(MongoDB.Query.DocFormat.FREE_FORM);
            setEmptySelectionAllowed(false);
            setWidth("50%");
        }};



        VerticalLayout upperPanel = new VerticalLayout(
                docFormatCombobox,
                new VerticalLayout(limitCountOfPagesSliderLayout, limitWeightSliderLayout) {{
                    setMargin(false);
                }}
        ){{
            setWidth("100%");
        }};
        setContent(upperPanel);
        changeListener.accept(this);
        docFormatCombobox.addValueChangeListener(e->changeListener.accept(this));
        limitCountOfPagesSliderLayout.addChangeListener(e->changeListener.accept(this));
        limitWeightSliderLayout.addChangeListener(e->changeListener.accept(this));
    }

    public FilterWindowParams getValue(){
        return new FilterWindowParams(
                limitWeightSliderLayout.getValue(),
                limitCountOfPagesSliderLayout.getValue(),
                docFormatCombobox.getValue()
        );
    }

    @Value
    public static class FilterWindowParams{
        Pair<Integer,Integer> size;
        Pair<Integer,Integer> pageCount;
        MongoDB.Query.DocFormat docFormat;
    }

    @Override
    public void close(){
        setVisible(false);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        mongoDB.getPageRange()
                .ifPresent(r->limitCountOfPagesSliderLayout.setRange(r.getLeft(),r.getRight()+1));

        mongoDB.getSizeRange()
                .ifPresent(r->limitWeightSliderLayout.setRange(r.getLeft()/1024,r.getRight()/1024+1));
    }
}