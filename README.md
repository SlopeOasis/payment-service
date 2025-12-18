# Payment Service - Dokumentacija

## Pregled
`payment-service` je mikroservis za izvedbo nakupov in verifikacijo Polygon transakcij.

Glavne odgovornosti:
- Ustvari **payment intent** za izdelek (USD → Wei) in pripravi podatke za MetaMask transakcijo.
- Preveri **txHash** na Polygon omrežju (receipt/status/value/from/to).
- Zapiše transakcijo v bazo in doda kupca na post (klic na `post-service`).

## Tehnologije
- **Spring Boot 3.3.x**
- **PostgreSQL 15** (docker, port 5433)
- **Web3j** (Polygon RPC)
- **Clerk JWT** (Bearer token + interceptor)
- **CoinGecko API** (POL/USD cena, header-based API key)

## Porti
- HTTP: `http://localhost:8082`
- DB: `localhost:5433` (docker compose)

## Zagon

### Predpogoji
1) Docker infra (Postgres) mora biti zagnan:
```bash
cd ..\..\docker
docker compose up -d
```

2) Nastavi environment variables (glej spodaj).

### Lokalni razvoj
```bash
cd ..\..\payment\payment-service
mvn clean package
mvn spring-boot:run
```

Če uporabljaš skripto:
```bat
run-dev.bat
```

## Environment variables

### Obvezno
- `CLERK_ISSUER` – Clerk issuer URL
- `CLERK_JWKS_URL` – Clerk JWKS endpoint
- `POLYGON_RPC_URL` – RPC URL (npr. `https://polygon-rpc.com`)

### Priporočeno
- `COINGECKO_API_KEY` – CoinGecko key (demo/pro)
- `COINGECKO_API_KEY_HEADER` – privzeto `x-cg-demo-api-key` (pro: `x-cg-pro-api-key`)
- `COINGECKO_BASE_URL` – privzeto `https://api.coingecko.com/api/v3`

### Cross-service base URL (lahko ostane default)
- `POST_SERVICE_BASE_URL` – privzeto `http://localhost:8081`
- `USER_SERVICE_BASE_URL` – privzeto `http://localhost:8080`

### Ostalo
- `SERVER_PORT` – privzeto `8082`
- `JWT_DEV_MODE` – `true/false` (privzeto `false`)

## Avtentikacija (Clerk JWT)

Vsi endpointi pod:
- `/paymentIntents/**`
- `/payments/**`
- `/transactions/**`

zahtevajo `Authorization: Bearer <clerk-jwt>`.

Interceptor nastavi `X-User-Id` request attribute (Clerk user id), ki se uporablja kot buyer id.

## REST API

### POST /paymentIntents/intent
Ustvari payment intent za post.

**Headers**
- `Authorization: Bearer <jwt>`

**Body**
```json
{ "postId": 123 }
```

**Response (200)**
```json
{
  "paymentId": "uuid",
  "chainId": 137,
  "currency": "POL",
  "amountWei": "123456789000000000",
  "sellerWalletAddress": "0x..."
}
```

Opomba: `amountWei` je serializiran kot string (JSON-safe), na frontendu ga obravnavaj kot `BigInt`.

### POST /payments/confirm
Potrdi, da je tx mined in ustreza intentu.

**Headers**
- `Authorization: Bearer <jwt>`

**Body**
```json
{ "paymentId": "uuid", "txHash": "0x..." }
```

**Responses**
- `200 OK` – plačilo potrjeno, buyer doda na post
- `202 Accepted` – tx še ni mined (`TX_PENDING`), frontend mora poll-at
- `400 Bad Request` – tx failed ali mismatch (`TX_FAILED`)

### GET /transactions/me
Vrne transakcije trenutnega uporabnika.

**Headers**
- `Authorization: Bearer <jwt>`

## Integracije

### post-service
Po uspešni potrditvi payment-service kliče `post-service` endpoint `POST /posts/{id}/buyers` in **forwarda isti `Authorization` header**, ki ga je prejel od frontenda.

### user-service
Za verifikacijo buyer/seller wallet naslovov payment-service kliče user-service public endpoint:
- `GET /users/public/pol-wallet-addres?clerkId=...`

## Troubleshooting
- `401 Unauthorized`: manjka ali je invaliden Clerk token.
- `202 TX_PENDING`: tx ni mined; ponovi confirm čez nekaj sekund.
- CoinGecko `429`: nastavi `COINGECKO_API_KEY` ali počakaj; service uporabi cache samo ob napaki.
