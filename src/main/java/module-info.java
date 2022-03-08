module com.chart.pie.piechart {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires transitive java.desktop;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires javafx.swing;


    opens com.chart.pie.piechart to javafx.fxml;
    exports com.chart.pie.piechart;
}