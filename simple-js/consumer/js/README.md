# Consumer TypeScript (Express.js)

Cette application est un serveur Express.js écrit en TypeScript qui permet d'exécuter la [logique métier](../../business-logic-provider/README.md) via une API REST.

## Rôle

-   **Endpoint `POST /execute`** : Reçoit les données, les transmet à la [bibliothèque cliente TypeScript](../../business-logic-client-lib/ts/js-ts-business-logic-client-lib/README.md) et retourne le résultat.

## Démarrage

1.  **Installer les dépendances :**
    ```bash
    npm install
    ```

2.  **Compiler et lancer le serveur :**
    ```bash
    npm start
    ```

Le serveur est accessible sur `http://localhost:3000`.

## Exemple d'utilisation

Pour plus d'informations, voir le [README du dossier parent](../README.md).

