package nosql.docdb.web_application.views;

import com.github.daishy.rangeslider.RangeSlider;
import com.github.daishy.rangeslider.client.Range;
import com.vaadin.data.HasValue;
import com.vaadin.ui.*;
import org.apache.commons.lang3.tuple.Pair;

public class Slider extends HorizontalLayout {
    private final RangeSlider slider;
    public Slider(int min,int max,String caption){
        slider  = new RangeSlider(new Range(min, max));
        slider.setValue(new Range(min, max));
        slider.setStep(10);
        slider.setWidth("100%");
          MarginPanel mp = new MarginPanel(slider) {{
              setHeight("45px");
          }};
        Label cap = new Label(caption);
        addComponents(cap,mp);
        setComponentAlignment(cap, Alignment.BOTTOM_LEFT);
        setExpandRatio(mp,1);
        setWidth("100%");
        setMargin(false);
    }

    public Pair<Integer,Integer> getValue(){
        return Pair.of(slider.getValue().getLowerAsInt(),slider.getValue().getUpperAsInt());
    }

    public void addChangeListener(HasValue.ValueChangeListener<Range> changeListener){
        slider.addValueChangeListener(changeListener);
    }
}
