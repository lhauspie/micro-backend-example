# Module Provider en Vanilla JS
```bash
cd ./business-logic-provider
docker build . -t complex-js-business-logic-provider
docker run --rm -it -p 8080:80 --name complex-js-business-logic-provider complex-js-business-logic-provider
```

## Business Logic
```bash
curl http://localhost:8080/business-logic.js
```

## Input JSON Schema
```bash
curl http://localhost:8080/input-schema.json
```

## Output JSON Schema
```bash
curl http://localhost:8080/output-schema.json
```

# How to consume in Javascript

## Module Client Librairies in Vanilla JS
```bash
cd ./business-logic-client-lib/js
npm install
```

## Module Consumer in Vanilla JS
```bash
cd ./consumers/nodejs
npm install
npm start
```

Then you can query the API with:
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"a": 11, "b": 20}' | jq
```
Should return 
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

So if you add these missing fields:
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"fence": 1, "materials": 2}' | jq
```

, you should get:
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

So now you can query the Rest API of the consumer with the full payload:
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

You should get:
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

## Communiquer les schemas de données au Consumer

TO BE CONTINUED...


## End then

### ➕➕ Resiliency
Like in the simple-js example, you can now shut down the Module Provider:
```bash
docker stop complex-js-business-logic-provider
```
and continue to request the Module Consumer API
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"a": 11, "b": 20}'
```
It will still return same result even if the Business Logic Provider is down.

### ➕➕ Auto Update
Like in the simple-js example, you can now update the Business Logic Provider to change the inner business logic and continue to request the Consumer's REST API.


### ➖➖ Drawbacks
- Il faut implémenter toutes les vérifications de format et de présence. Cela peut simplement venir du fait que je n'utilise aucune lib. Peut-être que j'aurais pu utiliser un outil de validation de Schema de données par exemple avant de commencer les calculs (ex: [Joi](https://joi.dev/api/)).
- Changer la signature de méthode de la logique métier va casser le Consumer, il faut donc anticiper au maximum ce qu'on pourrait avoir besoin
  - ex : Répondre toujours avec la même structure de réponse, même s'il y a des erreurs
    - un champ `result` dans la réponse pour y ranger le résultat
    - un champ `warnings` dans la réponse pour permettre au Consumer de savoir s'il y a un risque particulier (ex: Changement de version)
    - un champ `errors` dans la réponse pour permettre au Consumer de savoir s'il y a des erreurs
