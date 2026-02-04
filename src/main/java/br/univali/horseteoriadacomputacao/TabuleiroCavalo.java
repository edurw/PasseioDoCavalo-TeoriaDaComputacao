package br.univali.horseteoriadacomputacao;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class TabuleiroCavalo extends Application {
    @Override
    public void start(Stage stage) {

        GridPane tabuleiro = new GridPane();
        tabuleiro.setPrefSize(480, 480);

        // lógica
        Logica logica = new Logica(tabuleiro);

        // MÉTRICAS
        Label lblPosicao = new Label("Posição: -");
        Label lblMovimentos = new Label("Movimentos: 0");
        Label lblIteracoes = new Label("Iterações: 0");
        Label lblTempo = new Label("Tempo: 0 ms");
        Label lblSolucao = new Label("Solução: -");

        logica.setLabels(
                lblPosicao,
                lblMovimentos,
                lblIteracoes,
                lblTempo,
                lblSolucao
        );

        // BOTÃO RESET
        Button btnReset = new Button("Reset");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setOnAction(e -> logica.reset());

        VBox painelLateral = new VBox(10,
                new Label("Métricas"),
                lblPosicao,
                lblMovimentos,
                lblIteracoes,
                lblTempo,
                lblSolucao,
                btnReset
        );

        painelLateral.setPadding(new Insets(15));
        painelLateral.setPrefWidth(160);

        BorderPane root = new BorderPane();
        root.setCenter(tabuleiro);
        root.setRight(painelLateral);

        Scene scene = new Scene(root, 680, 520);
        stage.setScene(scene);
        stage.setTitle("Passeio do Cavalo");
        stage.show();
    }
}
