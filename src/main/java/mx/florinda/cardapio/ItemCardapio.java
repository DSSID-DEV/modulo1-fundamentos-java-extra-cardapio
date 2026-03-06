package mx.florinda.cardapio;

import com.dssid.dev.persistence.config.annotation.Id;
import com.dssid.dev.persistence.config.annotation.JPColumn;
import com.dssid.dev.persistence.config.annotation.JPTable;

import java.math.BigDecimal;

@JPTable(name = "item_cardapio")
public class ItemCardapio {
    @Id
    private Long id;
    private String nome;
    private String descricao;
    private CategoriaCardapio categoria;
    private BigDecimal preco;
    @JPColumn(name = "preco_promocional", nullable = true)
    private BigDecimal precoPromocional;

    public ItemCardapio() {
    }

    public ItemCardapio(Long id, String nome, String descricao, CategoriaCardapio categoria, BigDecimal preco, BigDecimal precoPromocional) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
        this.precoPromocional = precoPromocional;
    }

    public enum CategoriaCardapio {
        ENTRADAS, PRATOS_PRINCIPAIS, BEBIDAS, SOBREMESA;
    }

    public ItemCardapio alteraPreco(BigDecimal novoPreco) {
        this.preco = novoPreco;
        return this;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public CategoriaCardapio getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaCardapio categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public BigDecimal getPrecoPromocional() {
        return precoPromocional;
    }

    public void setPrecoPromocional(BigDecimal precoPromocional) {
        this.precoPromocional = precoPromocional;
    }

    @Override
    public String toString() {
        return "ItemCardapio{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", categoria=" + categoria +
                ", preco=" + preco +
                ", precoPromocional=" + precoPromocional +
                '}';
    }
}
