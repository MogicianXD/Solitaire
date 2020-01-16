package application;

import application.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/view.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Solitaire纸牌游戏");
        primaryStage.setScene(new Scene(root));
        Controller controller = loader.getController();
        controller.init();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
