# Business Logic Provider en Vanilla JS
```bash
cd ./business-logic-provider
docker build . -t simple-js-business-logic-provider
docker run --rm -it -p 8080:80 --name simple-js-business-logic-provider simple-js-business-logic-provider
```

# How to consume in Javascript

## Business Logic Client Libraries en Vanilla JS
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

Then you can query the API with:
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"a": 11, "b": 20}'
```
Should return `{result: 31}`

## End then

### ➕➕ Resiliency
You can now shut down the Business Logic Provider:
```bash
docker stop simple-js-business-logic-provider
```
and continue to request the Consumer API
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"a": 11, "b": 20}'
```
It will still return `{result: 31}`

### ➕➕ Auto Update
You can now update the Business Logic Provider to change the inner business logic and continue to request the Consumer API.

Let say you choose to multiply instead of sum.
Make your change and then run again the Business Logic Provider:
```bash
cd ./business-logic-provider
docker build . -t simple-js-business-logic-provider
docker run --rm -it -p 8080:80 --name simple-js-business-logic-provider simple-js-business-logic-provider
```

If the cacheDuration is overdue, next cUrl commands:
```bash
curl --location 'http://localhost:3000/execute' \
-H 'Content-Type: application/json' \
--data '{"a": 11, "b": 20}'
```
Will return `{result: 220}`


### ➖➖ Drawbacks
- Changer la signature de méthode de la logique métier va casser le consommateur
- La logic métier mise en cache côté Consumer via la Business Logic Client Library restera la même pendant la durée du cache
  - Pour palier ce problème, nous fournissons au Consumer le moyen de changer ce comportement en mettant la cacheDuration à 0 par exemple, mais cela viendra avec un coût en performance.
- L'utilisation de JavaScript permet d'avoir un contrat d'interface plus souple, mais cela vient avec un coût en termes de sécurité et de performance. Que se passera-t-il si l'objet passé à la méthode `execute` ne respecte pas la structure attendue par la logique métier ?
- Comment communiquer les types de données (aussi bien les input que les output) aux Consumers ?