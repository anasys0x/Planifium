# Tests

Cette section décrit l’approche de test utilisée pour valider le bon
fonctionnement de l’application dans le cadre du **Devoir 3**.

## Stratégie de tests
Les tests sont principalement des **tests unitaires** développés avec **JUnit 5**.
Chaque test cible une fonctionnalité précise et est isolé autant que possible
des dépendances externes.

Lorsque nécessaire, des **mocks** sont utilisés afin de :<br>
- simuler les appels à l’API externe (Planifium) ;<br>
- isoler la logique métier des accès aux données ;<br>
- garantir des tests reproductibles.

---

## Organisation
Les tests sont organisés selon les composants de l’application :<br>
- services (logique métier) ;<br>
- contrôleurs (validation et gestion des erreurs) lorsque pertinent.

---

## Exécution des tests
Les tests peuvent être exécutés via Maven :

```bash
mvn test
