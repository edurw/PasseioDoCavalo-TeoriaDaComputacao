package teoriadacomputacao.passeiodocavalo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class TabuleiroCavalo extends Application {

    @Override
    public void start(Stage stage) {

        GridPane tabuleiro = new GridPane();

        int tamanho = 8;

        // Labels
        Label lblPosicao = new Label("Posição: -");
        Label lblMovimentosTotais = new Label("Movimento Total: 0");
        Label lblMovimentoAtual = new Label("Movimento Atual: 0");
        Label lblIteracoes = new Label("Iterações: 0");
        Label lblTempo = new Label("Tempo: 0 ms");
        Label lblSolucao = new Label("Solução: -");

        // TextFields
        TextField txtSleepTime = new TextField("5");
        txtSleepTime.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();
            if (novoTexto.isEmpty()) return change; // permite vazio (pra pessoa apagar e digitar de novo)
            if (novoTexto.matches("\\d+")) return change; // permite somente dígitos
            return null; // bloqueia qualquer outra coisa
        }));

        TextField txtTamanho = new TextField("8");
        txtTamanho.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();
            if (novoTexto.isEmpty()) return change; // permite vazio (pra pessoa apagar e digitar de novo)
            if (novoTexto.matches("\\d+")) return change; // permite somente dígitos
            return null; // bloqueia qualquer outra coisa
        }));

        Button btnAplicarTimeSleep = new Button("Aplicar Tempo de Espera");
        btnAplicarTimeSleep.setMaxWidth(Double.MAX_VALUE);
        txtSleepTime.setOnAction(e -> btnAplicarTimeSleep.fire());

        Button btnAplicarTamanho = new Button("Aplicar tamanho");
        btnAplicarTamanho.setMaxWidth(Double.MAX_VALUE);
        txtTamanho.setOnAction(e -> btnAplicarTamanho.fire());

        // BOTÕES
        Button btnForcaBruta = new Button("Força Bruta");
        btnForcaBruta.setMaxWidth(Double.MAX_VALUE);

        Button btnPoda = new Button("Warnsdorff");
        btnPoda.setMaxWidth(Double.MAX_VALUE);

        Button btnPodaBordas = new Button("Poda de Bordas");
        btnPodaBordas.setMaxWidth(Double.MAX_VALUE);

        Button btnPodaCantos = new Button("Poda de Cantos");
        btnPodaCantos.setMaxWidth(Double.MAX_VALUE);

        Button btnSegmentacao = new Button("Segmentação");
        btnSegmentacao.setMaxWidth(Double.MAX_VALUE);

        Button btnConectividade = new Button("Conectividade");
        btnConectividade.setMaxWidth(Double.MAX_VALUE);

        Button btnReset = new Button("Reset");
        btnReset.setMaxWidth(Double.MAX_VALUE);

        VBox painelLateral = new VBox(10,
                new Label("Tempo de espera (ms)"),
                txtSleepTime,
                btnAplicarTimeSleep,
                new Label("Tamanho do tabuleiro (N x N)"),
                txtTamanho,
                btnAplicarTamanho,
                lblPosicao,
                lblMovimentosTotais,
                lblMovimentoAtual,
                lblIteracoes,
                lblTempo,
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

        double tamanhoTabuleiro = 720;

        double larguraTotal = tamanhoTabuleiro + painelLateral.getPrefWidth();
        double alturaTotal = Math.max(tamanhoTabuleiro, painelLateral.getPrefHeight());

        Scene scene = new Scene(root, larguraTotal, alturaTotal);

        // runnable para ativar botoes ao finalizar/resetar
        Runnable habilitarBotoesBusca = () -> {
            switchButtons(true,
                    btnAplicarTimeSleep, btnAplicarTamanho, btnForcaBruta, btnPoda, btnPodaBordas,
                    btnPodaCantos, btnSegmentacao, btnConectividade);
            switchTextFields(true, txtSleepTime, txtTamanho);
        };

        // Inicializa lógica numa "referência mutável"
        final Logica[] logica = new Logica[1];

        logica[0] = new Logica(tabuleiro, tamanho, tamanhoTabuleiro);
        logica[0].setLabels(
                lblPosicao,
                lblMovimentosTotais,
                lblMovimentoAtual,
                lblIteracoes,
                lblTempo,
                lblSolucao
        );
        logica[0].setComponentSwitchState(ativo -> {
            switchButtons(ativo,
                    btnAplicarTimeSleep, btnAplicarTamanho, btnForcaBruta, btnPoda, btnPodaBordas,
                    btnPodaCantos, btnSegmentacao, btnConectividade);
            switchTextFields(ativo, txtSleepTime, txtTamanho);
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

        btnAplicarTimeSleep.setOnAction(e -> {
            String texto = txtSleepTime.getText();

            int novoTempo;
            try {
                novoTempo = Integer.parseInt(texto.trim());
            } catch (NumberFormatException ex) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setHeaderText("Tempo inválido");
                a.setContentText("Digite um número inteiro");
                a.showAndWait();
                return;
            }

            int min = 1;
            int max = 5000;
            if (novoTempo < min || novoTempo > max) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setHeaderText("Tempo fora do intervalo");
                a.setContentText("Escolha um tamanho entre " + min + " e " + max + ".");
                a.showAndWait();
                return;
            }

            logica[0].setSleepTime(novoTempo);
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

            // Salva o sleepTime atual antes de recriar
            int sleepTimeAtual = Integer.parseInt(txtSleepTime.getText().trim());

            // Calcula o tamanho atual da janela
            double larguraDisponivel = scene.getWidth() - painelLateral.getWidth();
            double tamanhoAtual = Math.min(larguraDisponivel, scene.getHeight());

            // Recria a lógica com o novo tamanho E com o tamanho visual atual
            logica[0] = new Logica(tabuleiro, novoTam, tamanhoAtual);
            logica[0].setLabels(
                    lblPosicao,
                    lblMovimentosTotais,
                    lblMovimentoAtual,
                    lblIteracoes,
                    lblTempo,
                    lblSolucao
            );

            // Reconecta o controle de componentes
            logica[0].setComponentSwitchState(ativo -> {
                switchButtons(ativo,
                        btnAplicarTimeSleep, btnAplicarTamanho, btnForcaBruta, btnPoda, btnPodaBordas,
                        btnPodaCantos, btnSegmentacao, btnConectividade);
                switchTextFields(ativo, txtSleepTime, txtTamanho);
            });

            // Reconecta o callback de finalização
            logica[0].setOnBuscaFinalizada(habilitarBotoesBusca);

            logica[0].setSleepTime(sleepTimeAtual);

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
        tabuleiro.setPrefSize(tamanhoTabuleiro, tamanhoTabuleiro);

        stage.setScene(scene);
        stage.setTitle("Passeio do Cavalo");
        stage.centerOnScreen();
        stage.setResizable(false);

        try {
            Image icone = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cavalo.png")));
            stage.getIcons().add(icone);

        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + e.getMessage());
        }

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
