package com.trabalho.api;

import com.trabalho.api.logging.SpringAppLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ProdutoControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void crudCompletoComStatusCorretos() throws Exception {
        String json = """
                {"nome":"Teclado","descricao":"Mecânico ABNT2","preco":199.90,"estoque":10}""";

        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        mvc.perform(get("/produtos")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mvc.perform(get("/produtos/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Teclado"));

        mvc.perform(put("/produtos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Teclado RGB","descricao":"Mecânico","preco":249.90,"estoque":5}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Teclado RGB"));

        mvc.perform(delete("/produtos/1")).andExpect(status().isNoContent());

        mvc.perform(get("/produtos/1")).andExpect(status().isNotFound());
        mvc.perform(put("/produtos/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/produtos/1")).andExpect(status().isNotFound());
    }

    @Test
    void springGaranteUmUnicoBeanDoLogger(@Autowired ApplicationContext ctx) {
        assertSame(ctx.getBean(SpringAppLogger.class), ctx.getBean(SpringAppLogger.class));
    }
}
