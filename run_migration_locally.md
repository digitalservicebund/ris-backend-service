# Local Migration

## Background

The [migration appplication](https://github.com/digitalservicebund/ris-data-migration) transforms XML data from the jDV into a relational DB structure. This includes lookup tables (courts, norm abbreviations, keywords and many more) and documentation units. The caselaw application uses the database that is filled by the migration application.

This instruction will allow engineers to setup their local environment with the lookup tables, that are necessary to run the caselaw application, and some test documentation units if desired.

## Prerequisites

1. Being inside the `ris-backend-service` directory, move one level up and clone the [ris-data-migration](https://github.com/digitalservicebund/ris-data-migration) repository:

   ```bash
   cd ..
   git clone git@github.com:digitalservicebund/ris-data-migration.git
   ```

2. Follow the steps [here](https://platform-docs.prod.ds4g.net/user-docs/how-to-guides/access-obs-via-aws-sdk/) to get access to OTC buckets via command line. You can use the `AWS_` environment variables that you use for [neuris-infra](https://github.com/digitalservicebund/neuris-infra). Add them to your local `.env` file, that has been generated before (through `./run.sh init`):

   ```bash
   AWS_ACCESS_KEY_ID=YOUR_KEY_HERE
   AWS_SECRET_ACCESS_KEY=YOUR_SECRET_HERE
   AWS_BUCKET_NAME=neuris-migration-juris-data
   ```

   Check if you can access the right bucket with:

   ```bash
   aws s3 ls --profile otc --endpoint-url https://obs.eu-de.otc.t-systems.com s3://neuris-migration-juris-data
   # output should look like this:
   #                           PRE daily/
   #                           PRE monthly/
   ```

3. Make sure [docker-cli](https://www.docker.com/) is running: `docker --version`

## Import Data

### Checkout Migration at Current Tag

Change to the location where you checked out the [ris-data-migration](https://github.com/digitalservicebund/ris-data-migration) repository. Pull the latest state

```bash
cd ../ris-data-migration
git pull
```

### Import with Script

Move back to the `ris-backend-service` repo directory where you can execute the import by:

```bash
cd ../ris-backend-service
./run_migration_locally.sh
```

This will use the environment variables from `.env` to download the xml files from the s3 bucket to `../ris-data-migration/juris-xml-data`.

In parallel it will spin up a docker container with name `postgres14`, which contains the database schema.

And it runs the migration which can take a while. Wait for it to complete.

To repeat files downloading, remove the `../ris-data-migration/juris-xml-data` directory, and rerun.

### Import Manually

#### Initialize the Schema

1. In `ris-backend-service` start the database

   ```bash
   cd ris-backend-service
   docker compose up postgres14
   # or use your favourite startup command, e.g. ./run.sh dev --no-backend
   ```

2. All data that is migrated by the migration application resides in the `incremental_migration` DB schema. Make sure it exists in your local database

3. Create a directory where you will store the xml files to import into the database
   in `ris-data-migration`

   ```
   cd ris-data-migration
   mkdir juris-xml-data
   ```

4. Download the lookup tables in `ris-data-migration`

   ```bash
   aws s3 cp --profile otc --endpoint-url https://obs.eu-de.otc.t-systems.com --recursive s3://neuris-migration-juris-data/monthly/2024/10/Tabellen ./juris-xml-data/Tabellen
   ```

5. Download example BGH DocumentationUnits in `ris-data-migration`

   ```bash
   aws s3 cp --profile otc --endpoint-url https://obs.eu-de.otc.t-systems.com --recursive s3://neuris-migration-juris-data/monthly/2024/05/BGH-juris/RSP/2022/ ./juris-xml-data/BGH-juris/RSP/2022/
   ```

6. Setup your local .env file with this command as described
   in [Set up local env](https://github.com/digitalservicebund/ris-data-migration#set-up-local-env)

7. Change the `.env` file in `ris-migration-data` with the following variables:

   ```bash
   RIS_MIGRATION_TABLES_LOCATION=juris-xml-data
   RIS_MIGRATION_INCLUDE_NORM_ABBREVIATIONS=true
   RIS_MIGRATION_CLI_MODE=true

   # database config
   RIS_MIGRATION_DB_HOST=localhost
   RIS_MIGRATION_DB_PORT=5432
   RIS_MIGRATION_DB_NAME=neuris
   RIS_MIGRATION_DB_USER=migration
   RIS_MIGRATION_DB_PASSWORD=migration
   RIS_MIGRATION_DB_SCHEMA=caselaw
   ```

8. For console logging

   ```bash
   export SPRING_PROFILES_ACTIVE=dev
   ```

9. Build the ris-data-migration application into a jar

   ```bash
   ./gradlew :cli:bootJar
   ```

10. Import the lookup tables

    ```bash
    java -jar cli/build/libs/ris-data-migration-cli.jar juris-table seed
    ```

11. Import the BGH DocumentationUnits

    ```bash
    java -jar cli/build/libs/ris-data-migration-cli.jar juris-r migrate -p juris-xml-data/
    ```

### Update the Lookup Tables / Reimport for Backfilling

1. Pull `ris-data-migration`

2. Download the new lookup tables and document units (see steps 4 and 5 above)

3. If you need to fill new categories in all documentation units, truncate the `caselaw`/`incremental_migration` schema

4. To update/reimport new data, repeat steps 9 - 12 above

#### Update by script:

Delete the folder in pointed in `DATA_MIGRATION_IMPORT_PATH`, update the month/day in the s3 commands and rerun the script. It will download the necessary files again.

```bash
./run_migration_locally.sh
```
