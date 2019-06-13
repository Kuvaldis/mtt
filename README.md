# Description

Money transfer test example.

* Java 11 is required.
* Only minimum necessary REST API operations are added. For more explanations see 'Rationale' section.
* Body format should be "application/json" for both, request and response.
* Service is run on port 8080 by default.

Following operations are provided:
* **POST /users**. Creates a new user. Http request example:
```json
{
  "username": "ross"
}
```
* **GET /users/{userId}**. Returns a user by its id. Http response example:
```json
{
  "id": 1,
  "username": "rachel"
}
```
* **POST /accounts**. Creates a new account for a user. User may have several accounts. Only one currency is supported. Http request example:
```json
{
  "userId": 1,
  "balance": 20.15
}
```
* **GET /accounts/{accountId}**. Returns an account by its id. Http response example:
```json
{
  "id": 1,
  "userId": 1,
  "balance": 20.15
}
```
* **POST /transfers**. Transfers money from one account to another on behalf of a user.
```json
{
  "endUserId": 1,
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 12.35
}
```

# How to

## Build

Run following command from the root directory.
```bash
./gradlew clean build
```

## Run integration tests

Run following command from the root directory.
```bash
./gradlew integrationTest
```

## Run using Jooby plugin

Run following command from the root directory.
```bash
./gradlew joobyRun
```

## Run as a standalone

1. Build project, run integration tests if required
2. Unpack zip/tar.gz archive from build/distributions
3. Go to unpacked directory, run `./bin/mtt`.

# Rationale

## API

* Only minimal operations are present. No PUT, DELETE etc because they are not required to demonstrate transfer mechanism.
* Only one currency is supported. It would be a good feature, but implementation is quite trivial in it's simplest form and only complicates test example.

## Main Framework

Jooby was chosen as a main framework because it's lightweight, but still has some convenient features
like dependency injection. It has a lot of common extensions.

## Low level architecture

Only controllers and repositories layer are created for simplicity.
All main business logic is added to controller layer.

In a real world service there would be a business logic service layer, 
but for our scenario there was no need to create one.

## Data Storage

Two options have been considered: H2 and simple Java implementation.
Even though for current task latter could be enough, the solution might become too complicated and error prone.

H2 pros:
* Still lightweight with in memory mode.
* Has SQL support, database schema, constraints etc. It makes data manipulation easier, provides data integrity.
* Closer to the real world scenarios.
* Provides flexibility.

## Transaction support

Custom implementation is used.

All controllers are within transactional context. I.e. at a time business logic is performed 
there already is an open connection in a transaction mode created.

Can be easily modified to create a transaction per route, but it's not required.

## Validation

No validation framework (e.g. Hibernate Validator) was used since it only complicates things for our simple case.

## Tests

Lots of dependencies are in fact for testing purposes, and are not part of final distributive.
Rest easy is used to provide integration tests.

# Considered Features

This section describes what feature have been considered but not implemented.

* Different currencies. Account may have another currency, so either currency exchange
data should be provided somehow, or different currency account transfers are not allowed.
* Account balance modification as a simple PUT operation. Useful feature, but not really required.
* Remove users, accounts etc. Again, simple DELETE operations, but without concrete
requirements it's hard to come up with valid logic.
* Transfer save. In a RESTful API it should be possible to fetch an entity after it's creation.
Since this feature does not add up to the test task there was no need to implement it.
* Search account/user or make transfer by username. Again, useful but no need for example.