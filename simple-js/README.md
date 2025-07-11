# Architecture Micro-Backend : Exemple Simple

Cet exemple est une version simplifiée (POC) de l'architecture micro-backend. Il se concentre sur l'essentiel : externaliser une logique métier JavaScript et la consommer depuis une application Node.js.

## Composants

1.  **[Business Logic Provider](./business-logic-provider/README.md)** : Un serveur Nginx qui expose le fichier `business-logic.js`.

2.  **[Bibliothèque Cliente](./business-logic-client-lib/README.md)** : Une librairie cliente en JavaScript simple pour récupérer et exécuter la logique.

3.  **[Consumer](./consumer/README.md)** : Une application Node.js qui utilise la bibliothèque cliente.

