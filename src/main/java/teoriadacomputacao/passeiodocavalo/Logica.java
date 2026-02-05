package teoriadacomputacao.passeiodocavalo;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

import static java.lang.Thread.sleep;

public class Logica {

    private final int TAM;

    private double tamanhoTabuleiro;

    private final int[][] estado;

    // referência visual
    private final StackPane[][] casas;

    // grid visual
    private final GridPane tabuleiro;

    // modo do algoritmo
    public enum Modo {
        NENHUM,
        LIVRE,
        FORCA_BRUTA,
        BUSCA_PROFUNDIDADE,
        PODA
    }

    public void iniciarModoLivre() {
        resetExecucao();
        resetTabuleiro();
        modo = Modo.LIVRE;
    }

    public void iniciarForcaBruta() {
        resetExecucao();
        resetTabuleiro();
        modo = Modo.FORCA_BRUTA;
    }

    public void iniciarBuscaProfundidade() {
        resetExecucao();
        resetTabuleiro();
        modo = Modo.BUSCA_PROFUNDIDADE;
    }

    public void iniciarPoda() {
        resetExecucao();
        resetTabuleiro();
        modo = Modo.PODA;
    }


    private Modo modo = Modo.NENHUM;


    // posição atual do cavalo
    private int cavaloLinha = -1;
    private int cavaloColuna = -1;

    // movimentos validos pro cavalo
    private final int[][] MOVIMENTOS = {
            {2,1},{1,2},{-1,2},{-2,1},
            {-2,-1},{-1,-2},{1,-2},{2,-1}
    };

    // contagem de movimentos
    private int movimentos = 0;
    private int iteracoes = 0;

    private int inicioLinha = -1;
    private int inicioColuna = -1;
    private boolean isFechada = false;

    private long tempoInicio = 0;
//    private final long tempoFim = 0;

    private int visitados = 0;

    private boolean executando = false;
    private boolean cancelarExecucao = false;
    private boolean finalizado = false;

    private Label lblPosicao;
    private Label lblMovimentos;
    private Label lblIteracoes;
    private Label lblTempo;
    private Label lblSolucao;
    private Label lblVisitados;

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
        if (modo == Modo.NENHUM || executando || finalizado) return;

        if (cavaloLinha == -1) {
            iniciarPasseio(linha, coluna);

            if (modo == Modo.BUSCA_PROFUNDIDADE) {
                executarBuscaProfundidade();
            }

            if (modo == Modo.FORCA_BRUTA) {
                executarForcaBruta();
            }
            return;
        }

