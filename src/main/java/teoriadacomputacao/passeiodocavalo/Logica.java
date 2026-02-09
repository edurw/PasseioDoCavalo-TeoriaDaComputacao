package teoriadacomputacao.passeiodocavalo;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.stage.Screen;

public class Logica {
    // logica dos algoritmos
    private PasseioCavalo passeio;

    // pra habilitar botoes depois de finalizar/resetar
    private Runnable onBuscaFinalizada;

    public void setOnBuscaFinalizada(Runnable r) {
        this.onBuscaFinalizada = r;
    }

    private java.util.function.Consumer<Boolean> componentSwitchState;
    
    public void setComponentSwitchState(java.util.function.Consumer<Boolean> controle) {
        this.componentSwitchState = controle;
    }

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

    // posição anterior do cavalo
    private int cavaloLinhaAnterior = -1;
    private int cavaloColunaAnterior = -1;

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

    private boolean cancelarExecucao = false;
    private boolean finalizado = false;
    private Thread threadExecucao;
    private boolean executando = false;

    public boolean isExecutando() {
        return executando;
    }

    private Label lblPosicao;
    private Label lblMovimentosTotais;
    private Label lblMovimentosAtuais;
    private Label lblIteracoes;
    private Label lblTempo;
    private Label lblSolucao;
    private Label lblDescobertos;

    // modo do algoritmo
    public enum Modo {
        NENHUM, // nenhum algoritmo
        FORCA_BRUTA, // Busca em profundidade sem heurísticas (testa todas as possibilidades)
        PODA, // Busca com heurística de otimização (ex.: ordenação estratégica dos movimentos)
        BORDAS, // Busca priorizando posições próximas às bordas do tabuleiro
        CANTOS,
        SEGMENTOS,
        CONECTIVIDADE
    }

    public void iniciarForcaBruta() {
        modo = Modo.FORCA_BRUTA;
    }

    public void iniciarPoda() {
        modo = Modo.PODA;
    }

    public void iniciarBordas() {
        modo = Modo.BORDAS;
    }

    public void iniciarCantos() {
        modo = Modo.CANTOS;
    }

    public void iniciarSegmentacao() {
        modo = Modo.SEGMENTOS;
    }

