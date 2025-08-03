package com.example.controller.stock;

import com.example.controller.dao.StockDao;
import com.example.model.StockEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StockManagementController {

    @FXML private TableView<StockEntry> stockTable;
    @FXML private TableColumn<StockEntry, Integer> colId;
    @FXML private TableColumn<StockEntry, String> colBookTitle;
    @FXML private TableColumn<StockEntry, Integer> colQuantity;
    @FXML private TableColumn<StockEntry, String> colDate;

    private final StockDao stockDao = new StockDao();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colBookTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getBookTitle()));
        colQuantity.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEntryDate().toString()));

        loadStockEntries();
    }

    private void loadStockEntries() {
        ObservableList<StockEntry> list = FXCollections.observableArrayList(stockDao.getAllStockEntries());
        stockTable.setItems(list);
    }
}
