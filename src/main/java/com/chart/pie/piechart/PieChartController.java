package com.chart.pie.piechart;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.IntegerStringConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class PieChartController {

    @FXML
    private TextField category;
    @FXML
    private TextField value;
    @FXML
    private PieChart pieChart;
    @FXML
    private TableView dataTable;
    @FXML
    private TextField chartTitle;
    private int selectedTableRow;
    @FXML
    private ColorPicker colorpicker;

    /**
     * The following fields are Observable fields that hold the data passed to TableView and PieChart.
     * First the data is added to the dataForTableView list and the ListChangeListener of the dataForTableView
     * pass the data into the dataForPieChart list.
     */
    ObservableList<ChartData.Data> dataForPieChart;
    ObservableList<ChartData> dataForTableView;


    /**
     * The following initializes the fields add ListChangeListener for the dataForTableViewList and creates the
     * dataTable TableView with the required columns.
     */
    public void initialize() {
        dataForPieChart = FXCollections.observableArrayList();
        pieChart.setData(dataForPieChart);
        pieChart.titleProperty().bind(chartTitle.textProperty());

        pieChart.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        if(e.getPickResult().getIntersectedNode().toString().startsWith("Region")){
                            colorpicker.show();
                            colorpicker.setOnAction(new EventHandler() {
                                public void handle(Event t) {
                                    String color = colorpicker.getValue().toString().substring(2,8).toUpperCase();
                                    e.getPickResult().getIntersectedNode().setStyle("-fx-pie-color:#" + color + ";");
                                }
                            });
                        }
                    }
                });

        dataTable.setEditable(true);
        dataForTableView = FXCollections.observableArrayList(chartData -> new Observable[]{chartData.checkedProperty()});
        dataForTableView.addListener((ListChangeListener<ChartData>) c -> {
            while (c.next()) {
                for (var a : c.getAddedSubList()) {
                    /** add the data to dataForPieChartList when added into the dataForTableView */
                    dataForPieChart.add(new PieChart.Data(a.getCategory(), a.getValue()));
                }
                /** Check if the data is changed inside the table view or the checkbox state is changed */
                if (c.wasUpdated()) {
                    int start = c.getFrom();
                    int end = c.getTo();
                    /** loop through the changed data */
                    for (int i = start; i < end; i++) {
                        /** if the state of the checkbox is changed to false */
                        if (!c.getList().get(i).getShow()) {
                            /** get category of the data */
                            String catToRemove = c.getList().get(i).getCategory();
                            /** loop through the dataForPieChart list and remove that category from chart */
                            for (int j = 0; j < dataForPieChart.size(); j++) {
                                if (dataForPieChart.get(j).getName().equals(catToRemove)) {
                                    dataForPieChart.remove(j);
                                }
                            }
                            /** if state of the checkbox changed from false to true then get the data and add to the
                             * dataForPieChart list. */
                        } else {
                            dataForPieChart.add(new PieChart.Data(c.getList().get(i).getCategory(), c.getList().get(i).getValue()));
                        }
                    }
                }
            }
        });

        /** Create the Category column of the table */

        TableColumn<ChartData, String> category = new TableColumn<>("Category");
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        category.setCellFactory(TextFieldTableCell.forTableColumn());

        /** Create the Category column of the table */
        TableColumn<ChartData, Integer> value = new TableColumn<>("Value");
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        value.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        /** Create the Show column of the table */
        TableColumn<ChartData, Boolean> showHide = new TableColumn<>("Show");
        showHide.setCellValueFactory(new PropertyValueFactory<>("show"));
        showHide.setCellFactory(CheckBoxTableCell.forTableColumn(param -> dataForTableView.get(param).checkedProperty()));

        /** Add all columns to the table */
        dataTable.getColumns().addAll(category, value, showHide);

        /** Link the list to the table */
        dataTable.setItems(dataForTableView);
    }

    /**
     * The following method gets the data from user input (category and value text fields)
     * and if the data is redundant it fires an alert and if the data is not redundant
     * it adds the data into dataForTableView list
     */
    @FXML
    public void addData() {
        String category = this.category.getText();
        Integer value = Integer.parseInt(this.value.getText());
        ArrayList<ChartData> toAdd = new ArrayList<>();
        if (dataForTableView.size() > 0 && dataForPieChart.size() > 0) {
            for (var item : dataForTableView) {
                if (item.getCategory().equals(category)) {
                    alert();
                    toAdd.clear();
                    break;
                } else {
                    toAdd.clear();
                    toAdd.add(new ChartData(category, value, true));
                }
            }
        } else if (dataForTableView.size() == 0) {
            dataForTableView.add(new ChartData(category, value, true));
        }
        dataForTableView.addAll(toAdd);
    }


    /**
     * This method exports the generated chart as a png file.
     */
    @FXML
    public void exportChartImage() {
        int height = (int) pieChart.getHeight();
        int width = (int) pieChart.getWidth();
        final SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setViewport(new Rectangle2D(0.0, 0.0, width, height));
        try {
            WritableImage image = pieChart.snapshot(snapshotParameters, new WritableImage(width, height));
            File file = new File(selectDirAndFileName() + ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)), "png", file);
        } catch (IOException ex) {
            System.out.println("Error saving the image");
        }
    }

    /**
     * choose a directory to save the file into.
     *
     * @return returns the directory path and name.
     */
    public File selectDirAndFileName() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
        File filename = fileChooser.showSaveDialog(pieChart.getScene().getWindow());
        return filename;
    }

    /**
     * Creates an alert. This alert is called if there is a redundant data in the dataForTableView list.
     */
    private void alert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText("Can not add. Category Exists!");
        alert.showAndWait();
    }

    /**
     * The following table listener gets the row index of the tableview.
     *
     * @param event
     */
    @FXML
    private void tableListener(MouseEvent event) {
        this.selectedTableRow = dataTable.getSelectionModel().getSelectedIndex();
    }

    /**
     * When the remove item button is clicked this method is getting called which removes the selected
     * row from the tableview.
     */
    @FXML
    private void removeItemFromTable() {
        dataForTableView.remove(selectedTableRow);
    }

    /**
     * When the reset button is click this method is called and it clears both the dataForPieChart and
     * dataForTableView lists.
     */
    @FXML
    public void resetPieChartData() {
        this.dataForPieChart.clear();
        this.dataForTableView.clear();
    }
}