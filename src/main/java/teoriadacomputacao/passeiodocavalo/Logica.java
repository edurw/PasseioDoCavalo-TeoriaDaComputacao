package teoriadacomputacao.passeiodocavalo;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

public class Logica {
    // logica dos algoritmos
    private PasseioCavalo passeio;

    private final int TAM;
    private double tamanhoTabuleiro;
    // grid visual
    private final GridPane tabuleiro;
    // referência visual
    private final StackPane[][] casas;

    private final int[][] estado;

    // posição atual do cavalo
    private int cavaloLinha = -1;
    private int cavaloColuna = -1;

    // movimentos validos pro cavalo
    private final int[][] MOVIMENTOS = {
            {2,1},{1,2},{-1,2},{-2,1},
            {-2,-1},{-1,-2},{1,-2},{2,-1}
    };

    // contagem de movimentos
    private int movimentosTotais = 0;
    private int movimentosAtuais = 0;
    private int iteracoes = 0;
    private int descobertos = 0;

    private int inicioLinha = -1;
    private int inicioColuna = -1;
    private boolean isFechada = false;
    private long tempoInicio = 0;
//    private final long tempoFim = 0;

    private boolean executando = false;
    private boolean cancelarExecucao = false;
    private boolean finalizado = false;
    private Thread threadExecucao;

    private Label lblPosicao;
    private Label lblMovimentosTotais;
    private Label lblMovimentosAtuais;
    private Label lblIteracoes;
    private Label lblTempo;
    private Label lblSolucao;
    private Label lblDescobertos;

    // modo do algoritmo
    public enum Modo {
        NENHUM,
        FORCA_BRUTA,
        PODA
    }

    public void iniciarForcaBruta() {
        resetExecucao();
        resetTabuleiro();
        modo = Modo.FORCA_BRUTA;
    }

    public void iniciarPoda() {
        resetExecucao();
        resetTabuleiro();
        modo = Modo.PODA;
    }


    private Modo modo = Modo.NENHUM;

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

            if (modo == Modo.FORCA_BRUTA) {
                executarForcaBruta();
            }
            else if (modo == Modo.PODA) {
                executarPoda();
            }
        }
    }


    private void iniciarPasseio(int linha, int coluna) {
        resetExecucao();
        resetTabuleiro();

        passeio = new PasseioCavalo(
                TAM,
                new Posicao(linha, coluna),
                modo == Modo.PODA,
                (pos, desfazendo) -> {

                    if(cancelarExecucao || finalizado) return; // caso resete, cancele o jogo

                    try { Thread.sleep(5); } catch (Exception ignored) {}

                    javafx.application.Platform.runLater(() -> {

                        if(cancelarExecucao) return;

                        if (desfazendo) { // se esta efetuando backtracking
                            estado[pos.linha][pos.coluna] = 0;

                            movimentosAtuais--; // profundidade diminui
                            descobertos--; // diminuir descobertos, pq esta voltando, entao nao esta mais descoberto
                            iteracoes++; // aqui aumenta iteracao, indicando que precisou fazer uma revisao nos movimentos
                        } else {
                            movimentosTotais++;
                            movimentosAtuais++;

                            if(estado[pos.linha][pos.coluna] == 0)
                                descobertos++;

                            // aparentemente aqui tem um erro, usar movimentos e nao descobertos
                            estado[pos.linha][pos.coluna] = movimentosAtuais;
//                            estado[pos.linha][pos.coluna] = descobertos;

                            cavaloLinha = pos.linha;
                            cavaloColuna = pos.coluna;
                        }

                        //  potencial problema na solucao, que permite o algoritmo rodar por alguns instantes
                        if(descobertos == TAM*TAM || descobertos >= TAM*TAM){
//                            if(descobertos >= TAM*TAM){

                            finalizado = true;
                            executando = false;

                            // evitar race condiction
                            cancelarExecucao = true;

                            // talvez melhore a execucao ao encerrar
                            if(threadExecucao != null)
                                threadExecucao.interrupt();
                        }

                        atualizarVisual();
                        atualizarMetricas();
                    });
                }

        );

        tempoInicio = System.nanoTime();
        cavaloLinha = linha;
        cavaloColuna = coluna;
        inicioLinha = linha;
        inicioColuna = coluna;

//        movimentos = 1;
//        estado[linha][coluna] = movimentos;
        movimentosTotais = 1;
        movimentosAtuais = 1;
        estado[linha][coluna] = movimentosAtuais;

        descobertos = 1;

        atualizarVisual();
        atualizarMetricas();
    }

    private long getTempoDecorridoMs() {
        if (tempoInicio == 0) return 0;
        return (System.nanoTime() - tempoInicio) / 1_000_000;
    }

    private void moverCavalo(int linha, int coluna) {
        movimentosTotais++;
        movimentosAtuais++;
//        movimentos++; // qualquer movimento conta

        // se ainda não visitado
        if (estado[linha][coluna] == 0) {
            descobertos++;
        }

        estado[linha][coluna] = movimentosAtuais;
//        estado[linha][coluna] = movimentos;

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
            Label movimentosTotais,
            Label movimentosAtuais,
            Label iteracoes,
            Label tempo,
            Label solucao,
            Label descobertos
    ) {
        this.lblPosicao = posicao;
        this.lblMovimentosTotais = movimentosTotais;
        this.lblMovimentosAtuais = movimentosAtuais;
        this.lblIteracoes = iteracoes;
        this.lblTempo = tempo;
        this.lblSolucao = solucao;
        this.lblDescobertos = descobertos;

        atualizarMetricas();
    }

    private void atualizarMetricas() {
        if (lblPosicao != null) {
            if (cavaloLinha == -1)
                lblPosicao.setText("Posição: -");
            else
                lblPosicao.setText("Posição: (" + cavaloLinha + ", " + cavaloColuna + ")");
        }

        if (lblMovimentosTotais != null)
            lblMovimentosTotais.setText("Total: " + movimentosTotais);

        if (lblMovimentosAtuais != null)
            lblMovimentosAtuais.setText("Atual: " + movimentosAtuais);


        if (lblTempo != null)
            lblTempo.setText("Tempo: " + getTempoDecorridoMs() + " ms");

        if (lblIteracoes != null)
            lblIteracoes.setText("Iterações: " + iteracoes);

        if (lblDescobertos != null)
            lblDescobertos.setText("Grids Descobertos: " + descobertos);

        if (lblSolucao != null) {
            int movimentosParaConcluir = (TAM * TAM) - 1;

            if (descobertos < movimentosParaConcluir) {
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
        if (descobertos < movimentosParaConcluir) return;

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

        if(threadExecucao != null && threadExecucao.isAlive()){
            threadExecucao.interrupt();
            threadExecucao = null;
        }

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

        movimentosTotais = 0;
        movimentosAtuais = 0;
        descobertos = 0;
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

    /// FORCA BRUTA
    private void executarForcaBruta() {

        executando = true;
        cancelarExecucao = false;

        threadExecucao = new Thread(() -> {

            boolean encontrou = passeio.executaPasseio();

            isFechada = passeio.IsFechado;

            javafx.application.Platform.runLater(this::verificarSolucao);

            executando = false;

        });
        threadExecucao.start();
    }

    /// PODA (NAO USAR Warnsdorff)
    private void executarPoda() {

        executando = true;
        cancelarExecucao = false;

        threadExecucao = new Thread(() -> {

            boolean encontrou = passeio.executaPasseio();

            isFechada = passeio.IsFechado;

            javafx.application.Platform.runLater(this::verificarSolucao);

            executando = false;

        });
        threadExecucao.start();
    }

}
