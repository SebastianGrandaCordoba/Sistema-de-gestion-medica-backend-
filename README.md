# Backend del sistema de gestión médica

API REST en Java 21 y Spring Boot que aplica el patrón Facade. El cliente usa `ClinicFacade` para agendar citas, consultar la historia consolidada, generar prescripciones y solicitar exámenes. Los cinco servicios de dominio conservan responsabilidades separadas.

## Requisitos

- Java 21
- Maven 3.6.3 o posterior

## Ejecutar localmente

```powershell
mvn spring-boot:run
```

La aplicación escucha en `http://localhost:8080`. H2 se crea en memoria al iniciar el proceso; Hibernate crea o actualiza las tablas automáticamente. La consola H2 está en `http://localhost:8080/h2-console` con JDBC URL `jdbc:h2:mem:clinicdb`, usuario `sa` y contraseña vacía. Al reiniciar, los datos guardados en H2 se borran. Se precargan cinco médicos de Cardiología, Dermatología y Pediatría.

## Base de datos

Las entidades JPA y los repositorios Spring Data JPA persisten pacientes, médicos, citas, historias clínicas, prescripciones y órdenes/resultados de laboratorio. Por defecto se usa H2 en memoria, adecuada para desarrollo y demostración.

Para ejecutar con PostgreSQL, activa el perfil `prod` y define `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USERNAME` y `DATABASE_PASSWORD`. Por ejemplo:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DATABASE_HOST = "localhost"
$env:DATABASE_PORT = "5432"
$env:DATABASE_NAME = "clinic"
$env:DATABASE_USERNAME = "clinic"
$env:DATABASE_PASSWORD = "change-me"
mvn spring-boot:run
```

El esquema se actualiza al inicio con `ddl-auto=update`. Para un entorno real, debe reemplazarse por migraciones versionadas antes de almacenar datos clínicos reales.

## Endpoints

Las rutas requeridas por el taller están disponibles bajo `/api/clinica`:

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/clinica/paciente` | Registra un paciente; el documento es único. |
| `POST` | `/api/clinica/cita` | Agenda en la primera franja libre de la especialidad y devuelve recordatorio. |
| `GET` | `/api/clinica/historia/{pacienteId}` | Consolida perfil, citas pasadas, antecedentes, prescripciones y laboratorios. |
| `POST` | `/api/clinica/prescripcion` | Valida medicamentos contra alergias registradas y crea la prescripción. |
| `POST` | `/api/clinica/laboratorio` | Solicita hemograma, glicemia y/o perfil lipídico con resultados simulados. |
| `GET` | `/api/clinica/medicos?especialidad=Cardiology` | Lista todos los médicos o filtra por especialidad. |

También se incluyen rutas complementarias para consultar perfil, registrar antecedentes y cancelar citas:

- `GET /api/clinica/paciente/{patientId}`
- `GET /api/clinica/paciente?documento=1085123456` — busca al paciente por documento; el portal lo usa para iniciar sesión.
- `POST /api/clinica/historia`
- `DELETE /api/clinica/cita/{appointmentId}`
- `GET /api/clinica/disponibilidad?medicoId=1&fecha=2026-10-20` — franjas libres de un médico en una fecha.

`POST /api/clinica/cita` acepta dos campos opcionales, `doctorId` y `time`, para que el paciente
elija médico y horario. Si se omiten, la cita se asigna en la primera franja libre de la
especialidad. `GET /api/clinica/historia/{pacienteId}` devuelve `upcomingAppointments` además de
`pastAppointments`, de modo que el portal arma el tablero con una sola llamada a la fachada.

## CORS

`CorsConfig` habilita `/api/**` para `http://localhost:*` y `https://*.vercel.app`. La propiedad
`clinic.cors.allowed-origin-patterns` permite sobrescribir la lista por entorno.

### Ejemplos de cuerpos JSON

Registrar paciente:

```json
{
  "documentNumber": "CC-10001",
  "fullName": "Alex Morgan",
  "email": "alex@example.com",
  "phone": "+57 300 000 0000",
  "birthDate": "1990-04-12",
  "gender": "Unspecified"
}
```

Agendar cita (fecha ISO `YYYY-MM-DD`):

```json
{
  "patientId": 1,
  "specialty": "Cardiology",
  "date": "2027-01-15"
}
```

Registrar consulta, diagnóstico y alergias (varias alergias separadas por coma):

```json
{
  "patientId": 1,
  "consultation": "Initial consultation",
  "diagnosis": "Pending evaluation",
  "allergies": "penicillin, latex"
}
```

Generar prescripción. Cada elemento de `medications` sigue el formato `name|dose|duration`; la dosis y duración las debe ingresar el profesional tratante:

```json
{
  "patientId": 1,
  "medications": ["Example medication|dose specified by clinician|duration specified by clinician"]
}
```

Solicitar exámenes. Se aceptan `hemograma`, `glicemia` y `perfil lipídico` (sin distinguir mayúsculas ni tildes):

```json
{
  "patientId": 1,
  "exams": ["hemograma", "glicemia", "perfil lipídico"]
}
```

## Patrón Facade

Todos los métodos de `ClinicFacade` llaman dos o más subsistemas. `viewCompleteHistory` agrega resultados de cuatro servicios; `generatePrescription` comprueba alergias antes de crear la receta. El controlador delega los flujos compuestos a la fachada y las lecturas simples a sus servicios correspondientes.

## Despliegue sugerido: Render

El repositorio incluye `Dockerfile` y `render.yaml`. En Render crea un Blueprint usando el repositorio; el blueprint crea el servicio web y una base PostgreSQL gratuita, enlaza el proceso al puerto de entorno `PORT` (Render usa `10000` por defecto) y publica `/actuator/health` como health check.

La base PostgreSQL gratuita creada por el Blueprint conserva registros entre reinicios, pero Render la elimina después de 30 días; es solo para demostración. Para conservar datos por más tiempo, cambia a un plan PostgreSQL de pago antes de la fecha de vencimiento. H2 en memoria es apropiada solo para ejecución local: el contenido desaparece al reiniciar o escalar a varias instancias. Un servicio gratuito puede suspenderse cuando no recibe tráfico; el primer request tras la suspensión puede tardar en responder.

Render es la opción sugerida para esta primera publicación porque conecta directamente el repositorio GitHub, construye el Dockerfile y ofrece un Web Service con URL pública y health check. Railway también es compatible, aunque conviene elegir un proveedor de PostgreSQL persistente en cualquiera de las dos plataformas antes de usar datos que deban sobrevivir reinicios.