    public void iniciarConectividade() {
        modo = Modo.CONECTIVIDADE;
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
            // DESABILITA BOTÕES (quando clica no tabuleiro)
            if (componentSwitchState != null) {
                componentSwitchState.accept(false);
            }
            iniciarPasseio(linha, coluna);
            executarBusca();
        }
    }


    private void iniciarPasseio(int linha, int coluna) {
        resetExecucao();
        resetTabuleiro();

        passeio = new PasseioCavalo(
                TAM,
                new Posicao(linha, coluna),
                modo == Modo.PODA,
                modo == Modo.BORDAS,

                (pos, desfazendo) -> {

                    if(cancelarExecucao || finalizado) return; // caso resete, cancele o jogo

                    try { Thread.sleep(5); } catch (Exception ignored) {}

                    javafx.application.Platform.runLater(() -> {

                        if(cancelarExecucao || finalizado) return;

                        if (desfazendo) { // se esta efetuando backtracking
                            estado[pos.linha][pos.coluna] = 0;
                            movimentosAtuais--; // profundidade diminui
                            descobertos--; // diminuir descobertos, pq esta voltando, entao nao esta mais descoberto
                            iteracoes++; // aqui aumenta iteracao, indicando que precisou fazer uma revisao nos movimentos

                            // Atualizar posição anterior ao desfazer
                            cavaloLinhaAnterior = cavaloLinha;
                            cavaloColunaAnterior = cavaloColuna;
                        } else {
                            movimentosTotais++;
                            movimentosAtuais++;

                            if(estado[pos.linha][pos.coluna] == 0)
                                descobertos++;

                            // Salvar posição anterior ANTES de atualizar atual
                            cavaloLinhaAnterior = cavaloLinha;
                            cavaloColunaAnterior = cavaloColuna;

                            estado[pos.linha][pos.coluna] = movimentosAtuais;

                            cavaloLinha = pos.linha;
                            cavaloColuna = pos.coluna;
                        }

                        //  potencial problema na solucao, que permite o algoritmo rodar por alguns instantes
                        if(descobertos == TAM*TAM || descobertos >= TAM*TAM){

                            finalizado = true;
                            executando = false;

                            // evitar race condiction
                            cancelarExecucao = true;

                            if (onBuscaFinalizada != null) {
                                javafx.application.Platform.runLater(onBuscaFinalizada);
                            }
                        }

                        atualizarVisual();
                        atualizarMetricas();
                    });
                }

        );

        passeio.setPriorizarCantos(modo == Modo.CANTOS);
        passeio.setUsarSubdivisao(modo == Modo.SEGMENTOS);
        passeio.setUsarConectividade(modo == Modo.CONECTIVIDADE);

        tempoInicio = System.nanoTime();
        cavaloLinha = linha;
        cavaloColuna = coluna;
        cavaloLinhaAnterior = -1;
        cavaloColunaAnterior = -1;
        inicioLinha = linha;
        inicioColuna = coluna;

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

    private void limparTabuleiro() {
        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < TAM; j++) {
                estado[i][j] = 0;
            }
        }
    }

    private void atualizarVisual() {
        Screen screen = Screen.getPrimary();
        double dpi = screen.getDpi();

        double tamanhoCasa = tamanhoTabuleiro / TAM;
        double tamanhoCavalo = tamanhoCasa * 0.7 * dpi / 100;

        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < TAM; j++) {
                StackPane casa = casas[i][j];
                casa.getChildren().clear();

                boolean clara = (i + j) % 2 == 0;

                String corBase = clara ? "#f0d9b5" : "#b58863";
                String corVisitada = clara ? "#cfe8ff" : "#7fa7d9";

                String corFinal = (estado[i][j] > 0) ? corVisitada : corBase;

                casa.setStyle(
                        "-fx-background-color: " + corFinal +
                                "; -fx-border-color: black;"
                );

                // Nunca mostrar número na posição atual do cavalo
                if (estado[i][j] > 0 && (i != cavaloLinha || j != cavaloColuna)) {
                    Label passo = new Label(String.valueOf(estado[i][j]));
                    passo.setStyle("-fx-font-weight: bold; -fx-font-size: " + (tamanhoCasa * 0.4) + "px;");
                    casa.getChildren().add(passo);
                }

                // Mostrar cavalo na posição atual
                if (i == cavaloLinha && j == cavaloColuna) {
                    Label cavalo = new Label("♞");
                    cavalo.setStyle("-fx-font-size: " + tamanhoCavalo + "px; -fx-text-fill: black;");
                    casa.getChildren().add(cavalo);
                }
            }
        }
        // aqui pra mostrar os movimentos validos do cavalo visualmente
        if (cavaloLinha != -1 && !finalizado) {
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
            lblMovimentosTotais.setText("Movimentos totais: " + movimentosTotais);

        if (lblMovimentosAtuais != null)
            lblMovimentosAtuais.setText("Movimento Atual: " + movimentosAtuais);

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
        if (onBuscaFinalizada != null) {
            javafx.application.Platform.runLater(onBuscaFinalizada);
        }
    }

    private boolean movimentoValido(int l1, int c1, int l2, int c2) {
        int dl = Math.abs(l1 - l2);
        int dc = Math.abs(c1 - c2);
        return (dl == 2 && dc == 1) || (dl == 1 && dc == 2);
    }

    private void cancelarThreadAnterior() {
        if (threadExecucao != null && threadExecucao.isAlive()) {
            cancelarExecucao = true;

            if (passeio != null) {
                passeio.cancelar();
            }

            try {
                // Aguarda até 2 segundos para thread terminar
                threadExecucao.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Se ainda estiver viva, interrompe forçadamente
            if (threadExecucao.isAlive()) {
                threadExecucao.interrupt();
            }
        }
    }

    public void reset() {
        cancelarThreadAnterior();

        cancelarExecucao = true;
        executando = false;
        finalizado = false;
        tempoInicio = 0;

        resetTabuleiro();
        atualizarVisual();
        atualizarMetricas();

        if (onBuscaFinalizada != null) {
            Platform.runLater(onBuscaFinalizada);
        }
    }

    // reseta a execucao
    private void resetExecucao() {
        cancelarThreadAnterior();

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

        cavaloLinhaAnterior = -1;
        cavaloColunaAnterior = -1;

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
    private void executarBusca() {
        executando = true;
        cancelarExecucao = false;

        threadExecucao = new Thread(() -> {
            try {
                passeio.executaPasseio();

                // VERIFICAR SE NÃO FOI CANCELADO
                if (!cancelarExecucao) {
                    isFechada = passeio.IsFechado;

                    javafx.application.Platform.runLater(this::verificarSolucao);
                }
            } catch (Exception e) {
                // Log do erro para debug
                System.err.println("Erro na execução do passeio: " + e.getMessage());
                e.printStackTrace();
            } finally {
                executando = false;

                if (onBuscaFinalizada != null && !cancelarExecucao) {
                    javafx.application.Platform.runLater(onBuscaFinalizada);
                }
            }
        });
        threadExecucao.setDaemon(true);
        threadExecucao.start();
    }

}
