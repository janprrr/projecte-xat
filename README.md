# 💬 Projecte Xat — Aplicació de Missatgeria en Temps Real

## 📌 Descripció

Projecte Xat és una aplicació de missatgeria instantània desenvolupada en Java que permet la comunicació en temps real entre múltiples usuaris mitjançant una arquitectura client-servidor basada en sockets TCP.

El sistema inclou una interfície gràfica moderna amb JavaFX i una capa de persistència de dades basada en MongoDB, dissenyada amb una arquitectura desacoblada mitjançant interfícies.

---

## 🎯 Funcionalitats principals

* 🔐 Autenticació d'usuaris (login)
* 💬 Enviament i recepció de missatges en temps real
* 👥 Llista d'usuaris connectats
* 📡 Comunicació concurrent (multi-client)
* 🕒 Missatges amb timestamp
* 📜 Consulta d'historial per data
* 🧠 Protocol de comunicació propi
* 🗄️ Persistència amb MongoDB (en desenvolupament)

---

## 🏗️ Arquitectura

El sistema segueix una arquitectura client-servidor:

```
Client (JavaFX)  <--TCP-->  Servidor (Java Sockets)  <--->  MongoDB
```

### Components:

* **Client**

  * Interfície gràfica (JavaFX)
  * Gestió d'esdeveniments
  * Comunicació amb el servidor

* **Servidor**

  * Gestió de connexions
  * Autenticació
  * Broadcast de missatges
  * Gestió d'usuaris

* **Base de dades**

  * MongoDB
  * Persistència de missatges i usuaris
  * Accés mitjançant interfícies (DAO)

---

## 🧩 Tecnologies utilitzades

* Java 17
* JavaFX
* Sockets TCP (java.net)
* MongoDB
* Threads (concurrència)
* Maven

---

## 🔄 Protocol de comunicació

Els missatges segueixen un format de text pla:

```
MSG:[timestamp] usuari: text
SISTEMA:text
USUARIS:u1,u2,u3
HISTORY:INICI
HISTORY:[timestamp] usuari: text
HISTORY:FI
```

---

## ▶️ Execució del projecte

### 1. Clonar el repositori

```bash
git clone https://github.com/EL_TEU_USUARI/projecte-xat.git
cd projecte-xat
```

### 2. Executar servidor

```bash
java -jar server.jar
```

### 3. Executar client

```bash
java -jar AppLauncher.jar
```

---

## 📂 Estructura del projecte

```
src/
 ├── server/
 ├── client/
 │   └── ui/
 │       ├── controllers/
 │       └── views/
 └── database/
```

---

## 🗄️ Persistència de dades

El sistema utilitza MongoDB per:

* Emmagatzemar usuaris
* Emmagatzemar missatges
* Consultar historial per data

S'ha implementat una capa d'accés a dades basada en interfícies per permetre canviar el motor de base de dades en el futur.

---

## 🧪 Estat del projecte

| Funcionalitat  | Estat        |
| -------------- | ------------ |
| Xat temps real | ✅            |
| Autenticació   | ✅            |
| UI JavaFX      | ✅            |
| Historial      | ✅            |
| MongoDB        | 🟡 En procés |
| Seguretat      | ❌ Pendent    |

---

## 🚀 Millores futures

* Xifrat de comunicació (TLS)
* Xats privats
* Enviament d'arxius
* Emojis
* Versió web (WebSockets)

---

## 👨‍💻 Autor

Projecte desenvolupat per Jan Pros

CFGS DAM — 2025/2026

---

## 📜 Llicència

Aquest projecte és d'ús acadèmic.
