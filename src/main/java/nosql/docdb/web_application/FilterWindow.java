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

    public FilterWindow(Consumer<FilterWindow> changeListener) {
        super("Фильтры"); // Set window caption
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

        limitWeightSliderLayout = new Slider(0,100000,"Объем документа, KB");
        limitCountOfPagesSliderLayout = new Slider(0,10000,"Количество страниц");

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
}