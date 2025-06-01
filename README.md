# Packaging schedule for glazed cheese

## Prerequisites

Install Java and Maven, for example with [Sdkman](https://sdkman.io):

```bash
sdk install java
sdk install maven
```

## Run the application

Git clone food-packaging
 repo and navigate to this directory:

```bash
git clone git@github.com:savushkin-r-d/food-packaging.git
cd food-packaging/
```

## Start the application with Maven

The program takes the date of the production order request as the argument of the main method.

```bash
mvn clean install
java -jar target/food-packaging-run.jar 2025-05-28
```

The data for the solver from the database in `src/main/resources/importFiles/2025-05-28Import.yaml`.

The solution will be recorded in `src/main/resources/exportFiles/2025-05-28Export.json`.
