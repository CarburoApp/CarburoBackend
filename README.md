# Carburo - Backend Java a través de Spring Boot (TFG)

## 📊 Calidad del Código (SonarCloud)

**Estado General**

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=alert_status&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=ncloc&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)

**Bugs y Vulnerabilidades**

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=reliability_rating&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=bugs&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=vulnerabilities&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=security_rating&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)

**Cobertura y Mantenibilidad**

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=coverage&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=sqale_rating&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_CarburoBackend&metric=sqale_index&token=d2b061a19fef68eec4e7767962d878aa977ad254)](https://sonarcloud.io/summary/new_code?id=CarburoApp_CarburoBackend)

---

## 📘 Introducción

Este repositorio forma parte del **Trabajo de Fin de Grado (TFG)** de **Manuel García Baldo** en la **Universidad de Oviedo**.  
Su objetivo es proporcionar un **backend robusto en Java usando Spring Boot** que gestione **usuarios, vehículos y repostajes**, con sus históricos y estadísticas, para la aplicación principal del proyecto (**Carburo App**).

La **comunicación con la aplicación móvil** se realiza a través de la **API REST que este backend genera**, mientras que los **datos de estaciones de servicio y precios de combustibles** se consultan directamente desde la **API REST de PostgreSQL/Supabase**, asegurando disponibilidad y datos actualizados.

Como ampliación opcional del TFG se plantea:

- Una **versión web mínima** que muestre estaciones y precios de manera abierta.  
- Una posible **versión web completa** que gestione usuarios, vehículos y repostajes, replicando la funcionalidad de la app móvil.

---

## ⚙️ Estructura general del proyecto

El proyecto incluye:

- **Spring Boot con Spring Data JPA**: Persistencia de entidades (usuarios, vehículos, repostajes) en **Supabase/PostgreSQL**.  
- **API REST**: Endpoints para la gestión de usuarios, vehículos y repostajes.  
- **Servicios de negocio**: Clases `@Service` que encapsulan la lógica de negocio.  
- **Repositorios JPA**: Interfaces `JpaRepository` para acceder a la base de datos de forma eficiente.  
- **Manejo de excepciones y logging**: Uso de `@ControllerAdvice` y `Slf4j` para registrar errores y actividad.  

---

## 🔧 Configuración inicial

Antes de ejecutar el backend, es **imprescindible configurar correctamente las variables de entorno** relacionadas con la base de datos.

### 1️⃣ Variables necesarias para la base de datos

| Variable  | Descripción                                 |
|-----------|---------------------------------------------|
| `DB_URL`  | Dirección del host de la base de datos      |
| `DB_USER` | Usuario con permisos de lectura y escritura |
| `DB_PASS` | Contraseña correspondiente                  |

Estas variables se utilizan en **`application.properties`** para configurar Spring Boot.

> ⚠️ **Importante:**  
> Si estas variables no se definen correctamente, el backend **no podrá establecer conexión con la base de datos** y los endpoints dejarán de funcionar.

---

## 🔄 Funcionamiento general

### API REST

El backend expone endpoints REST para:

- Gestionar **usuarios**: alta, baja y modificación.  
- Registrar y consultar **vehículos** asociados a usuarios.  
- Registrar y consultar **repostajes**, con cálculos de consumo y gasto histórico.

La **aplicación móvil** interactúa únicamente con esta API para consultar o actualizar datos.  
Los **precios y datos de estaciones** se consultan directamente desde la **API REST de Supabase/PostgreSQL**.

### Registro de actividad

El backend registra logs detallados de cada operación:

- Altas y modificaciones de usuarios y vehículos.  
- Registro de repostajes y cálculos asociados.  
- Errores detectados y estado de persistencia.

---

## 📱 Relación con la aplicación Carburo

Este backend es el **motor de gestión interna** de la aplicación Android del TFG:

- Los usuarios pueden **gestionar sus vehículos y repostajes**.  
- Consultar **estadísticas y medias de consumo**.  
- La app obtiene los **precios y localización de estaciones** directamente desde la API de Supabase.


---

## 👨‍💻 Autor

**Manuel García Baldo**  
Universidad de Oviedo  
Trabajo de Fin de Grado - Ingeniería Informática (2025)

---

## 🧾 Licencia

Este proyecto se distribuye bajo licencia **MIT**.  
Puede utilizarse, modificarse o redistribuirse libremente, siempre que se cite la fuente original.
