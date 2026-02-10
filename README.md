# Sus Connect - Traffic Intelligence Service

## 🧠 Sobre o Projeto

O **Traffic Intelligence Service** é o motor analítico de decisão do ecossistema Sus Connect. Implementado seguindo **DDD (Domain-Driven Design)** e **Clean Architecture**, o serviço é responsável por orquestrar dados geográficos e operacionais de múltiplas fontes para sugerir a melhor unidade de saúde para um paciente, baseado em um algoritmo de direcionamento por pesos (ADP).

### Funcionalidades Principais

- **Algoritmo de Direcionamento por Pesos (ADP)**: Calcula o melhor direcionamento considerando distância, TMA, ocupação e especialidades
- **Integração com Network Service**: Busca unidades de saúde próximas com dados geográficos
- **Integração com LiveOps Service**: Obtém indicadores operacionais em tempo real (TMA, ocupação)
- **Cache Inteligente**: Redis com TTL curto para otimizar performance
- **Observabilidade Completa**: Métricas, logs estruturados e health checks

## 🛠️ Tecnologias

- **Java 21**
- **Spring Boot 3.4.x**
- **Spring Cloud OpenFeign** (Clientes HTTP)
- **Redis** (Cache)
- **Resilience4j** (Circuit Breaker e Retry)
- **Micrometer + Prometheus** (Métricas)
- **Spring Cloud Sleuth** (Rastreabilidade)
- **JUnit 5 + Mockito** (Testes)
- **Jacoco** (Cobertura de código - mínimo 80%)

## 📂 Estrutura do Projeto (DDD + Clean Architecture)

```
src/main/java/com/fiap/sus/traffic/
├── domain/                    # Camada de Domínio
│   ├── model/                # Entidades e Value Objects
│   ├── service/              # Serviços de domínio (lógica de negócio)
│   └── repository/           # Interfaces de repositório
├── application/              # Camada de Aplicação
│   ├── usecase/              # Casos de uso
│   ├── port/                 # Portas (interfaces)
│   └── dto/                  # DTOs de aplicação
├── infrastructure/           # Camada de Infraestrutura
│   ├── client/               # Clientes HTTP (Feign)
│   ├── cache/                # Adaptadores de cache
│   ├── config/               # Configurações
│   ├── health/               # Health indicators
│   └── repository/           # Implementações de repositório
├── presentation/             # Camada de Apresentação
│   ├── controller/           # Controllers REST
│   ├── dto/                  # DTOs de apresentação
│   └── mapper/               # Mappers
└── shared/                   # Utilitários compartilhados
    └── util/                 # Utilitários (ex: cálculo de distância)
```

## 🚀 Como Executar

### Pré-requisitos

- Docker e Docker Compose
- Maven 3.9+
- JDK 21
- Network Service rodando (porta 8080)
- LiveOps Service rodando (porta 8081)

### 1. Subir Infraestrutura (Redis)

```bash
docker compose up -d
```

Isso iniciará o Redis na porta `6379`.

### 2. Configurar Serviços Externos

Edite o `application.yml` se os serviços externos estiverem em URLs diferentes:

```yaml
traffic:
  intelligence:
    network-service:
      url: http://localhost:8080
    liveops-service:
      url: http://localhost:8081
```

### 3. Executar o Backend

```bash
mvn clean spring-boot:run
```

O serviço estará disponível em `http://localhost:8082`

## 📍 Endpoints Principais

### GET /direcionamento/consultar

Endpoint público (sem autenticação) para consultar direcionamento de pacientes.

**Query Parameters:**
- `baseAddress` (String, obrigatório): Endereço de referência (ex: "Av. Paulista, 1000, São Paulo, SP")
- `riskClassification` (String, obrigatório): RED, ORANGE, YELLOW, GREEN ou BLUE (Protocolo Manchester)
- `especialidade` (String, opcional): Especialidade médica desejada
- `radius` (Double, opcional): Raio de busca (padrão: 50.0, min: 1.0, max: 100.0)
- `distanceUnit` (String, opcional): Unidade de distância - KM, METERS ou MILES (padrão: KM)

**Exemplo de Requisição:**
```bash
curl "http://localhost:8082/direcionamento/consultar?baseAddress=Av.%20Paulista,%201000,%20São%20Paulo,%20SP&riskClassification=YELLOW&especialidade=Cardiologia&radius=30.0&distanceUnit=KM"
```

**Exemplo de Resposta:**
```json
{
  "sugestoes": [
    {
      "unidadeId": "550e8400-e29b-41d4-a716-446655440000",
      "nome": "Hospital Central",
      "scoreFinal": 0.85,
      "distanciaKm": 5.2,
      "tempoEstimadoMinutos": 55,
      "razao": "Próxima (5.2 km). TMA rápido (45 min). Baixa ocupação. Possui especialidade necessária."
    }
  ],
  "totalUnidadesAnalisadas": 3,
  "tempoProcessamentoMs": 245
}
```

### PUT /config/pesos

Endpoint para atualizar os pesos do algoritmo de direcionamento.

