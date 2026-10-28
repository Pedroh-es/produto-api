package com.trabalho.api.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger da aplicação implementado com o padrão Singleton clássico.
 *
 * - Construtor privado: ninguém de fora consegue dar "new AppLogger()".
 * - getInstance() estático com inicialização LAZY: a instância só é criada
 *   na primeira chamada.
 * - Thread-safe com "volatile + dupla checagem" (double-checked locking).
 * - Níveis: DEBUG, INFO e ERRO.
 * - Escreve no console e no arquivo app.log.
 * - Sem nenhuma anotação do Spring.
 */
public final class AppLogger {

    public enum Nivel { DEBUG, INFO, ERRO }

    private static final String ARQUIVO = "app.log";
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // volatile: garante que outras threads vejam o objeto totalmente construído
    private static volatile AppLogger instancia;

    private final PrintWriter arquivo;
    private Nivel nivelMinimo = Nivel.DEBUG;

    // Construtor privado
    private AppLogger() {
        try {
            // true = append (não apaga o log anterior); autoFlush = true
            this.arquivo = new PrintWriter(new FileWriter(ARQUIVO, true), true);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível abrir " + ARQUIVO, e);
        }
    }

    // Inicialização lazy + thread-safe (dupla checagem)
    public static AppLogger getInstance() {
        AppLogger local = instancia;           // 1ª leitura sem lock (rápida)
        if (local == null) {
            synchronized (AppLogger.class) {
                local = instancia;             // 2ª checagem, agora com lock
                if (local == null) {
                    instancia = local = new AppLogger();
                }
            }
        }
        return local;
    }

    public void setNivelMinimo(Nivel nivel) {
        this.nivelMinimo = nivel;
    }

    public void debug(String origem, String mensagem) {
        log(Nivel.DEBUG, origem, mensagem, null);
    }

    public void info(String origem, String mensagem) {
        log(Nivel.INFO, origem, mensagem, null);
    }

    public void erro(String origem, String mensagem) {
        log(Nivel.ERRO, origem, mensagem, null);
    }

    public void erro(String origem, String mensagem, Throwable ex) {
        log(Nivel.ERRO, origem, mensagem, ex);
    }

    // synchronized: duas threads não intercalam linhas no arquivo
    private synchronized void log(Nivel nivel, String origem, String mensagem, Throwable ex) {
        if (nivel.ordinal() < nivelMinimo.ordinal()) {
            return;
        }
        String linha = String.format("%s [%-5s] [%s] %s - %s",
                LocalDateTime.now().format(FORMATO),
                nivel,
                Thread.currentThread().getName(),
                origem,
                mensagem);
        if (ex != null) {
            // Só o tipo e a mensagem da exceção, sem stack trace com dados internos
            linha += " | causa: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }

        if (nivel == Nivel.ERRO) {
            System.err.println(linha);
        } else {
            System.out.println(linha);
        }
        arquivo.println(linha);
    }

    /**
     * Mascara dados sensíveis antes de logar. Ex.: CPF 123.456.789-00 -> ***.***.***-00
     * Senha e token nunca devem ser passados ao logger.
     */
    public static String mascarar(String valor) {
        if (valor == null || valor.length() <= 2) {
            return "***";
        }
        return "*".repeat(valor.length() - 2) + valor.substring(valor.length() - 2);
    }
}
