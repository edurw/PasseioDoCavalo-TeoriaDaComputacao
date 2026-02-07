package teoriadacomputacao.passeiodocavalo;

public class Posicao {
    public int linha;
    public int coluna;

    public Posicao (int linha, int coluna)
    {
        this.linha = linha;
        this.coluna = coluna;
    }

    public Posicao retornaNovaPosicao(Posicao p)
    {
        return new Posicao(linha + p.linha, linha + p.coluna);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Posicao)) return false;
        Posicao other = (Posicao) o;
        return this.linha == other.linha && this.coluna == other.coluna;
    }
}
