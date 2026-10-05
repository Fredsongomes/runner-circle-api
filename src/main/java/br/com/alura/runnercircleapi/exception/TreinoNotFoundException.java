package br.com.alura.runnercircleapi.exception;

public class TreinoNotFoundException extends RuntimeException {

    public TreinoNotFoundException(Long id) {
        super("treino " + id + " não encontrado");
    }
}
