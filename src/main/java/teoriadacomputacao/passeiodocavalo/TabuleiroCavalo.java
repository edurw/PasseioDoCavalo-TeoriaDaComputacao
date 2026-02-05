package teoriadacomputacao.passeiodocavalo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
        Label lblVisitados = new Label("Grids Visitados: 0");

        logica.setLabels(
                lblPosicao,
                lblMovimentos,
                lblIteracoes,
                lblTempo,
                lblSolucao,
                lblVisitados
        );

        // BOTÕES
        Button btnLivre = new Button("Modo Livre");
        btnLivre.setMaxWidth(Double.MAX_VALUE);
        btnLivre.setOnAction(e -> logica.iniciarModoLivre());

        Button btnForcaBruta = new Button("Força Bruta");
        btnForcaBruta.setMaxWidth(Double.MAX_VALUE);
        btnForcaBruta.setOnAction(e -> logica.iniciarForcaBruta());

        Button btnBuscaProfundidade = new Button("Busca Profundidade");
        btnBuscaProfundidade.setMaxWidth(Double.MAX_VALUE);
        btnBuscaProfundidade.setOnAction(e -> logica.iniciarBuscaProfundidade());

        Button btnPoda = new Button("Poda");
        btnPoda.setMaxWidth(Double.MAX_VALUE);
        btnPoda.setOnAction(e -> logica.iniciarPoda());

        Button btnReset = new Button("Reset");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setOnAction(e -> logica.reset());

        VBox painelLateral = new VBox(10,
                new Label("Métricas"),
                lblPosicao,
                lblMovimentos,
                lblIteracoes,
                lblTempo,
                lblVisitados,
                lblSolucao,
                new Label("Algoritmos"),
                btnLivre,
                btnForcaBruta,
                btnBuscaProfundidade,
                btnPoda,
                btnReset
        );

        painelLateral.setPadding(new Insets(15));
        painelLateral.setPrefWidth(180);

        BorderPane root = new BorderPane();
        root.setCenter(tabuleiro);
        root.setRight(painelLateral);

        Scene scene = new Scene(root, 720, 520);
        stage.setScene(scene);
        stage.setTitle("Passeio do Cavalo");
        stage.show();
    }
}
