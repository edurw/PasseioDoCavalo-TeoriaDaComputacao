package teoriadacomputacao.passeiodocavalo;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

public class Logica {

    private final int TAM;

    // REMOVIDO O FINAL para permitir atualização
    private double tamanhoTabuleiro;

    // 0 = vazio, 1 = pode mover, 2 = cavalo
    private final int[][] estado;

    // referência visual
    private final StackPane[][] casas;

    // grid visual
    private final GridPane tabuleiro;

    // posição atual do cavalo
    private int cavaloLinha = -1;
    private int cavaloColuna = -1;

    // contagem de movimentos
    private int movimentos = 0;
    private int iteracoes = 0;

    private int inicioLinha = -1;
    private int inicioColuna = -1;
    private boolean isFechada = false;

    private long tempoInicio = 0;

    private Label lblPosicao;
    private Label lblMovimentos;
    private Label lblIteracoes;
    private Label lblTempo;
    private Label lblSolucao;

    // Construtor
    public Logica(GridPane tabuleiro, int tamanho, double tamanhoVisual) {
        this.TAM = tamanho;
        this.tabuleiro = tabuleiro;
        this.tamanhoTabuleiro = tamanhoVisual;

        this.estado = new int[TAM][TAM];
        this.casas = new StackPane[TAM][TAM];

        criarTabuleiro();
    }

    // Criação do tabuleiro visual
    private void criarTabuleiro() {
        tabuleiro.getChildren().clear();
        tabuleiro.setPrefSize(tamanhoTabuleiro, tamanhoTabuleiro);

        double tamanhoCasa = tamanhoTabuleiro / TAM;

        for (int linha = 0; linha < TAM; linha++) {
            for (int coluna = 0; coluna < TAM; coluna++) {

                StackPane casa = new StackPane();
                casa.setPrefSize(tamanhoCasa, tamanhoCasa);

                boolean clara = (linha + coluna) % 2 == 0;
                casa.setStyle(
                        "-fx-background-color: " +
                                (clara ? "#f0d9b5" : "#b58863") +
                                "; -fx-border-color: black;"
                );

                final int l = linha;
                final int c = coluna;

                casa.setOnMouseClicked(e -> tratarClique(l, c));

                casas[linha][coluna] = casa;
                tabuleiro.add(casa, coluna, linha);
            }
        }
    }

    // Clique do mouse
    private void tratarClique(int linha, int coluna) {
        if (cavaloLinha == -1) {
            colocarCavalo(linha, coluna);
        } else {
            moverCavalo(linha, coluna);
        }
    }

    private void colocarCavalo(int linha, int coluna) {
        limparTabuleiro();
        tempoInicio = System.nanoTime();
        estado[linha][coluna] = 2;
        cavaloLinha = linha;
        cavaloColuna = coluna;

        inicioLinha = linha;
        inicioColuna = coluna;

        atualizarVisual();
        movimentos = 0;
        atualizarMetricas();
    }

    private long getTempoDecorridoMs() {
        if (tempoInicio == 0) return 0;
        return (System.nanoTime() - tempoInicio) / 1_000_000;
    }

    private void moverCavalo(int linha, int coluna) {
        iteracoes++;
        limparTabuleiro();
        estado[linha][coluna] = 2;
        cavaloLinha = linha;
        cavaloColuna = coluna;
        atualizarVisual();
        movimentos++;
        verificarSolucao();
        atualizarMetricas();
    }

    private void limparTabuleiro() {
        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < TAM; j++) {
                estado[i][j] = 0;
            }
        }
    }

    private void atualizarVisual() {
        double tamanhoCasa = tamanhoTabuleiro / TAM;
        double tamanhoCavalo = tamanhoCasa * 0.6;

        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < TAM; j++) {
                StackPane casa = casas[i][j];
                casa.getChildren().clear();

                if (estado[i][j] == 2) {
                    Label cavalo = new Label("♞");
                    cavalo.setStyle("-fx-font-size: " + tamanhoCavalo + "px; -fx-text-fill: black;");
                    casa.getChildren().add(cavalo);
                }
            }
        }
    }

    public void setLabels(
            Label posicao,
            Label movimentos,
            Label iteracoes,
            Label tempo,
            Label solucao
    ) {
        this.lblPosicao = posicao;
        this.lblMovimentos = movimentos;
        this.lblIteracoes = iteracoes;
        this.lblTempo = tempo;
        this.lblSolucao = solucao;

        atualizarMetricas();
    }

    private void atualizarMetricas() {
        if (lblPosicao != null) {
            if (cavaloLinha == -1) {
                lblPosicao.setText("Posição: -");
            } else {
                lblPosicao.setText(
                        "Posição: (" + cavaloLinha + ", " + cavaloColuna + ")"
                );
            }
        }

        if (lblMovimentos != null) {
            lblMovimentos.setText("Movimentos: " + movimentos);
        }

        if (lblTempo != null) {
            lblTempo.setText("Tempo: " + getTempoDecorridoMs() + " ms");
        }

        if (lblIteracoes != null) {
            lblIteracoes.setText("Iterações: " + iteracoes);
        }

        if (lblSolucao != null) {
            int movimentosParaConcluir = (TAM * TAM) - 1;

            if (movimentos < movimentosParaConcluir) {
                lblSolucao.setText("Solução: -");
            } else {
                lblSolucao.setText(
                        "Solução: " + (isFechada ? "Fechada" : "Aberta")
                );
            }
        }
    }

    private void verificarSolucao() {
        int movimentosParaConcluir = (TAM * TAM) - 1;
        if (movimentos < movimentosParaConcluir) return;

        isFechada = movimentoValido(
                cavaloLinha,
                cavaloColuna,
                inicioLinha,
                inicioColuna
        );
    }

    private boolean movimentoValido(int l1, int c1, int l2, int c2) {
        int dl = Math.abs(l1 - l2);
        int dc = Math.abs(c1 - c2);
        return (dl == 2 && dc == 1) || (dl == 1 && dc == 2);
    }

    public void reset() {
        cavaloLinha = -1;
        cavaloColuna = -1;
        movimentos = 0;
        iteracoes = 0;
        tempoInicio = 0;
        isFechada = false;

        limparTabuleiro();
        atualizarVisual();
        atualizarMetricas();
    }

    public void redimensionar(double novoTamanho) {
        this.tamanhoTabuleiro = novoTamanho;
        criarTabuleiro(); // recria o tabuleiro com novo tamanho

        // Se houver um cavalo no tabuleiro, redesenha ele
        if (cavaloLinha != -1) {
            atualizarVisual();
        }
    }
}