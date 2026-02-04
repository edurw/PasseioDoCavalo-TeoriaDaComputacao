module teoriadacomputacao.passeiodocavalo {
    requires javafx.controls;
    requires javafx.fxml;


    opens teoriadacomputacao.passeiodocavalo to javafx.fxml;
    exports teoriadacomputacao.passeiodocavalo;
}