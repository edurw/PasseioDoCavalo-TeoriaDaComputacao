package teoriadacomputacao.passeiodocavalo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TabuleiroCavalo extends Application {

    @Override
    public void start(Stage stage) {

        GridPane tabuleiro = new GridPane();

        int tamanho = 8;

        // Labels (MÉTRICAS)
        Label lblPosicao = new Label("Posição: -");
        Label lblMovimentosTotais = new Label("Movimento Total: 0");
        Label lblMovimentosAtuais = new Label("Movimento Atual: 0");
        Label lblIteracoes = new Label("Iterações: 0");
        Label lblTempo = new Label("Tempo: 0 ms");
        Label lblSolucao = new Label("Solução: -");
        Label lblDescobertos = new Label("Grids Descobertos: 0");

        // TextField para tamanho
        TextField txtTamanho = new TextField("8");
        txtTamanho.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();

            // permite vazio (pra pessoa apagar e digitar de novo)
            if (novoTexto.isEmpty()) return change;

            // permite somente dígitos
            if (novoTexto.matches("\\d+")) return change;

            // bloqueia qualquer outra coisa
            return null;
        }));

        Button btnAplicarTamanho = new Button("Aplicar tamanho");
        btnAplicarTamanho.setMaxWidth(Double.MAX_VALUE);
        txtTamanho.setOnAction(e -> btnAplicarTamanho.fire());

        // BOTÕES
        Button btnForcaBruta = new Button("Força Bruta");
        btnForcaBruta.setMaxWidth(Double.MAX_VALUE);

        Button btnPoda = new Button("Menos movimentos Futuros");
        btnPoda.setMaxWidth(Double.MAX_VALUE);

        Button btnPodaBordas = new Button("Focar nas Bordas");
        btnPodaBordas.setMaxWidth(Double.MAX_VALUE);

        Button btnPodaCantos = new Button("Focar nos Cantos");
        btnPodaCantos.setMaxWidth(Double.MAX_VALUE);

        Button btnSegmentacao = new Button("Segmentar Secções");
        btnSegmentacao.setMaxWidth(Double.MAX_VALUE);

        Button btnConectividade = new Button("Evitar Casas Sozinhas");
        btnConectividade.setMaxWidth(Double.MAX_VALUE);

        Button btnReset = new Button("Reset");
        btnReset.setMaxWidth(Double.MAX_VALUE);

        VBox painelLateral = new VBox(10,
                new Label("Tamanho do tabuleiro (N x N)"),
                txtTamanho,
                btnAplicarTamanho,
                lblPosicao,
                lblMovimentosTotais,
                lblMovimentosAtuais,
                lblIteracoes,
                lblTempo,
                lblDescobertos,
                lblSolucao,
                new Label(""),
                new Label("Algoritmos:"),
                btnForcaBruta,
                btnPoda,
                btnPodaBordas,
                btnPodaCantos,
                btnSegmentacao,
                btnConectividade,
                btnReset
        );

        painelLateral.setPadding(new Insets(15));
        painelLateral.setPrefWidth(220);

        BorderPane root = new BorderPane();
        root.setCenter(tabuleiro);
        root.setRight(painelLateral);

        // Tamanhos base
        double larguraBase = 900;
        double alturaBase = 700;

        Scene scene = new Scene(root, larguraBase, alturaBase);

        // runnable para ativar botoes ao finalizar/resetar
        Runnable habilitarBotoesBusca = () -> {
            switchButtons(true,
                    btnAplicarTamanho, btnForcaBruta, btnPoda, btnPodaBordas,
                    btnPodaCantos, btnSegmentacao, btnConectividade);
            switchTextFields(true, txtTamanho);
        };

        // runnable para ativar botoes ao finalizar/resetar
        Runnable habilitarTextfields = () -> switchTextFields(true, txtTamanho);

        // Inicializa lógica numa "referência mutável"
        final Logica[] logica = new Logica[1];

        // Inicializa com tamanho calculado
        double tamanhoInicial = Math.min(
                scene.getWidth() - painelLateral.getWidth(),
                scene.getHeight()
        );

        logica[0] = new Logica(tabuleiro, tamanho, tamanhoInicial);
        logica[0].setLabels(
                lblPosicao,
                lblMovimentosTotais,
                lblMovimentosAtuais,
                lblIteracoes,
                lblTempo,
                lblSolucao,
                lblDescobertos
        );
        logica[0].setComponentSwitchState(ativo -> {
            switchButtons(ativo,
                    btnAplicarTamanho, btnForcaBruta, btnPoda, btnPodaBordas,
                    btnPodaCantos, btnSegmentacao, btnConectividade);
            switchTextFields(ativo, txtTamanho);
        });

        // passando pra logica o runnable
        logica[0].setOnBuscaFinalizada(habilitarBotoesBusca);

        // Metodo auxiliar para atualizar tamanho
        Runnable atualizarTamanho = () -> {
            double larguraDisponivel = scene.getWidth() - painelLateral.getWidth();
            double novoTamanho = Math.min(larguraDisponivel, scene.getHeight());
            tabuleiro.setPrefSize(novoTamanho, novoTamanho);
            if (logica[0] != null) {
                logica[0].redimensionar(novoTamanho);
            }
        };

        // VINCULA O TAMANHO DO TABULEIRO À ALTURA DA JANELA
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            atualizarTamanho.run();
        });

        // VINCULA O TAMANHO DO TABULEIRO À LARGURA DISPONÍVEL
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            atualizarTamanho.run();
        });

        btnAplicarTamanho.setOnAction(e -> {
            String texto = txtTamanho.getText();

            int novoTam;
            try {
                novoTam = Integer.parseInt(texto.trim());
            } catch (NumberFormatException ex) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setHeaderText("Tamanho inválido");
                a.setContentText("Digite um número inteiro (ex.: 8, 10, 12).");
                a.showAndWait();
                return;
            }

            int min = 5;
            int max = 25;
            if (novoTam < min || novoTam > max) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setHeaderText("Tamanho fora do intervalo");
                a.setContentText("Escolha um tamanho entre " + min + " e " + max + ".");
                a.showAndWait();
                return;
            }

            // Calcula o tamanho atual da janela
            double larguraDisponivel = scene.getWidth() - painelLateral.getWidth();
            double tamanhoAtual = Math.min(larguraDisponivel, scene.getHeight());

            // Recria a lógica com o novo tamanho E com o tamanho visual atual
            logica[0] = new Logica(tabuleiro, novoTam, tamanhoAtual);
            logica[0].setLabels(
                    lblPosicao,
                    lblMovimentosTotais,
                    lblMovimentosAtuais,
                    lblIteracoes,
                    lblTempo,
                    lblSolucao,
                    lblDescobertos
            );

            // Reconecta o controle de componentes
            logica[0].setComponentSwitchState(ativo -> {
                switchButtons(ativo,
                        btnAplicarTamanho, btnForcaBruta, btnPoda, btnPodaBordas,
                        btnPodaCantos, btnSegmentacao, btnConectividade);
                switchTextFields(ativo, txtTamanho);
            });

            // Reconecta o callback de finalização
            logica[0].setOnBuscaFinalizada(habilitarBotoesBusca);

            // Força atualização imediata
            tabuleiro.setPrefSize(tamanhoAtual, tamanhoAtual);
        });

        btnForcaBruta.setOnAction(e -> {
            if(logica[0].isExecutando()) return; 
            logica[0].iniciarForcaBruta();
        });

        btnPoda.setOnAction(e -> {
            if(logica[0].isExecutando()) return; 
            logica[0].iniciarPoda();
        });

        btnPodaBordas.setOnAction(e -> {
            if(logica[0].isExecutando()) return; 
            logica[0].iniciarBordas();
        });

        btnPodaCantos.setOnAction(e -> {
            if(logica[0].isExecutando()) return; 
            logica[0].iniciarCantos();
        });

        btnSegmentacao.setOnAction(e -> {
            if(logica[0].isExecutando()) return; 
            logica[0].iniciarSegmentacao();
        });

        btnConectividade.setOnAction(e -> {
            if(logica[0].isExecutando()) return;
            logica[0].iniciarConectividade();
        });

        btnReset.setOnAction(e -> logica[0].reset());

        // Define tamanho inicial
        tabuleiro.setPrefSize(tamanhoInicial, tamanhoInicial);

        stage.setScene(scene);
        stage.setTitle("Passeio do Cavalo");
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.show();
    }

    private void switchButtons(boolean ativo, Button... botoes) {
        for (Button b : botoes)
            b.setDisable(!ativo);
    }

    private void switchTextFields(boolean ativo, TextField... campos) {
        for (TextField c : campos)
            c.setDisable(!ativo);
    }
}
