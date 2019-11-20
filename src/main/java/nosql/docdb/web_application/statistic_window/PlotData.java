package nosql.docdb.web_application.statistic_window;

import java.util.ArrayList;

public class PlotData{

        public PlotData(String parametrName, ArrayList<Number> xs, ArrayList<Number> ys) {
                this.parametrName = parametrName;
                this.xs = xs;
                this.ys = ys;
        }
        public PlotData(String parametrName, ArrayList<Number> ys) {
                this.parametrName = parametrName;
                this.ys = ys;
        }

        String parametrName;

        ArrayList<Number> xs = new ArrayList<>();
        ArrayList<Number> ys = new ArrayList<>();
}
