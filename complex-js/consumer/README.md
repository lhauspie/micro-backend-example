# Consumers

Ce répertoire contient les applications backend qui utilisent les [bibliothèques clientes](../business-logic-client-lib/README.md) pour exposer la logique métier via une API REST.

## Rôle

Chaque consumer implémente un endpoint (généralement `POST /execute`) qui :

1.  Accepte des données d'entrée au format JSON.
2.  Utilise la bibliothèque cliente correspondante pour exécuter la logique métier.
3.  Retourne le résultat de l'exécution.

## Exemples de Consumers

Plusieurs implémentations sont fournies pour démontrer l'intégration dans différents écosystèmes :

-   **[JavaScript (Express.js)](./js/README.md)**
-   **[Spring Boot](./spring-boot/README.md)**
-   **[TypeScript (Express.js)](./ts/README.md)**

## Exemples d'Utilisation

Exemple de requête pour exécuter la logique métier via le endpoint `/execute` :

```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{
  "fence": {
    "fence_length": 100,
    "nb_segments": 3,
    "nb_segments_greater_than_roll_length": 2
  },
  "materials": {
    "wire_mesh_roll_length": 25,
    "wire_mesh_roll_height": 1.5,
    "tension_wire_roll_length": 50
  }
}' | jq
```

Réponse attendue :

```json
{
  "result": {
    "result": {
      "nb_wire_mesh_rolls": 4,
      "nb_stacks": 41,
      "nb_struts": 10,
      "nb_tension_bars": 10,
      "nb_tension_wires": 3,
      "nb_tensioners": 15,
      "nb_tension_wire_rolls": 7,
      "nb_staples": 900
    }
  }
}
```
