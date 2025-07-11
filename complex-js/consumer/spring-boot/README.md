# Consumer Spring Boot

Cette application est un serveur Spring Boot qui permet d'exécuter la logique métier via une API REST.

## Rôle

-   **Endpoint `POST /execute`** : Reçoit les données, les transmet à la [bibliothèque cliente Java](../../business-logic-client-lib/java/js-java-business-logic-client-lib/README.md) et retourne le résultat.

## Démarrage

1.  **Compiler le projet :**
    ```bash
    mvn clean install
    ```

2.  **Lancer l'application :**
    ```bash
    java -jar target/spring.consumer.js-0.0.1-SNAPSHOT.jar
    ```

Le serveur est accessible sur `http://localhost:3000`.

## Exemple d'utilisation

Pour plus d'informations, voir le [README du dossier parent](../README.md).


