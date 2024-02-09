# Local Setup of Migration Database Schema

### Background

The documentation units from the jDV are currently only available in the NeuRIS database with minimal data (document number, file number, court...). It is a provisional solution to enable references, e.g. in the legal process. For the pilot operation, this provisional solution is to be replaced by a complete (=all categories) and continuous migration of the data from the jDV.

The migration team transforms the XML data from the jDV into its own schema, which is independent of NeuRIS (so far). NeuRIS should now adopt this schema and also use it for documentation units that are created via NeuRIS.

In NeuRIS databases, the name of the "old" schema is `public` and the new is `incremental_migration` .

This document will help Developers in NeuRIS to setup the new schema with data in their local environments.

 ### Initialize the Schema

1. In ris-backend-service restart the database to add the user and the scheme:
   ``` bash
   cd ris-backend-service
   docker compose down postgres14 --remove-orphans
   docker compose up postgres14
   # or use your favourite startup command, e.g. ./run.sh dev --no-backend
   ```

2. Make sure the new schema `incremental_migration` has been added

3. Clone [ris-data-migration](https://github.com/digitalservicebund/ris-data-migration) repository

   ```bash
   git clone git@github.com:digitalservicebund/ris-data-migration.git
   cd ris-data-migration
   ```

4. Checkout the repo on the commit used by staging (commit id to be found [here](https://github.com/digitalservicebund/neuris-migration-infra/blob/main/manifests/overlays/staging/kustomization.yaml#L30)):
   ```
   git checkout <commit-hash>
   ```

5. Create a directory where you will store the xml files to import into the database

   ```
   mkdir juris-xml-data
   ```

6. Follow the steps here to get access to OTC buckets via command line. You can use the `AWS_` environment variables that you use for `neuris-infra`: https://platform-docs.prod.ds4g.net/user-docs/how-to-guides/access-obs-via-aws-sdk/

7. Check if you can access the right bucket with
   ```bash
   aws s3 ls --profile otc --endpoint-url https://obs.eu-de.otc.t-systems.com s3://neuris-migration-juris-data
   # output should look like this:
   #                           PRE daily/
   #                           PRE monthly/
   ```

8. Download the lookup tables

   ```bash
   aws s3 cp --profile otc --endpoint-url https://obs.eu-de.otc.t-systems.com --recursive s3://neuris-migration-juris-data/monthly/2023/09/Tabellen ./juris-xml-data/Tabellen
   ```

9. Download BGH DocumentationUnits

   ```bash
   aws s3 cp --profile otc --endpoint-url https://obs.eu-de.otc.t-systems.com --recursive s3://neuris-migration-juris-data/monthly/2023/09/BGH-juris/RSP/ ./juris-xml-data/BGH-juris/RSP/2022/
   ```

10. Setup your local .env file with this command as described in [Set up local env](https://github.com/digitalservicebund/ris-data-migration#set-up-local-env)

11. Change the following variables in the .env file:
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
    RIS_MIGRATION_DB_SCHEMA=incremental_migration
    ```

12. For console logging
       ```bash
       export SPRING_PROFILES_ACTIVE=dev
       ```

13. Build the ris-data-migration application into a jar

    ```bash
    ./gradlew :cli:bootJar
    ```

14. Import the static lookup tables into your new schema (see Confluence "Wertetabellen" to find out what is static and dynamic)
    ```bash
    java -jar cli/build/libs/ris-data-migration-cli.jar refdata seed
    ```

15. Import the dynamic lookup tables

    ```bash
    java -jar cli/build/libs/ris-data-migration-cli.jar juris-table seed
    ```

16. Import the BGH DocumentationUnits

    ```bash
    java -jar cli/build/libs/ris-data-migration-cli.jar juris-r migrate -p juris-xml-data/
    ```



### Upgrade the Schema

1. Optional: Drop the schema (manually) and restart the db

2. Optional: download the new lookup tables and Document Units (see step 8 & 9 above)

3. Checkout the ris-data-migration repo on the new commit used by staging (commit id to be found [here](https://github.com/digitalservicebund/neuris-migration-infra/blob/main/manifests/overlays/staging/kustomization.yaml#L30)):

   ```
   git checkout <commit-hash>
   ```

4. Continue with steps 12 - 15 above
