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

## Exemples d'Utilisation

Exemple de requête pour exécuter la logique métier via le endpoint `/execute` :

```bash
curl -X POST http://localhost:3000/execute \
  -H "Content-Type: application/json" \
  -d '{"a": 2, "b": 3}'
```

Réponse attendue :

```json
{
  "result": 5
}
```
