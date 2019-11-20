package nosql.docdb.web_application.statistic_window;

import java.util.ArrayList;
import java.util.List;

public class PlotData{

        public PlotData(String parametrName, List<Number> xs, List<Number> ys) {
                this.parametrName = parametrName;
                this.xs = xs;
                this.ys = ys;
        }
        public PlotData(String parametrName, ArrayList<Number> ys) {
                this.parametrName = parametrName;
                this.ys = ys;
        }

        String parametrName;

        List<Number> xs = new ArrayList<>();
        List<Number> ys = new ArrayList<>();
}
