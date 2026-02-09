package teoriadacomputacao.passeiodocavalo;

import javafx.geometry.Pos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PasseioCavalo {

    private final MovimentoListener listener;
    private volatile boolean cancelado = false;

    public boolean IsFechado = false;
    public int Passos = 0;

//    private boolean otimizado;
    private boolean otimizar;
    private boolean usarPrioridadeBordas;
    private boolean priorizarCantos;
    private boolean usarConectividade;
    private boolean usarSubdivisao;

    private int tamanhoTabuleiro = 8;
    private boolean[][] tabuleiro = new boolean[8][8];
    private ArrayDeque<Posicao> pilhaCaminho = new ArrayDeque<Posicao>();

    private final Posicao[] movimentos =
            {
                    new Posicao(-2, 1), // cima-direita
                    new Posicao(-1, 2), // direita-cima
                    new Posicao(1, 2),  // direita-baixo
                    new Posicao(2, 1),  // baixo-direita
                    new Posicao(2, -1), // baixo-esquerda
                    new Posicao(1, -2), // esquerda-baixo
                    new Posicao(-1, -2), // esquerda-cima
                    new Posicao(-2, -1), // cima-esquerda
            } ;

    public PasseioCavalo(int tamanho, Posicao posicaoInicial, boolean otimizar, boolean usarPrioridadeBordas, MovimentoListener listener) {
        this.tamanhoTabuleiro = tamanho;
        this.tabuleiro = new boolean[tamanho][tamanho];
        this.pilhaCaminho = new ArrayDeque<Posicao>();
        this.pilhaCaminho.add(posicaoInicial);
        this.otimizar = otimizar;
        this.usarPrioridadeBordas = usarPrioridadeBordas;
        this.listener = listener;
        this.cancelado = false;

        tabuleiro[posicaoInicial.linha][posicaoInicial.coluna] = true;
        Passos = 1;

        if(listener != null)
            listener.aoMover(posicaoInicial, false);
    }

    public void cancelar() {
        this.cancelado = true;
    }

    public void setPriorizarCantos(boolean v){ this.priorizarCantos = v; }
    public void setUsarConectividade(boolean v){ this.usarConectividade = v; }
    public void setUsarSubdivisao(boolean v){ this.usarSubdivisao = v; }

    public boolean executaPasseio(){
        if (cancelado) return false;

        Posicao posicaoAtual = pilhaCaminho.peek();
        if(posicaoAtual == null) return false;

        List<Posicao> proximasPosicoes = new ArrayList<>();
        for(Posicao movimento : movimentos){
            Posicao p = posicaoAtual.retornaNovaPosicao(movimento);
            if(posicaoValida(p))
                proximasPosicoes.add(p);
        }

        Comparator<Posicao> comparator = null;

        if (otimizar)
            comparator = Comparator.comparingInt(this::contaJogadasPossiveis);

        if (usarPrioridadeBordas)
            comparator = (comparator == null)
                    ? Comparator.comparingInt(this::distanciaBorda)
                    : comparator.thenComparingInt(this::distanciaBorda);

        if (priorizarCantos)
            comparator = (comparator == null)
                    ? Comparator.comparingInt(this::distanciaCanto)
                    : comparator.thenComparingInt(this::distanciaCanto);

        if (usarSubdivisao) {
            int quadranteAtual = quadrante(posicaoAtual);

            Comparator<Posicao> qComp =
                    Comparator.comparingInt(p -> quadrante(p) == quadranteAtual ? 0 : 1);

            comparator = (comparator == null) ? qComp : comparator.thenComparing(qComp);
        }

        if (comparator != null)
            proximasPosicoes.sort(comparator);


        for(Posicao novaPosicao : proximasPosicoes){
            if (cancelado) return false;

            if(posicaoValida(novaPosicao))
            {
                if(usarConectividade && criaIsolamento(novaPosicao))
                    continue;

                pushMovimento(novaPosicao);

                if(verificaFinal()) return true; // Condição de quebra

                if(executaPasseio()) return true; // Retorna true se a recursão alcancou ponto de quebra

                if (!cancelado) {
                    popMovimento(); // Desfaz movimento que não houve sucesso
                }
            }
        }

        return false;
    }

    // Verifica se cavalo passou por todas as casas
    private boolean verificaFinal()
    {
        if(pilhaCaminho.size() == tamanhoTabuleiro * tamanhoTabuleiro){
            verificaFechado();
            return true;
        }
        return false;
    }

    //Define a solução como fechada se algum proximo passo termine na posicao inicial
    private void verificaFechado()
    {
        Posicao posicaoAtual = pilhaCaminho.peek();
        if(posicaoAtual == null) return;

        for(Posicao movimento : movimentos){
            if(posicaoAtual.retornaNovaPosicao(movimento).equals(pilhaCaminho.peekLast())){
                IsFechado = true;
                return;
            }
        }
    }


    // Realiza movimento do cavalo
    public void pushMovimento(Posicao p){
        pilhaCaminho.push(p);
        Passos++;
        tabuleiro[p.linha][p.coluna] = true;

        if(listener != null)
            listener.aoMover(p, false);
    }

    // Desfaz movimento do cavalo
    public Posicao popMovimento(){
        Posicao p = pilhaCaminho.pop();
        tabuleiro[p.linha][p.coluna] = false;

        if(listener != null)
            listener.aoMover(p, true);

        return pilhaCaminho.peek();
    }

    // Verifica se a posição do cavelo é válida no tabuleiro
    private boolean posicaoValida(Posicao p){
        return (p.linha >= 0 && p.linha < tamanhoTabuleiro) &&
                (p.coluna >= 0 && p.coluna < tamanhoTabuleiro) &&
                (!tabuleiro[p.linha][p.coluna]);
    }

    private int contaJogadasPossiveis(Posicao posicaoOrigem) {
        int jogadas = 0;

        for (Posicao movimento : movimentos) {
            Posicao p = posicaoOrigem.retornaNovaPosicao(movimento);

            if (posicaoValida(p))
                jogadas++;
        }

        return jogadas;
    }

    public interface MovimentoListener {
        void aoMover(Posicao posicao, boolean desfazendo);
    }

    /**
     * Heurística de prioridade de bordas.
     *
     * Objetivo:
     * Priorizar posições mais próximas das bordas do tabuleiro,
     * evitando que regiões periféricas fiquem inacessíveis no final
     * do passeio.
     *
     * Funcionamento:
     * As posições candidatas são ordenadas pela distância mínima até
     * a borda do tabuleiro, dando preferência às mais próximas.
     *
     * Características:
     * - Ajuda a evitar regiões isoladas.
     * - Pode ser combinada com outras heurísticas.
     */
    private int distanciaBorda(Posicao p) {
        int dLinha = Math.min(p.linha, tamanhoTabuleiro - 1 - p.linha);
        int dColuna = Math.min(p.coluna, tamanhoTabuleiro - 1 - p.coluna);
        return Math.min(dLinha, dColuna);
    }

    /**
     * Heurística de prioridade de cantos.
     *
     * Objetivo:
     * Visitar regiões próximas aos cantos do tabuleiro mais cedo,
     * evitando que essas casas se tornem difíceis de alcançar no
     * final do percurso.
     *
     * Funcionamento:
     * As posições candidatas são ordenadas pela distância até o canto
     * mais próximo, priorizando as menores distâncias.
     *
     * Características:
     * - Complementa a estratégia de bordas.
     * - Reduz probabilidade de bloqueios tardios.
     */
    private int distanciaCanto(Posicao p){
        int d1 = p.linha + p.coluna;
        int d2 = p.linha + (tamanhoTabuleiro-1 - p.coluna);
        int d3 = (tamanhoTabuleiro-1 - p.linha) + p.coluna;
        int d4 = (tamanhoTabuleiro-1 - p.linha) + (tamanhoTabuleiro-1 - p.coluna);

        return Math.min(Math.min(d1,d2), Math.min(d3,d4));
    }

    /**
     * Heurística de subdivisão de regiões.
     *
     * Objetivo:
     * Reduzir mudanças frequentes entre regiões do tabuleiro,
     * mantendo o cavalo explorando a mesma área antes de migrar
     * para outras partes.
     *
     * Funcionamento:
     * O tabuleiro é dividido em quadrantes. Movimentos que permanecem
     * no mesmo quadrante atual recebem prioridade sobre movimentos
     * que levam a outros quadrantes.
     *
     * Características:
     * - Mantém a busca mais organizada espacialmente.
     * - Pode diminuir caminhos redundantes.
     */
    private int quadrante(Posicao p){
        int metade = tamanhoTabuleiro/2;
        int qLinha = p.linha < metade ? 0 : 1;
        int qCol = p.coluna < metade ? 0 : 1;
        return qLinha*2 + qCol;
    }

    /**
     * Heurística de conectividade (Evitar casas isoladas).
     *
     * Objetivo:
     * Impedir que um movimento deixe casas restantes sem nenhuma
     * possibilidade futura de acesso.
     *
     * Funcionamento:
     * Antes de aceitar um movimento, o algoritmo simula a jogada e
     * verifica se alguma casa livre do tabuleiro ficaria sem
     * movimentos possíveis. Caso isso ocorra, o movimento é descartado.
     *
     * Características:
     * - Reduz drasticamente falhas tardias.
     * - Aumenta o custo de cada verificação, porém diminui o número
     * total de caminhos explorados.
     */
    private boolean criaIsolamento(Posicao p){

        tabuleiro[p.linha][p.coluna] = true;

        for(int i=0;i<tamanhoTabuleiro;i++){
            for(int j=0;j<tamanhoTabuleiro;j++){
                if(!tabuleiro[i][j]){

                    int saidas = 0;

                    for(Posicao m: movimentos){
                        Posicao n = new Posicao(i+m.linha, j+m.coluna);
                        if(posicaoValida(n))
                            saidas++;
                    }

                    if(saidas==0){
                        tabuleiro[p.linha][p.coluna] = false;
                        return true;
                    }
                }
            }
        }

        tabuleiro[p.linha][p.coluna] = false;
        return false;
    }

}
