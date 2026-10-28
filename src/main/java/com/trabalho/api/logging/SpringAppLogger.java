package com.trabalho.api.logging;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cópia do AppLogger gerenciada pelo Spring (@Component).
 *
 * Aqui NÃO existe getInstance(), construtor privado nem dupla checagem:
 * quem garante a instância única é o container do Spring, porque o
 * escopo padrão de um bean é "singleton" (uma instância por ApplicationContext).
 *
 * @Lazy: o bean só é criado quando alguém o injeta. Na versão final o projeto
 * usa o AppLogger clássico, então este bean não é criado e não abre o app.log.
 */
@Lazy
@Component
public class SpringAppLogger {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final PrintWriter arquivo;

    // Construtor público: o Spring é que chama, uma única vez
    public SpringAppLogger() {
        try {
            this.arquivo = new PrintWriter(new FileWriter("app.log", true), true);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível abrir app.log", e);
        }
    }

    public void debug(String origem, String mensagem) {
        log(AppLogger.Nivel.DEBUG, origem, mensagem, null);
    }

    public void info(String origem, String mensagem) {
        log(AppLogger.Nivel.INFO, origem, mensagem, null);
    }

    public void erro(String origem, String mensagem) {
        log(AppLogger.Nivel.ERRO, origem, mensagem, null);
    }

    public void erro(String origem, String mensagem, Throwable ex) {
        log(AppLogger.Nivel.ERRO, origem, mensagem, ex);
    }

    private synchronized void log(AppLogger.Nivel nivel, String origem, String mensagem, Throwable ex) {
        String linha = String.format("%s [%-5s] [%s] %s - %s",
                LocalDateTime.now().format(FORMATO), nivel,
                Thread.currentThread().getName(), origem, mensagem);
        if (ex != null) {
            linha += " | causa: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
        (nivel == AppLogger.Nivel.ERRO ? System.err : System.out).println(linha);
        arquivo.println(linha);
    }

    // Vantagem do Spring: ciclo de vida gerenciado (fecha o arquivo no shutdown)
    @PreDestroy
    public void fechar() {
        arquivo.close();
    }
}
