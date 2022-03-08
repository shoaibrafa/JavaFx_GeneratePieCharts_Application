package com.chart.pie.piechart;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.PieChart;

public class ChartData extends PieChart{
    private SimpleStringProperty category;
    private SimpleIntegerProperty value;
    private SimpleBooleanProperty show;

    ChartData(String category, Integer value, Boolean show){
        this.category = new SimpleStringProperty(category);
        this.value = new SimpleIntegerProperty(value);
        this.show = new SimpleBooleanProperty(show);
    }

    public String getCategory() {
        return category.get();
    }

    public Integer getValue(){
        return value.get();
    }

    public boolean getShow(){
        return show.get();
    }

    public ObservableValue<Boolean> checkedProperty() {
        return show;
    }
}
