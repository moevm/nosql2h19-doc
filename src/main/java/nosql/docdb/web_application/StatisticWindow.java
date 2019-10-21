package nosql.docdb.web_application;

import com.vaadin.ui.*;

public class StatisticWindow extends Window {
        public StatisticWindow(MainView.FileFullInfo record) {
                super("Статистика по документу \""+record.getName()+"\""); // Set window caption
                center();
                setClosable(true);
                setModal(false);
                HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
                VerticalLayout layoutButtons = new VerticalLayout();
                VerticalSplitPanel layoutPlot = new VerticalSplitPanel();
                layoutPlot.addComponents(new Button("a"),new Button("b"));
                layoutButtons.addComponent(new Button("but"));
                horizontalSplitPanel.addComponents(layoutButtons, layoutPlot);
                setContent(horizontalSplitPanel);

                //layout.setExpandRatio(dateOfcreate, 1);

        }
}
