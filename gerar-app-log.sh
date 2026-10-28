#!/usr/bin/env bash
# Gera um app.log de exemplo exercitando todas as rotas do CRUD.
# Uso: suba a API (./mvnw spring-boot:run) e, em outro terminal, rode ./gerar-app-log.sh
set -e
URL=http://localhost:8080/produtos
JSON='Content-Type: application/json'

echo "POST (esperado 201)";        curl -s -o /dev/null -w "%{http_code}\n" -X POST "$URL" -H "$JSON" -d '{"nome":"Teclado","descricao":"Mecânico ABNT2","preco":199.90,"estoque":10}'
echo "POST (esperado 201)";        curl -s -o /dev/null -w "%{http_code}\n" -X POST "$URL" -H "$JSON" -d '{"nome":"Mouse","descricao":"Sem fio","preco":89.90,"estoque":25}'
echo "GET lista (esperado 200)";   curl -s -o /dev/null -w "%{http_code}\n" "$URL"
echo "GET 1 (esperado 200)";       curl -s -o /dev/null -w "%{http_code}\n" "$URL/1"
echo "GET 99 (esperado 404)";      curl -s -o /dev/null -w "%{http_code}\n" "$URL/99"
echo "PUT 1 (esperado 200)";       curl -s -o /dev/null -w "%{http_code}\n" -X PUT "$URL/1" -H "$JSON" -d '{"nome":"Teclado RGB","descricao":"Mecânico","preco":249.90,"estoque":8}'
echo "PUT 99 (esperado 404)";      curl -s -o /dev/null -w "%{http_code}\n" -X PUT "$URL/99" -H "$JSON" -d '{"nome":"X","preco":1}'
echo "DELETE 2 (esperado 204)";    curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$URL/2"
echo "DELETE 2 (esperado 404)";    curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$URL/2"
echo "GET abc (esperado 400)";     curl -s -o /dev/null -w "%{http_code}\n" "$URL/abc"
echo "Pronto. Veja o arquivo app.log na raiz do projeto."
