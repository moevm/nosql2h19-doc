package nosql.docdb.web_application;

import com.vaadin.ui.*;

public class StaticticWindow extends Window {
        public StaticticWindow(MainView.FileFullInfo record) {
                super("Статистика по документу \""+record.getName()+"\""); // Set window caption
                center();
                setClosable(true);
                setModal(false);
                HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
                VerticalLayout layoutButtons = new VerticalLayout();
                HorizontalLayout layoutPlot = new HorizontalLayout();

                layoutButtons.addComponent(new Button("but"));
                horizontalSplitPanel.addComponents(layoutButtons, layoutPlot);
                setContent(horizontalSplitPanel);

                //layout.setExpandRatio(dateOfcreate, 1);

        }
}
