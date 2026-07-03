# Alquiler de Vehiculos (Jesus Huaripaucar) - Backend (Hackathon)

2 microservicios en Spring WebFlux + MongoDB (JDK 26). Comparten la misma base de datos: `jalaton`.

## Inicio rapido (3 comandos)

Requisitos: Docker Desktop abierto, JDK 26 y Maven instalados.

```
docker compose up -d
```
Esto descarga (si hace falta) y levanta Mongo local en `localhost:27017`. No hay que "crear" ninguna imagen a mano, Docker la descarga solo la primera vez.

```
cd cliente-service; mvn spring-boot:run
```
```
cd alquiler-service; mvn spring-boot:run
```
Esto corre el backend directo con Maven (no usa Docker, solo necesita JDK/Maven instalados).

Con eso ya tienes todo funcionando: Mongo + cliente-service (`:8081`) + alquiler-service (`:8082`). Detalles y alternativas (Atlas en la nube) mas abajo.

## Base de datos (ya creada)

```js
use jalaton;

db.createCollection("cliente");
db.createCollection("alquiler");
```

### Coleccion `cliente` (maestro)
```json
{
  "_id": ObjectId(),
  "dni": "12345678",
  "nombres": "Jesus",
  "apellidos": "Huaripaucar",
  "celular": "999888777",
  "correo": "jesus@mail.com",
  "licencia": "Q12345678",
  "estado": "ACTIVO"
}
```

### Coleccion `alquiler` (transaccional)
```json
{
  "_id": ObjectId(),
  "clienteId": "64f1a2b3c4d5e6f7a8b9c0d1",
  "vehiculoId": "64f1a2b3c4d5e6f7a8b9c0d2",
  "dias": 5,
  "fechaInicio": "2026-07-05",
  "fechaFin": "2026-07-10",
  "total": 250.00,
  "estado": "PENDIENTE"
}
```
Estados posibles: `PENDIENTE`, `EN_CURSO`, `FINALIZADO`, `CANCELADO`.

## Como ejecutar (con MongoDB Atlas compartido)

El equipo usa una sola base de datos en la nube (MongoDB Atlas), asi todos ven los mismos datos sin instalar nada local.

Requisitos: JDK 26 y Maven. No se necesita Docker ni Mongo instalado.

1. Pide al dueño del cluster Atlas el connection string (algo como `mongodb+srv://usuario:password@cluster.mongodb.net/jalaton`).
2. Antes de correr cada servicio, define la variable de entorno `MONGO_URI` en la terminal (Spring Boot no lee el archivo `.env` solo, hay que exportarla):

   PowerShell:
   ```
   $env:MONGO_URI = "mongodb+srv://usuario:password@cluster.mongodb.net/jalaton"
   ```
   Bash:
   ```
   export MONGO_URI="mongodb+srv://usuario:password@cluster.mongodb.net/jalaton"
   ```
3. Levantar primero **cliente-service** (misma terminal donde exportaste la variable):
   ```
   cd cliente-service
   mvn spring-boot:run
   ```
4. Luego **alquiler-service** (exporta `MONGO_URI` de nuevo si es otra terminal):
   ```
   cd alquiler-service
   mvn spring-boot:run
   ```

Si no defines `MONGO_URI`, cada servicio cae por defecto a `mongodb://localhost:27017/jalaton` (util para pruebas locales rapidas).

El archivo `.env` de cada servicio es solo de referencia/plantilla (no se lee automaticamente) — copia su valor y expórtalo como se explico arriba. **No subas el `.env` con la contraseña real a un repositorio compartido.**

## Alternativa: MongoDB local con Docker

Si no quieres usar Atlas (por ejemplo, para probar offline sin depender de internet), puedes levantar Mongo local con Docker en vez de instalarlo.

Requisito: tener [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y abierto.

1. Abrir Docker Desktop y esperar que el motor este corriendo (icono de ballena estable en la barra de tareas).
2. Desde la carpeta raiz del proyecto (donde esta este `README.md`), levantar Mongo:
   ```
   docker compose up -d
   ```
   Esto crea un contenedor `mongo-jalaton` escuchando en `localhost:27017`.
3. No hace falta definir `MONGO_URI`: por defecto los servicios ya apuntan a `mongodb://localhost:27017/jalaton`.
4. Levantar los servicios normalmente:
   ```
   cd cliente-service
   mvn spring-boot:run
   ```
   ```
   cd alquiler-service
   mvn spring-boot:run
   ```
5. Para apagar Mongo cuando termines:
   ```
   docker compose down
   ```
   (agrega `-v` al final si tambien quieres borrar los datos guardados: `docker compose down -v`)

## Puertos y endpoints

### cliente-service -> http://localhost:8081

| Metodo | Endpoint              | Descripcion          |
|--------|------------------------|-----------------------|
| GET    | /api/clientes          | Listar clientes       |
| GET    | /api/clientes/{id}     | Obtener un cliente    |
| POST   | /api/clientes          | Crear cliente         |
| PUT    | /api/clientes/{id}     | Actualizar cliente    |
| DELETE | /api/clientes/{id}     | Eliminar cliente      |

Ejemplo body (POST/PUT):
```json
{
  "dni": "12345678",
  "nombres": "Jesus",
  "apellidos": "Huaripaucar",
  "celular": "999888777",
  "correo": "jesus@mail.com",
  "licencia": "Q12345678",
  "estado": "ACTIVO"
}
```

### alquiler-service -> http://localhost:8082

| Metodo | Endpoint               | Descripcion           |
|--------|-------------------------|------------------------|
| GET    | /api/alquileres         | Listar alquileres      |
| GET    | /api/alquileres/{id}    | Obtener un alquiler    |
| POST   | /api/alquileres         | Crear alquiler         |
| PUT    | /api/alquileres/{id}    | Actualizar alquiler    |
| DELETE | /api/alquileres/{id}    | Eliminar alquiler      |

Ejemplo body (POST/PUT), usa un `clienteId` que exista en cliente-service:
```json
{
  "clienteId": "64f1a2b3c4d5e6f7a8b9c0d1",
  "vehiculoId": "64f1a2b3c4d5e6f7a8b9c0d2",
  "dias": 5,
  "fechaInicio": "2026-07-05",
  "fechaFin": "2026-07-10",
  "total": 250.00,
  "estado": "PENDIENTE"
}
```

Al crear un alquiler, `alquiler-service` valida el `clienteId` llamando a `cliente-service` (http://localhost:8081). Si no existe, responde `400`.

## Estructura (igual en ambos servicios)

```
src/main/java/pe/huaripaucar/jesus/
  model/         -> entidad Mongo + enum de estado
  repository/    -> ReactiveMongoRepository
  service/       -> logica de negocio (una sola clase)
  controller/    -> endpoints REST
```
`alquiler-service` ademas tiene `client/` con el `WebClient` que consulta a `cliente-service`.
