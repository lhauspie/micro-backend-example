# How to

Ce repo fournit un DevContainer qui permet de contribuer très rapidement.
Donc, il suffit de lancer le DevContainer et on est prêt à contribuer.

Prenez donc le temps de lire les `README.md` de chaque module pour savoir comment l'utiliser.

# Why

## Micro Frontend

On connait le concept de micro-frontend : possibilité de découper en plusieurs composants une application web, chaque composant étant indépendant et pouvant être développé par une équipe différente.

Les avantages sont nombreux :
- Indépendance des équipes,
- cycle de vie indépendant,
- réutilisation de composants,
- Multi-langage,

Mais il y a aussi des inconvénients comme :
- La complexité de la mise en place,
- La communication entre les composants,
- La gestion des dépendances,
- La gestion des versions,
- La gestion des erreurs,
- La gestion des performances,
- etc.


## Micro Services

Dans le monde du backend, il y a le concept de micro-services qui permet de découper une application en plusieurs services indépendants. Chaque service est une application à part entière qui communique avec les autres services via des API (peu importe ce qu'on pourrait mettre derrière ce terme d'ailleurs).

Très souvent, on crée des API Rest et toute la communication se fait via le réseau, ce qui pose des problèmes de latence due à :
- la sérialisation / dé-sérialisation,
- les couches réseaux à traverser,
- les volumes des données échangées (verbosité),
- etc.

Pour palier ces problèmes, on peut par exemple utiliser des technologies moins coûteuses dans ce domaine comme gRPC. Mais cette technologie ne rencontre malheureusement pas son public pour plusieurs raisons comme sa complexité, la maturitié de l'eco-système, l'outillage, etc.

Et parfois, on s'oriente plutôt vers des applications avec des communications full asynchrone basées sur des évènements : les EDA (Event-Driven Architecture) car oui, qu'on le veuille ou non, le monde est évènementiel.

Autre point qui servira mon propos dans quelques lignes, dans 99% des cas, une application est, a minima, composée d'une base de données et/ou nécessite peut-être d'appeler d'autres APIs.
Mais dans 1% des cas, une application (ou seulement un pan) est complètement stateless et ne nécessite même pas de base de données.

Et c'est à ce cas que je m'intéresse aujourd'hui...

Imaginons que je doive mettre à disposition une calculette par exemple.
C'est quelque chose que j'ai déjà eu à faire pour un client : Fournir une calculette qui permet de savoir combien de mettre de grillage, combien de jambe de force et combien de cable guide, il faudrait commander pour fabriquer une clôture...
On fournit à la calculette le nombre de mettre linéaire, la hauteur de cloture et le nombre de côté et la calculette nous donne la liste de course.

On voit bien que dans ce cas, cette calculette ne nécessite aucune base de données.
Alors bon dieu pourquoi devrais-je fabriquer une API Rest et souffrir de tous ses désavantages ? 
Et, bien évidemment, on voit bien qu'on ne peut pas faire de l'évènementiel ici... ou en tout cas, ça apporterait au minimum une complexité non négligeable.

N'existe-t-il pas une solution adaptée pour fournir ce service de la manière la plus efficace possible ?


## Micro Backend

C'est là que j'en viens à penser à ce que je pourrais qualifier de Micro Backend.

Serait-il possible de découper un backend en plusieurs services indépendants qui communiquent entre eux via des appels de fonctions ?

Serait-il possible d'injecter dynamiquement ces services dans une application ?

Comment on pourrait effacer les problèmes de dépendances, de versions, de communication, de performances, etc. ?

C'est ce qu'on va tenter de voir avec ce repo.


# What

## Une évidence

Ma première piste évidente est de délivrer une librairie qui contient les services et de permettre à quiconque en aurait besoin de "tirer" sur cette librairie est de l'utiliser en appelant les SPI qu'elle expose.

