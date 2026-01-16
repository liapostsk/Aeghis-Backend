# Aegis – Backend

Backend de la aplicación **Aegis**, encargado de la lógica de negocio, la gestión de usuarios, grupos, invitaciones, ubicaciones seguras, contactos, trayectos, participaciones y solicitudes de acompañamiento así como de la exposición de una API REST que da soporte al frontend móvil.

Este proyecto forma parte de un **Trabajo de Fin de Grado (TFG)** y ha sido diseñado con una arquitectura modular, escalable y orientada a servicios.

---

## Descripción

El backend de Aegis proporciona los servicios necesarios para garantizar la **seguridad, persistencia y coherencia de los datos** utilizados por la aplicación móvil.

Ha sido desarrollado utilizando **Spring Boot**, siguiendo una arquitectura basada en controladores, servicios y repositorios, lo que facilita el mantenimiento, la extensibilidad y la integración con otros sistemas. El backend se comunica con el frontend mediante una **API REST** documentada y utiliza servicios externos cuando es necesario.

---

## Tecnologías utilizadas

- Java
- Spring Boot
- Spring Web (REST)
- Spring Data JPA
- Base de datos relacional
- Maven
- Swagger / OpenAPI

---

## Requisitos previos

- Java JDK 17 (recomendado)
- Maven

---

## Documentación de la API

La API REST está documentada mediante **Swagger / OpenAPI**.

- Entorno de producción:  
  https://aeghis-5846100a4265.herokuapp.com/swagger-ui/index.html#/

- Entorno local (modo desarrollo):  
  http://localhost:8080/swagger-ui/index.html

---

## Instalación y ejecución

### Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/Aegis-Backend.git
cd Aegis-Backend