**Body:**
```json
{
  "pesoDistancia": 0.3,
  "pesoTMA": 0.4,
  "pesoOcupacao": 0.2,
  "pesoEspecialidade": 0.1
}
```

**Validações:**
- Cada peso deve estar entre 0.0 e 1.0
- A soma dos pesos não pode ser maior que 1.0

## 🧮 Algoritmo de Direcionamento por Pesos (ADP)

O algoritmo calcula um score final para cada unidade candidata usando a fórmula:

```
scoreFinal = (pesoDistancia × scoreDistancia) +
             (pesoTMA × scoreTMA) +
             (pesoOcupacao × scoreOcupacao) +
             (pesoEspecialidade × scoreEspecialidade)
```

### Critérios de Avaliação

1. **Distância**: Normalizado inversamente (menor distância = maior score)
2. **TMA (Tempo Médio de Atendimento)**: Normalizado inversamente por classificação de risco
3. **Ocupação**: Normalizado inversamente (menor ocupação = maior score)
4. **Especialidade**: Binário (1.0 se possui, 0.0 caso contrário)

### Configuração de Pesos

Os pesos podem ser configurados via `application.yml` ou via endpoint `/config/pesos`. Valores padrão:

- Distância: 0.3
- TMA: 0.4
- Ocupação: 0.2
- Especialidade: 0.1

## 📊 Observabilidade

### Métricas (Prometheus)

- `traffic.intelligence.consultas.total`: Total de consultas realizadas
- `traffic.intelligence.consultas.duracao`: Duração das consultas
- `traffic.intelligence.unidades.analisadas`: Número de unidades analisadas
- `traffic.intelligence.cache.hits`: Cache hits
- `traffic.intelligence.cache.misses`: Cache misses

Acesse: `http://localhost:8082/actuator/prometheus`

### Health Checks

- **Redis**: `http://localhost:8082/actuator/health/redis`
- **Serviços Externos**: `http://localhost:8082/actuator/health/externalServices`

### Logs

Logs estruturados com `traceId` e `spanId` para rastreabilidade distribuída. Níveis:
- **INFO**: Fluxo principal de execução
- **DEBUG**: Detalhes de processamento
- **ERROR**: Erros e exceções

## 🧪 Testes

### Executar Testes

```bash
mvn test
```

### Cobertura de Código

```bash
mvn clean test jacoco:report
```

O relatório estará em `target/site/jacoco/index.html`

**Meta**: Mínimo de 80% de cobertura (validado pelo Jacoco no build)

## 🔧 Configurações

### Cache (Redis)

TTLs configuráveis em `application.yml`:

- **Indicadores**: 30 segundos
- **Unidades**: 60 segundos
- **Pesos**: 300 segundos (5 minutos)

### Circuit Breaker (Resilience4j)

- **Sliding Window Size**: 10 requisições
- **Failure Rate Threshold**: 50%
- **Wait Duration**: 10 segundos
- **Retry**: 3 tentativas com backoff exponencial

## 🏗️ Arquitetura

O serviço segue os princípios de **Clean Architecture** e **DDD**:

1. **Domain Layer**: Contém a lógica de negócio pura, independente de frameworks
2. **Application Layer**: Orquestra casos de uso e define contratos (portas)
3. **Infrastructure Layer**: Implementa adaptadores para serviços externos
4. **Presentation Layer**: Expõe a API REST

### Fluxo de Execução

1. Cliente faz requisição ao endpoint `/direcionamento/consultar`
2. Controller valida parâmetros e chama o caso de uso
3. Caso de uso busca unidades do Network Service (com cache)
4. Para cada unidade, busca indicadores do LiveOps Service (com cache)
5. Calcula distâncias usando Fórmula de Haversine
6. Aplica algoritmo ADP para calcular scores
7. Ordena e retorna top N sugestões

## 📝 Exemplos de Uso

### Cenário 1: Paciente com Risco Moderado

```bash
curl "http://localhost:8082/direcionamento/consultar?baseAddress=Av.%20Paulista,%201000,%20São%20Paulo,%20SP&riskClassification=YELLOW&radius=20.0"
```

### Cenário 2: Paciente com Especialidade Específica

```bash
curl "http://localhost:8082/direcionamento/consultar?baseAddress=Av.%20Paulista,%201000,%20São%20Paulo,%20SP&riskClassification=RED&especialidade=Cardiologia"
```

### Cenário 3: Ajustar Pesos para Priorizar Distância

```bash
curl -X PUT "http://localhost:8082/config/pesos" \
  -H "Content-Type: application/json" \
  -d '{
    "pesoDistancia": 0.6,
    "pesoTMA": 0.2,
    "pesoOcupacao": 0.1,
    "pesoEspecialidade": 0.1
  }'
```

## 🐛 Troubleshooting

### Redis não conecta

Verifique se o Redis está rodando:
```bash
docker ps | grep redis
```

### Serviços externos indisponíveis

O serviço implementa circuit breaker e fallbacks. Verifique os logs para detalhes.

### Cache não está funcionando

Verifique as configurações de TTL em `application.yml` e os logs de cache hits/misses.

## 📄 Licença

Desenvolvido por **Fiap-Sptechers** como parte do projeto integrador de Saúde Pública.
