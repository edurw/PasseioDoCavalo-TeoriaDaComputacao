package teoriadacomputacao.passeiodocavalo;

public class Posicao {
    public int linha;
    public int coluna;

    public Posicao (int linha, int coluna)
    {
        this.linha = linha;
        this.coluna = coluna;
    }

    private Posicao cimaDireita(){
        Posicao movimento = new Posicao(linha - 2, coluna + 1);
        return movimento;
    }

    private Posicao direitaCima(){
        Posicao movimento = new Posicao(linha - 1, coluna + 2);
        return movimento;
    }

    private Posicao direitaBaixo(){
        Posicao movimento = new Posicao(linha + 1, coluna + 2);
        return movimento;
    }

    private Posicao baixoDireita(){
        Posicao movimento = new Posicao(linha + 2, coluna + 1);
        return movimento;
    }

    private Posicao baixoEsquerda(){
        Posicao movimento = new Posicao(linha + 2, coluna - 1);
        return movimento;
    }

    private Posicao esquerdaBaixo(){
        Posicao movimento = new Posicao(linha + 1, coluna - 2);
        return movimento;
    }

    private Posicao esquerdaCima(){
        Posicao movimento = new Posicao(linha - 1, coluna - 2);
        return movimento;
    }

    private Posicao cimaEsquerda(){
        Posicao movimento = new Posicao(linha - 2, coluna - 1);
        return movimento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Posicao)) return false;
        Posicao other = (Posicao) o;
        return this.linha == other.linha && this.coluna == other.coluna;
    }
}
