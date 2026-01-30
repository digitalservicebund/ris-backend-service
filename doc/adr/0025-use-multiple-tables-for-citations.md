# 25. References

Date: 2026-01-16

## Status

Proposed

## Context

Various other documents can be referenced from a doc unit. We need to store these references in our database.

We also need to publish these references to a NeuRIS-wide database and include them in the LDML. We also need to
calculate passive references based on the active references found in our documents and the NeuRIS-wide database.

It is possible to create a reference to a document that does not exist in NeuRIS. We call these **blind-links**.

Our current database structure is not setup in a way to support these. Currently active & passive references as well as
blind-links and links are all stored in the same table. Especially the table for references between caselaw doc units
contains a lot of different references (like the "Rechtszug") and various fields only used by some of them.

We also have unpublished documents in our database which can not be used as targets for references by other doc offices.
References to these unpublished documents may only be published as blind-links.

We also need to handle unpublishing of documents.

## Decision

We want to create new tables to better organize the references. For each type of reference (caselaw, independent literature, dependent literature, administrative regulation) we create the following tables, if we have a need for them:
* `citation_{type}_link_active` (in the `caselaw` case just `citation_caselaw_link`)
    * Active citations having caselaw documents as sources
    * references both the source and target using an identifier
    * Rank column for sorting on the active side. The rank values are shared with `citation_{type}_blindlink_active`.
* `citation_{type}_blindlink_active`
    * Active citations having caselaw documents as sources
    * references the source using an identifier
    * references the target using various information based on the type of reference
    * Rank column for sorting on the active side. The rank values are shared with `citation_{type}_link`.
* `citation_{type}_blindlink_passive`
    * Passive citations having caselaw documents as targets
    * references the target using an identifier
    * references the source side using various information based on the type of reference
* `citation_{type}_link_passive`
  * In cases in which we need to store additional information about passive links targeting caselaw documents
  * The reference in the global ref schema is referenced using its id


### Example for dependent literature (ULI) and caselaw decisions

* `citation_uli_link_active`
    * A caselaw decision cites an ULI.
    * Both the SLI and the caselaw decision exist.
* `citation_uli_blindlink_active`
    * A caselaw decision cites an ULI.
    * The caselaw decision exist. The ULI does not.
* `citation_uli_blindlink_passive`
    * An ULI cites a caselaw decision.
    * The caselaw decision exist. The ULI does not.
* `citation_uli_link_passive`
    * An ULI cites a caselaw decision.
    * Both the SLI and the caselaw decision exist.
    * This table has only additional information about this reference. The reference itself lives in the global ref schema

### Publishing

When publishing we also publish the relevant references from these table to the NeuRIS-wide reference table:
 * Links for which both sides are published will be published
 * Links for which only the active side is published are published as blind-links.
 * Links for which only the passive side is published are NOT published.
 * Blind-links are always published

### Unpublishing

If a document is unpublished:
1. all active links from the document are removed
2. all passive links targeting the document are removed
3. all active links targeting the document are converted to blind-links

### Showing Passive Links in our software

For showing passive references in our software we are only showing data from the NeuRIS-wide reference table. This
ensures only published references are shown and unpublished data can not be seen by other doc-offices.

### Migration

During the migration we do not know of all possible reference targets. Therefore, we can not reliably write references
into the link tables. We only create blind-links during the main part of the migration and then convert active
blind-links with an existing target in a post-processing step to links. Afterward, we remove active and passive
blind-links for which the link also exists in the link table.

## Consequences

- We have a better and clearer database structure that enables us to do more advanced work with references.
- We can properly manage publishing and unpublishing links.
- We need to invest the time and effort into migrating our code and data into the new database structure. We need to do this using rolling-updates.

## Additional Small Decisions

* We no longer store the raw values for courts for blind-links: We have very few entries with courts we can not identify for caselaw citations and asked the doc-offices to fix these.
* We no longer store the raw values for document types for blind-links: There are no missing entries for caselaw citations
* We do not create extra tables for the fields shared between the three reference tables as these are only very few fields (e.g. for caselaw citations it's only the citation_type_id).
* We use the id of a doc unit for the foreign key and not the document_number. Using the document_number created problems with hibernate.
* We prefix the columns in the tables with `source_` or `target_` to identify the side of the relation for which the column stores information.
