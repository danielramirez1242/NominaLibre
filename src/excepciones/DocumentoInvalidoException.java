package excepciones;

public class DocumentoInvalidoException extends Exception {
    public DocumentoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
