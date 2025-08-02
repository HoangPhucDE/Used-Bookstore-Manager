package com.example.utils;

import com.example.model.OrderItem;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class OrderItemTableBuilder {
    public static TableView<OrderItem> createOrderItemTable(ObservableList<OrderItem> items) {
        TableView<OrderItem> table = new TableView<>(items);
        table.setPrefHeight(250);

        TableColumn<OrderItem, String> colTitle = new TableColumn<>("Tên sách");
        colTitle.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getBookTitle()));
        colTitle.setPrefWidth(200);

        TableColumn<OrderItem, Integer> colQuantity = new TableColumn<>("Số lượng");
        colQuantity.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
        colQuantity.setPrefWidth(80);

        TableColumn<OrderItem, Double> colUnitPrice = new TableColumn<>("Đơn giá");
        colUnitPrice.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUnitPrice()));
        colUnitPrice.setPrefWidth(100);

        TableColumn<OrderItem, Double> colTotal = new TableColumn<>("Thành tiền");
        colTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalPrice()));
        colTotal.setPrefWidth(120);

        table.getColumns().addAll(colTitle, colQuantity, colUnitPrice, colTotal);
        return table;
    }
}
