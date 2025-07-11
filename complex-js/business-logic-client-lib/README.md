# Bibliothèques Clientes

Ce répertoire contient les bibliothèques clientes responsables de l'interaction avec le [Business Logic Provider](../business-logic-provider/README.md).

## Rôle

Chaque bibliothèque a pour mission de :

1.  **Récupérer** la logique métier depuis le `business-logic-provider`.
2.  **Mettre en cache** le script pour éviter les appels réseau inutiles.
3.  **Exécuter** la logique métier avec les données d'entrée fournies.
4.  **Valider** les entrées et les sorties par rapport aux schémas JSON.

## Implémentations

Des bibliothèques sont disponibles pour différents langages et écosystèmes :

-   **[Java](./java/js-java-business-logic-client-lib/README.md)** : Pour les applications basées sur la JVM.
-   **[JavaScript](./js/js-js-business-logic-client-lib/README.md)** : Pour les environnements Node.js.
-   **[TypeScript](./ts/js-ts-business-logic-client-lib/README.md)** : Pour les projets TypeScript, offrant un typage fort.

