# API de Produtos — CRUD + Logger Singleton

Spring Boot 4.1.1 · Java 21 · JPA · H2 (memória) · Lombok
Arquitetura igual à do repositório da aula: `entities`, `repositories`, `services`, `controllers`.

## Como rodar

```bash
./mvnw spring-boot:run          # sobe na porta 8080
./gerar-app-log.sh              # em outro terminal: chama todas as rotas e gera o app.log
./mvnw test                     # testes do CRUD e do Singleton
```

Console do H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:produtosdb`, usuário `sa`).

## Parte 1 — CRUD (`/produtos`)

| Método | Rota | Sucesso | Erro |
|---|---|---|---|
| POST | `/produtos` | 201 Created | — |
| GET | `/produtos` | 200 OK (lista) | — |
| GET | `/produtos/{id}` | 200 OK | 404 Not Found |
| PUT | `/produtos/{id}` | 200 OK | 404 Not Found |
| DELETE | `/produtos/{id}` | 204 No Content | 404 Not Found |

- **Service** (`ProdutoService`): toda a lógica. Lança `RecursoNaoEncontradoException` quando o id não existe.
- **Controller** (`ProdutoController`): só recebe, chama o Service e devolve `ResponseEntity` com o status.
- **GlobalExceptionHandler**: converte `RecursoNaoEncontradoException` em 404, mantém o status dos erros padrão do Spring (ex.: 400 para id não numérico) e responde 500 para o resto. Todos registram ERRO.

Exemplo de corpo:

```json
{ "nome": "Teclado", "descricao": "Mecânico ABNT2", "preco": 199.90, "estoque": 10 }
```

## Parte 2 — Logger Singleton

### `AppLogger` (Singleton clássico) — `logging/AppLogger.java`

- Construtor `private`.
- `getInstance()` estático com inicialização **lazy** (só cria na primeira chamada).
- **Thread-safe** com `volatile` + dupla checagem (double-checked locking).
- Níveis `DEBUG`, `INFO` e `ERRO`.
- Escreve no console (`System.out` / `System.err` para ERRO) e no arquivo `app.log` (modo append).
- Método `log()` é `synchronized`, então linhas de threads diferentes não se misturam.
- Nenhuma anotação do Spring.

Formato da linha:

```
2026-10-27 22:30:01.123 [INFO ] [http-nio-8080-exec-1] ProdutoService - Produto criado com id=1
```

### Uso dos níveis

| Nível | Quando | Exemplo |
|---|---|---|
| DEBUG | detalhes da operação | `Atualizando produto id=1 -> nome=Teclado RGB, preco=249.90` |
| INFO | operação concluída | `PUT /produtos/1 -> 200 OK` |
| ERRO | recurso não encontrado ou exceção | `Produto id=99 não encontrado` |

**Dados sensíveis:** o recurso Produto não tem senha, token nem CPF. Mesmo assim o logger nunca recebe
o corpo inteiro da requisição, só campos escolhidos. Em exceções é registrado só o tipo e a mensagem,
sem stack trace. Para entidades com CPF existe `AppLogger.mascarar(cpf)` (`12345678900` → `*********00`).

### Cópia como `@Component` — `logging/SpringAppLogger.java`

Mesma lógica, mas com construtor público, sem `getInstance()` e anotada com `@Component`.
Ela foi injetada no `ProdutoService` pelo construtor (ver o primeiro commit do repositório):

```java
public ProdutoService(IJpaProdutoRepo produtoRepo, SpringAppLogger log) {
    this.produtoRepo = produtoRepo;
    this.log = log;
}
```

O teste `springGaranteUmUnicoBeanDoLogger` confirma que o Spring entrega sempre o mesmo objeto.

### Perguntas

**Quem garante a instância única em cada versão?**
No `AppLogger` clássico quem garante é a própria classe: construtor privado impede `new` de fora, e o
`getInstance()` com `volatile` + dupla checagem cria um único objeto por JVM, mesmo com várias threads.
No `SpringAppLogger` quem garante é o container do Spring: o escopo padrão de um bean é `singleton`,
então o `ApplicationContext` cria um objeto e injeta o mesmo em todo lugar (um por contexto, não por JVM;
nada impede um `new SpringAppLogger()` manual).

**Quando o Singleton clássico ainda faz sentido?**
Quando o código que precisa do objeto não é gerenciado pelo Spring (método `main`, classes utilitárias
estáticas, bibliotecas, código que roda antes do contexto subir ou fora dele) ou quando o projeto não usa
framework de injeção de dependência. Também faz sentido para recursos que precisam ser únicos na JVM
inteira, como um handle de arquivo compartilhado, e não apenas dentro de um contexto do Spring.

### Versão final escolhida: `AppLogger` (Singleton clássico)

A versão final usa o **`AppLogger` clássico** no Controller, no Service, no `GlobalExceptionHandler`
e no `main`. Justificativa:

1. O logger é usado também fora de beans do Spring (no `main`, depois do `SpringApplication.run`), e o
   clássico funciona em qualquer lugar, com ou sem container.
2. O `app.log` é um recurso único da JVM. Com o clássico existe garantidamente um único `PrintWriter`
   aberto no arquivo; com o bean, um `new` manual ou um segundo contexto (ex.: em testes) abriria outro.
3. É o objetivo da atividade: aplicar o padrão Singleton de forma explícita (privado, lazy, thread-safe).

O `SpringAppLogger` continua no código como comparação. Ele tem `@Lazy`, então o Spring só o cria se
alguém injetar; na versão final ninguém injeta, e ele não abre o `app.log`.
Trade-off assumido: o bean seria mais fácil de substituir por um mock em testes e tem ciclo de vida
gerenciado (`@PreDestroy` fecha o arquivo). Para um logger simples isso pesa menos que os pontos acima.

## Estrutura

```
src/main/java/com/trabalho/api/
├── ProdutoApiApplication.java
├── controllers/ProdutoController.java
├── services/ProdutoService.java
├── repositories/IJpaProdutoRepo.java
├── entities/Produto.java
├── exceptions/RecursoNaoEncontradoException.java
├── exceptions/GlobalExceptionHandler.java
└── logging/
    ├── AppLogger.java          # Singleton clássico (usado na versão final)
    └── SpringAppLogger.java    # cópia @Component (comparação)
```
