package teoriadacomputacao.passeiodocavalo;

import javafx.geometry.Pos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PasseioCavalo {

    private final MovimentoListener listener;

    public boolean IsFechado = false;
    public int Passos = 0;

    private boolean otimizado;
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

    public PasseioCavalo(int tamanho, Posicao posicaoInicial, boolean otimizar, MovimentoListener listener) {
        this.tamanhoTabuleiro = tamanho;
        this.tabuleiro = new boolean[tamanho][tamanho];
        this.pilhaCaminho = new ArrayDeque<Posicao>();
        this.pilhaCaminho.add(posicaoInicial);
        this.otimizado = otimizar;
        this.listener = listener;

        tabuleiro[posicaoInicial.linha][posicaoInicial.coluna] = true;
        Passos = 1;

        if(listener != null)
            listener.aoMover(posicaoInicial, false);
    }

    public boolean executaPasseio(){

        Posicao posicaoAtual = pilhaCaminho.peek();
        if(posicaoAtual == null) return false;

        List<Posicao> proximasPosicoes = new ArrayList<>();
        for(Posicao movimento : movimentos){
            Posicao p = posicaoAtual.retornaNovaPosicao(movimento);
            if(posicaoValida(p))
                proximasPosicoes.add(p);
        }

        if (otimizado)
            proximasPosicoes.sort(Comparator.comparingInt(this::contaJogadasPossiveis));

        for(Posicao novaPosicao : proximasPosicoes){
            if(posicaoValida(novaPosicao))
            {
                pushMovimento(novaPosicao);

                if(verificaFinal()) return true; // Condição de quebra

                if(executaPasseio()) return true; // Retorna true se a recursão alcancou ponto de quebra

                popMovimento(); // Desfaz movimento que não houve sucesso
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

}