        if (modo == Modo.LIVRE) {
            if (!movimentoValido(cavaloLinha, cavaloColuna, linha, coluna)) return;
            moverCavalo(linha, coluna);
        }
    }


    private void iniciarPasseio(int linha, int coluna) {
        resetExecucao();
        resetTabuleiro();

        tempoInicio = System.nanoTime();
        cavaloLinha = linha;
        cavaloColuna = coluna;
        inicioLinha = linha;
        inicioColuna = coluna;

        movimentos = 1;
        estado[linha][coluna] = movimentos;
        visitados = 1;

        atualizarVisual();
        atualizarMetricas();
    }

    private void colocarCavalo(int linha, int coluna) {
        limparTabuleiro();
        tempoInicio = System.nanoTime();
        estado[linha][coluna] = 2;
        cavaloLinha = linha;
        cavaloColuna = coluna;

        inicioLinha = linha;
        inicioColuna = coluna;

        movimentos = 0;
        atualizarVisual();
        atualizarMetricas();
    }

    private long getTempoDecorridoMs() {
        if (tempoInicio == 0) return 0;
        return (System.nanoTime() - tempoInicio) / 1_000_000;
    }

    private void moverCavalo(int linha, int coluna) {
        movimentos++; // qualquer movimento conta

        // se ainda não visitado
        if (estado[linha][coluna] == 0) {
            visitados++;
        }

        estado[linha][coluna] = movimentos;

        cavaloLinha = linha;
        cavaloColuna = coluna;

        verificarSolucao();
        atualizarVisual();
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

                boolean clara = (i + j) % 2 == 0;
                casa.setStyle(
                        "-fx-background-color: " +
                                (clara ? "#f0d9b5" : "#b58863") +
                                "; -fx-border-color: black;"
                );

                if (estado[i][j] > 0) {
                    Label passo = new Label(String.valueOf(estado[i][j]));
                    passo.setStyle("-fx-font-weight: bold;");
                    casa.getChildren().add(passo);
                }

                if (i == cavaloLinha && j == cavaloColuna) {
                    Label cavalo = new Label("♞");
                    cavalo.setStyle("-fx-font-size: " + tamanhoCavalo + "px; -fx-text-fill: black;");
                    casa.getChildren().add(cavalo);
                }
            }
        }
        // aqui pra mostrar os movimentos validos do cavalo visualmente
        if (cavaloLinha != -1) {
            mostrarMovimentosValidos(cavaloLinha, cavaloColuna);
        }
    }

    private void mostrarMovimentosValidos(int l, int c) {
        for (int[] m : MOVIMENTOS) {
            int nl = l + m[0];
            int nc = c + m[1];

            if (dentro(nl, nc) && estado[nl][nc] == 0) {
                casas[nl][nc].setStyle(
                        "-fx-background-color: #7fc97f; -fx-border-color: black;"
                );
            }
        }
    }

    private boolean dentro(int l, int c) {
        return l >= 0 && l < TAM && c >= 0 && c < TAM;
    }

    public void setLabels(
            Label posicao,
            Label movimentos,
            Label iteracoes,
            Label tempo,
            Label solucao,
            Label visitados
    ) {
        this.lblPosicao = posicao;
        this.lblMovimentos = movimentos;
        this.lblIteracoes = iteracoes;
        this.lblTempo = tempo;
        this.lblSolucao = solucao;
        this.lblVisitados = visitados;

        atualizarMetricas();
    }

    private void atualizarMetricas() {
        if (lblPosicao != null) {
            if (cavaloLinha == -1)
                lblPosicao.setText("Posição: -");
            else
                lblPosicao.setText("Posição: (" + cavaloLinha + ", " + cavaloColuna + ")");
        }

        if (lblMovimentos != null)
            lblMovimentos.setText("Movimentos: " + movimentos);

        if (lblTempo != null)
            lblTempo.setText("Tempo: " + getTempoDecorridoMs() + " ms");

        if (lblIteracoes != null)
            lblIteracoes.setText("Iterações: " + iteracoes);

        if (lblVisitados != null)
            lblVisitados.setText("Grids Visitados: " + visitados);

        if (lblSolucao != null) {
            int movimentosParaConcluir = (TAM * TAM) - 1;

            if (visitados < movimentosParaConcluir) {
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
        if (visitados < movimentosParaConcluir) return;

        isFechada = movimentoValido(
                cavaloLinha,
                cavaloColuna,
                inicioLinha,
                inicioColuna
        );

        iteracoes++;     // <<< jogo terminou
        finalizado = true;
        executando = false;
    }

    private boolean movimentoValido(int l1, int c1, int l2, int c2) {
        int dl = Math.abs(l1 - l2);
        int dc = Math.abs(c1 - c2);
        return (dl == 2 && dc == 1) || (dl == 1 && dc == 2);
    }

    public void reset() {
        resetExecucao();
        modo = Modo.NENHUM;   // reset completo

        resetTabuleiro();

        atualizarVisual();
        atualizarMetricas();
    }

    // reseta a execucao
    private void resetExecucao() {
        cancelarExecucao = true;
        executando = false;
        finalizado = false;

        tempoInicio = 0;
        iteracoes = 0;
    }

    // reseta o tabuleiro
    private void resetTabuleiro() {

        cavaloLinha = -1;
        cavaloColuna = -1;

        inicioLinha = -1;
        inicioColuna = -1;

        movimentos = 0;
        visitados = 0;
        isFechada = false;

        limparTabuleiro();
    }

    public void redimensionar(double novoTamanho) {
        this.tamanhoTabuleiro = novoTamanho;
        criarTabuleiro(); // recria o tabuleiro com novo tamanho

        // Se houver um cavalo no tabuleiro, redesenha ele
        if (cavaloLinha != -1) {
            atualizarVisual();
        }
    }

    /// algoritmos

    /// helpers
    // helper pra esperar um tempinho
    private void sleep() {
        try { Thread.sleep(1); }
        catch (Exception ignored) {}
    }
    // backtracking
    private void desfazerMovimento(int linhaAnterior, int colunaAnterior,
                                   int linhaAtual, int colunaAtual) {

        estado[linhaAtual][colunaAtual] = 0;

        visitados--;
        movimentos--;

        cavaloLinha = linhaAnterior;
        cavaloColuna = colunaAnterior;
    }

    // para evitar race condition com javafx
    private void moverLogico(int linha, int coluna) {

        movimentos++;

        if (estado[linha][coluna] == 0)
            visitados++;

        estado[linha][coluna] = movimentos;

        cavaloLinha = linha;
        cavaloColuna = coluna;
    }

    /// FORCA BRUTA
    private void executarForcaBruta() {

        executando = true;
        cancelarExecucao = false;

        new Thread(() -> {

            java.util.Random rnd = new java.util.Random();

            while (visitados < TAM*TAM && !cancelarExecucao) {

                java.util.List<int[]> validos = new java.util.ArrayList<>();

                for (int[] m : MOVIMENTOS) {
                    int nl = cavaloLinha + m[0];
                    int nc = cavaloColuna + m[1];

                    if (dentro(nl,nc) && estado[nl][nc] == 0)
                        validos.add(new int[]{nl,nc});
                }

                // travou → acabou tentativa
                if (validos.isEmpty()) {
                    finalizado = true;
                    executando = false;
                    return;
                }

                int[] prox = validos.get(rnd.nextInt(validos.size()));

                moverLogico(prox[0], prox[1]);

                javafx.application.Platform.runLater(() -> {
                    atualizarVisual();
                    atualizarMetricas();
                });

                sleep();
            }

            javafx.application.Platform.runLater(this::verificarSolucao);

            executando = false;

        }).start();
    }

    /// DFS
    private void executarBuscaProfundidade() {

        executando = true;
        cancelarExecucao = false;
        new Thread(() -> {
            dfs(cavaloLinha, cavaloColuna);
            executando = false;
            cancelarExecucao = true;
        }).start();
    }

    private boolean dfs(int linha, int coluna) {

        if (cancelarExecucao || finalizado)
            return true;

        if (visitados == TAM*TAM) {
            finalizado = true;

            javafx.application.Platform.runLater(this::verificarSolucao);
            return true;
        }

        for (int[] m : MOVIMENTOS) {

            if (finalizado) return true;

            int nl = linha + m[0];
            int nc = coluna + m[1];

            if (!dentro(nl,nc) || estado[nl][nc] != 0)
                continue;

            moverLogico(nl,nc);

            javafx.application.Platform.runLater(() -> {
                atualizarVisual();
                atualizarMetricas();
            });

            sleep();

            if (dfs(nl,nc))
                return true;

            if (finalizado) return true;

            // BACKTRACK
            desfazerMovimento(linha, coluna, nl, nc);

            javafx.application.Platform.runLater(() -> {
                atualizarVisual();
                atualizarMetricas();
            });

            sleep();
        }

        return false;
    }
}
