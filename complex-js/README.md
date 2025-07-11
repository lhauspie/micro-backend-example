
# Business Logic Provider in Vanilla JS
[README](business-logic-provider/README.md)

# How to consume in Javascript

## Business Logic Client Librairies in Vanilla JS
```bash
cd ./business-logic-client-lib/js
npm install
```

## Consumer in Vanilla JS
```bash
cd ./consumer/js
npm install
npm start
```

Une requête à l'API du Consumer :
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"a": 11, "b": 20}' | jq
```
Devrait retourner : 
```
{
  "result": {
    "result": {},
    "errors": [
      {
        "code": "MISSING_FIELD",
        "path": ".fence",
        "message": "`.fence` is missing"
      },
      {
        "code": "MISSING_FIELD",
        "path": ".materials",
        "message": "`.materials` is missing"
      }
    ]
  }
}
```

Donc si on ajoute les champs manquants indiqués dans la réponse précédente :
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"fence": 1, "materials": 2}' | jq
```

On devrait avoir la réponse :
```
{
  "result": {
    "result": {
      "nb_wire_mesh_rolls": null,
      "nb_stacks": null,
      "nb_struts": null,
      "nb_tension_bars": null,
      "nb_tension_wires": null,
      "nb_tensioners": null,
      "nb_tension_wire_rolls": null,
      "nb_staples": null
    },
    "errors": [
      {
        "code": "MISSING_FIELD",
        "path": ".fence.fence_length",
        "message": "`.fence.fence_length` is missing"
      },
      {
        "code": "MISSING_FIELD",
        "path": ".materials.wire_mesh_roll_length",
        "message": "`.materials.wire_mesh_roll_length` is missing"
      },
      {
        "code": "MISSING_FIELD",
        "path": ".fence.nb_segments_greater_than_roll_length",
        "message": "`.fence.nb_segments_greater_than_roll_length` is missing"
      },
      {
        "code": "MISSING_FIELD",
        "path": ".fence.nb_segments",
        "message": "`.fence.nb_segments` is missing"
      },
      {
        "code": "MISSING_FIELD",
        "path": ".materials.wire_mesh_roll_height",
        "message": "`.materials.wire_mesh_roll_height` is missing"
      },
      {
        "code": "MISSING_FIELD",
        "path": ".materials.tension_wire_roll_length",
        "message": "`.materials.tension_wire_roll_length` is missing"
      }
    ]
  }
}
```

Il est maintenant possible de requêter l'API REST du Consumer avec le payload complet :
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

La réponse devrait être :
```
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

## End then

### ➕➕ Resiliency
Comme pour l'exemple `simple-js`, vous pouvez maintenant arrêter le Business Logic Provider:
```bash
podman stop complex-js-business-logic-provider
```
et continuer de requêter l'API du Consumer:
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"a": 11, "b": 20}'
```
Le Consumer peut continuer à fonctionner même si le Business Logic Provider est en panne.

### ➕➕ Auto Update
Vous pouvez maintenant mettre à jour le Business Logic Provider pour changer la logique métier interne et continuer à interroger l'API REST du Consumer.

### ➖➖ Drawbacks
- Il faut implémenter toutes les vérifications de format et de présence. Cela peut simplement venir du fait que je n'utilise aucune lib. Peut-être que j'aurais pu utiliser un outil de validation de Schema de données par exemple avant de commencer les calculs (ex: [Joi](https://joi.dev/api/)).
- Changer la signature de méthode de la logique métier va casser le Consumer, il faut donc anticiper au maximum ce qu'on pourrait avoir besoin
  - ex : Répondre toujours avec la même structure de réponse, même s'il y a des erreurs
    - un champ `result` dans la réponse pour y ranger le résultat
    - un champ `warnings` dans la réponse pour permettre au Consumer de savoir s'il y a un risque particulier (ex: Changement de version)
    - un champ `errors` dans la réponse pour permettre au Consumer de savoir s'il y a des erreurs

### ➖➖ Communiquer les schemas de données au Consumer

Si on veut être hautement flexible dans les inputs autorisés (l'objectif étant que le Consumer puisse envoyer un Payload avec plus d'information que nécessaire, sans casser le contrat d'interface), il faut utiliser un format d'échange hautement flexible. Les langages fortement typés, comme Java, ne sont donc pas les bons candidats.
JavaScript semble être un très bon candidat.
Et en JavaScript, il existe un format d'échange tout trouvé pour répondre à ce besoin : le JSON.

Mais le JSON est tellement flexible qu'il devient difficile de guider le Consumer pour qu'il envoie les bonnes données dans le bon format.
Il faut donc trouver un moyen de le guider et de lui permettre de coder facilement les objets qui seront échangés entre le Consumer et la Business Logic.

Ma première piste est donc de fournir un schéma de données JSON (JSON Schema) pour les inputs et les outputs de la Business Logic via des endpoints dédiés.
Ainsi, lorsque le Consumer voudra coder ses objets, il pourra télécharger le JSON schema et faire une génération automatique de code compatible avec ces JSON Schemas (cf. https://www.jsonschema2pojo.org/).
Ces JSON Schemas doivent donc être le plus complet possible et le plus stable possible.

On pourrait tenter d'intégrer, à la tuyauterie, une gestion des changements de ces schemas pour afficher des logs de Warning pour prévenir le Consumer que quelque chose a changé dans le contrat d'interface.

### ➖➖ Faire en sorte que nous soyons informés de dysfonctionnement

# How to consume in TypeScript

## Business Logic Client Library in TypeScript
```bash
cd ./business-logic-client-lib/ts
npm install
npm run build
```

## Consumer in TypeScript
```bash
cd ./consumer/ts
npm install
npm run build
npm start
```

## Call the Consumer API
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

La réponse devrait être :
```
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

# How to consume in Java

## Business Logic Client Library in Java
```bash
cd ./business-logic-client-lib/java
mvn clean install
```

## Consumer in Java
```bash
cd ./consumer/java
mvn clean install
java -jar target/consumer-1.0.0.jar
```


## Call the Consumer API
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

La réponse devrait être :
```
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



