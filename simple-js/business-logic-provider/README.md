# Business Logic Provider (Simple)

Ce service utilise Nginx pour exposer un unique fichier JavaScript contenant la logique métier.

La logique métier est volontairement très simple pour illustrer le concept. Elle additionne simplement deux nombres passés en paramètres.

## Démarrage

1.  **Construire l'image Docker :**
    ```bash
    podman build -t business-logic-provider-simple .
    ```

2.  **Lancer le conteneur :**
    ```bash
    podman run -d -p 8082:80 --name business-logic-provider-simple business-logic-provider-simple
    ```

Le script est accessible sur `http://localhost:8082/business-logic.js`.

