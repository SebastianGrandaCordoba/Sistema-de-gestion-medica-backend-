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

Para ejecutar con PostgreSQL, activa el perfil `prod` y define `DATABASE_URL` como una URL JDBC (`jdbc:postgresql://host:5432/database`), además de `DATABASE_USERNAME` y `DATABASE_PASSWORD`. Por ejemplo:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/clinic"
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
- `POST /api/clinica/historia`
- `DELETE /api/clinica/cita/{appointmentId}`

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

El repositorio incluye `Dockerfile` y `render.yaml`. En Render crea un servicio Web usando el repositorio; el blueprint usa Docker, enlaza el proceso al puerto de entorno `PORT` (Render usa `10000` por defecto) y publica `/actuator/health` como health check.

Para conservar registros entre reinicios, crea una base de datos PostgreSQL administrada y define `DATABASE_URL` con su URL JDBC, junto con `DATABASE_USERNAME` y `DATABASE_PASSWORD`. H2 en memoria es apropiada solo para demostración: el contenido desaparece al reiniciar o escalar a varias instancias. Un servicio gratuito puede suspenderse cuando no recibe tráfico; el primer request tras la suspensión puede tardar en responder.

Render es la opción sugerida para esta primera publicación porque conecta directamente el repositorio GitHub, construye el Dockerfile y ofrece un Web Service con URL pública y health check. Railway también es compatible, aunque conviene elegir un proveedor de PostgreSQL persistente en cualquiera de las dos plataformas antes de usar datos que deban sobrevivir reinicios.
