package teoriadacomputacao.passeiodocavalo;

import javafx.geometry.Pos;

import java.util.ArrayDeque;
import java.util.ArrayList;

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

    private boolean posicaoValida(Posicao p){
        return (p.linha >= 0 && p.linha < tamanhoTabuleiro) &&
                (p.coluna >= 0 && p.coluna < tamanhoTabuleiro) &&
                (!tabuleiro[p.linha][p.coluna]);
    }

    public Posicao PushMovimento(Posicao p){
        pilhaCaminho.push(p);
        return pilhaCaminho.peek();
    }

    public Posicao PopMovimento(){
        pilhaCaminho.pop();
        return pilhaCaminho.peek();
    }
}
