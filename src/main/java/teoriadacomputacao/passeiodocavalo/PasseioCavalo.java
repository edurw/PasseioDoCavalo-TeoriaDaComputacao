package teoriadacomputacao.passeiodocavalo;

import javafx.geometry.Pos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class PasseioCavalo {
    private int tamanhoTabuleiro = 8;
    private boolean[][] tabuleiro = new boolean[8][8];
    private ArrayDeque<Posicao> pilhaCaminho = new ArrayDeque<Posicao>();

    public PasseioCavalo(int tamanho, Posicao posicaoInicial) {
        this.tamanhoTabuleiro = tamanho;
        this.tabuleiro = new boolean[tamanho][tamanho];
        this.pilhaCaminho = new ArrayDeque<Posicao>();
        this.pilhaCaminho.add(posicaoInicial);
    }

    public boolean executaPasseio(){

        Posicao posicaoAtual = pilhaCaminho.peek();

        Posicao movimento = null;

        movimento = cimaDireita(posicaoAtual);

        if(movimento != null){
            pushMovimento(movimento);
            if (executaPasseio());
                return true;

            // Incompleto
        }
        return false;
    }

    public Posicao pushMovimento(Posicao p){
        pilhaCaminho.push(p);
        tabuleiro[p.linha][p.coluna] = true;
        return pilhaCaminho.peek();
    }

    public Posicao popMovimento(){
        Posicao p = pilhaCaminho.pop();
        tabuleiro[p.linha][p.coluna] = false;
        return pilhaCaminho.peek();
    }

//    public ArrayList<Posicao> retornaMovimentosValidos(Posicao p){
//        Posicao movimento;
//
//        movimento = cimaDireita(p);
//        if(movimento != null);
//
//    }

    private Posicao cimaDireita(Posicao p){
        Posicao movimento = new Posicao(p.linha - 2, p.coluna + 1);
        return posicaoValida(movimento)? movimento : null;
    }

    private Posicao direitaCima(Posicao p){
        Posicao movimento = new Posicao(p.linha - 1, p.coluna + 2);
        return posicaoValida(movimento)? movimento : null;
    }

    private Posicao direitaBaixo(Posicao p){
        Posicao movimento = new Posicao(p.linha + 1, p.coluna + 2);
        return posicaoValida(movimento)? movimento : null;
    }

    private Posicao baixoDireita(Posicao p){
        Posicao movimento = new Posicao(p.linha + 2, p.coluna + 1);
        return posicaoValida(movimento)? movimento : null;
    }

    private Posicao baixoEsquerda(Posicao p){
        Posicao movimento = new Posicao(p.linha + 2, p.coluna - 1);
        return posicaoValida(movimento)? movimento : null;
    }

    private Posicao esquerdaBaixo(Posicao p){
        Posicao movimento = new Posicao(p.linha + 1, p.coluna - 2);
        return posicaoValida(movimento)? movimento : null;
    }

    private Posicao esquerdaCima(Posicao p){
        Posicao movimento = new Posicao(p.linha - 1, p.coluna - 2);
        return posicaoValida(movimento)? movimento : null;
    }

    private Posicao cimaEsquerda(Posicao p){
        Posicao movimento = new Posicao(p.linha - 2, p.coluna - 1);

        return posicaoValida(movimento)? movimento : null;
    }

    private boolean posicaoValida(Posicao p){
        return (p.linha >= 0 && p.linha < tamanhoTabuleiro) &&
                (p.coluna >= 0 && p.coluna < tamanhoTabuleiro) &&
                (!tabuleiro[p.linha][p.coluna]);
    }

}