Mais cela vient avec des inconvénients tout aussi évidents que la solution :
- La librairie devient une dépendance de l'application,
- La librairie doit être mise à jour par le consommateur,
- La portabilité de la librairie est limitée aux applications développées dans le même langage (ou du moins dans le même éco-système comme la JVM par exemple),
- La sécurité de l'application peut être compromise si la librairie n'est pas bien maintenue ou si elle contient du code malicieux (accès aux variables d'environnement, aux fichiers, aux sockets, etc.),
- etc.


## Mais alors...

J'en viens donc à lancer une reflexion sur un moyen de délivrer ces services (fully stateless a.k.a. fonctions pures) de manière plus indépendante, plus dynamique, plus sécurisée, plus performante, etc.

En discutant avec un collègue du Use Case final, il m'a exposé les axes de reflexion suivants :
- Ne serait-ce pas une problématique de format d'échange ton cas ? Tu pourrais faire ça avec un contrat XML et une XSD bien ficelée. 
  - Pas tout à fait, puisque je souhaite décentraliser certaines règles de validation de fond (Règles métier) et non de forme (format d'échange)
- Si c'est un client qui déroule de la logique métier d'un serveur, quid de l'obsolescence des données calculées par le client quand on changera la logique métier serveur délivrée ?
  - C'est exactement la même chose que si j'expose une API, si le client enregistre les données calculées par le serveur, il y a un risque d'obsolescence de ces données.
- Quid du versioning, comment gérer un breaking change (ex : ajout ou suppression d'un champ) ?
  - C'est une très bonne question. Ma première piste à ce moment-là, c'est de délivrer une nouvelle version majeure indépendante, ce qui permettra aux applications Clientes de continuer à fonctionner le temps de la migration... exactement comme on ferait avec une API. On pourrait même se permettre de modifier la version N-1 pour y ajouter une trace de log warn qui indique qu'une nouvelle version existe et qu'il faut mêtre à jour.
  - La seconde, c'est de permettre une flexibilité maximum sur les formats du contrat d'interface (ex : Utiliser du JSon)

On va tenter maintenant de fournir le service tout en conservant toutes ces problématiques en tête et on va voir où ça nous mène.

Bien entendu, il existe d'autres obstacles comme :
- La techno que je veux utiliser fait-elle partie du TechRadar de l'entreprise ?
- L'entreprise a-t-elle les compétences pour maintenir ces composants après mon départ ?
- Cette solution s'inscrit-elle correctement dans les coutumes et les contraintes de l'entreprise (ex : Utilisation d'un API Gateway car oui, on peut considérer cette solution comme une API) ?
- Comment s'assurer de ne pas casser le contrat d'interface par erreur (ex : Ajouter un champ sans trop faire attention) ? J'ai bien une idée pour celle-là.



# How

L'axe principal de ma reflexion est de séparer complètement la logique métier de l'execution de cette logique métier.

J'aimerais que le consommateur de ce nouveau genre de service n'est jamais de mise à jour à faire, en tout cas de lui-même et consciemment.
Lorsqu'un consommateur intègre mon service, il le fait une fois et n'a plus à y penser.
Ce serait la librairie intégrée par le consommateur qui tirerait la logique métier distante pour ensuite l'exécuter.
En d'autres termes, la librairie n'est finalement que de la tuyauterie qui permet de faire transiter la logique métier et l'exécuter.

Pour schématiser : Consumer --> Business Logic Client Library --> Business Logic Provider

Les exigences sont donc les suivantes :
- Le Service doit être indépendant de la Librairie en termes de cycle de vie, de version, et de technologie
- Le Service doit être le plus flexible possible (Changement de contrat d'interface, etc.)
- Le Service doit être cacheable en utilisant les standards HTTP
- La Librairie dit être facilement intégrable peu importe la techno (Maven, Gradle, NPM, pip, go get, etc.)
- Je dois pouvoir porter la librairie dans n'importe quelle technologie sans qu'il soit nécessaire de changer quoi que ce soit au Service ou au Service Provider
- Ce doit être plus performant qu'une API Rest
- Le Consumer pourra définir une durée de cache localement
- L'intégration et la configuration de la Librairie doit être le plus simple possible (grossièrement, une url et une durée de validité du cache)


Une première limite évidente : Il faut que les technos pour lesquelles je fournis la Libraire supporte l'exécution de la techno du Business Logic Provider.
En effet, si je fournis une librairie en Go, il faut que le Consumer soit capable d'exécuter du Go.
Il faudra donc choisir la technologie du Business Logic Provider en fonction des capacités de la Technologie du Business Logic Client Library et donc du Consumer.
Ou autrement dit, il faudra que la Business Logic soit exécutable dans la techno du Consumer.


## Service en JavaScript
[Un cas simple : une somme](./simple-js/README.md)

TO BE CONTINUE...

## Possible avec Python

TO BE CONTINUE...

## Soyons fou, et WebAssembly ?

### Qu'est-ce que WebAssembly ?

WebAssembly (WASM) est un format de fichier binaire qui permet d'exécuter du code dans un navigateur web.
Il est conçu pour être exécuté à des vitesses proches de celles du code natif et permet de développer des applications web plus complexes et plus performantes.

Mais en quoi WASM peut aider dans ce cas d'usage si c'est pour exécuter du code dans un navigateur web ?
En fait, il existe un moyen de l'exécuter en dehors d'un navigateur web, ce qui ouvre la voie à de nombreuses applications comme fabriquer des micro-backends.

WebAssembly apporte déjà une première chose évidente : la sécurité.
En effet, un executable WASM est sandboxé et ne peut pas accéder à des ressources du système hôte.
Il n'est donc pas possible qu'un service WASM puisse accéder à la base de données de l'application hôte par exemple. Il ne pourra pas non plus accéder aux variables d'environnement, aux fichiers en tout genre (config incluse), aux sockets, etc.


### Des exemples de services en WASM

TO BE CONTINUE...

