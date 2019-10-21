package nosql.docdb.web_application;

import com.github.daishy.rangeslider.RangeSlider;
import com.github.daishy.rangeslider.client.Range;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SliderLayout extends HorizontalLayout {
    private final RangeSlider slider;
    SliderLayout(int min,int max,String caption, boolean margin){
        slider  = new RangeSlider(new Range(min, max));
        slider.setValue(new Range(0, max/10));
        slider.setStep(10);
        slider.setWidth("100%");
          MarginPanel mp = new MarginPanel(slider, 35) {{
              setHeight("45px");
          }};
        Label cap = new Label(caption);
        addComponents(cap,mp);
        setComponentAlignment(cap,Alignment.BOTTOM_LEFT);
        setExpandRatio(mp,1);
        setWidth("100%");
        setMargin(false);
        //setSizeFull();
    }
}



class MarginPanel extends AbsoluteLayout{
    MarginPanel(Component source,int margins){
        //String cssRule=Stream.of( "top,left,right,bottom".split(","))
        //        .map(side->side+": "+margins+"px;")
        //        .collect(Collectors.joining(" "));
        String cssRule = "top: 30px; left: 20px; right: 0px; bottom: 0px";
        System.out.println(cssRule);
        addComponent(source,cssRule);
    }
}

class FilterWindow extends Window{
    private final CheckBox definedDocumentStructureCheckBox;
    private final CheckBox documentsWithoutHeadersCheckBox;
    private final SliderLayout limitCountOfPagesSliderLayout;
    private final SliderLayout limitWeightSliderLayout;

    FilterWindow() {
        super("Фильтры"); // Set window caption
        center();
        setClosable(true);
        setModal(false);
        definedDocumentStructureCheckBox =new CheckBox("Документы с заданной структурой");
        documentsWithoutHeadersCheckBox =new CheckBox("Документы без заголовков");
        limitWeightSliderLayout = new SliderLayout(0,1000,"Объем документа",true);
        limitCountOfPagesSliderLayout = new SliderLayout(0,1000,"Количество страниц",false);

        VerticalLayout upperPanel = new VerticalLayout(
                new VerticalLayout(definedDocumentStructureCheckBox, documentsWithoutHeadersCheckBox) {{
                    setMargin(false);
                    //setSizeFull();
                }},
                new VerticalLayout(limitCountOfPagesSliderLayout, limitWeightSliderLayout) {{
                    setMargin(false);
                    //setSizeFull();
                    //setComponentAlignment(limitCountOfPagesSliderLayout,Alignment.TOP_LEFT);
                    //setComponentAlignment(limitWeightSliderLayout,Alignment.BOTTOM_LEFT);

                }}
        )   {{
            setWidth("100%");
        }};
        setContent(upperPanel);
    }
}