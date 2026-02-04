module br.univali.horseteoriadacomputacao {
    requires javafx.controls;
    requires javafx.fxml;

    opens br.univali.horseteoriadacomputacao to javafx.fxml;
    exports br.univali.horseteoriadacomputacao;
}