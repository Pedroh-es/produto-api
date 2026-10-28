package com.trabalho.api;

import com.trabalho.api.logging.AppLogger;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AppLoggerTest {

    @Test
    void construtorDeveSerPrivado() throws Exception {
        Constructor<AppLogger> c = AppLogger.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(c.getModifiers()));
    }

    @Test
    void getInstanceSempreRetornaAMesmaInstancia() {
        assertSame(AppLogger.getInstance(), AppLogger.getInstance());
    }

    @Test
    void getInstanceEhThreadSafe() throws Exception {
        int threads = 200;
        Set<AppLogger> instancias = ConcurrentHashMap.newKeySet();
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(32);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                largada.await();
                instancias.add(AppLogger.getInstance());
                return null;
            });
        }
        largada.countDown(); // todas as threads chamam getInstance() ao mesmo tempo
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1, instancias.size());
    }

    @Test
    void mascararEscondeDadoSensivel() {
        assertEquals("*********00", AppLogger.mascarar("12345678900"));
    }
}
