# Business Logic Provider

Ce service expose la logique métier en tant que ressource statique via un serveur Nginx.

## Rôle

-   **Héberger `business-logic.js`** : Le script contenant toute la logique métier.
-   **Exposer les schémas** : Fournit `input-schema.json` et `output-schema.json` pour la validation des données.

L'utilisation de Nginx garantit une distribution performante et fiable des fichiers statiques.

## Démarrage

Ce service est conteneurisé avec Docker.

1.  **Construire l'image :**
    ```bash
    podman build -t business-logic-provider .
    ```

2.  **Lancer le conteneur :**
    ```bash
    podman run -d -p 8080:80 --name business-logic-provider business-logic-provider
    ```

Le serveur est maintenant accessible sur `http://localhost:8080`.

## Endpoints
  
- http://localhost:8080/business-logic.js
- http://localhost:8080/input-schema.json
- http://localhost:8080/output-schema.json

