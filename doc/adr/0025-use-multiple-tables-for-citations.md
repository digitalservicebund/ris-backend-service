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

We want to create new tables to better organize the references. For each type of reference (e.g. caselaw cites caselaw, caselaw cites dependent literature, dependent literature cites caselaw, ...) we create the following tables:
* `citation_{source}_to_{target}_link`
    * references both the active and passive side using an identifier
    * Rank column for sorting on the active side. The rank values are shared with `citation_{source}_to_{target}_blindlink_active`.
* `citation_{source}_to_{target}_blindlink_active`
    * references the active side using an identifier
    * references the passive side using various information based on the type of reference
    * Rank column for sorting on the active side. The rank values are shared with `citation_{source}_to_{target}_link`.
* `citation_{source}_to_{target}_blindlink_passive`
    * references the passive side using an identifier
    * references the active side using various information based on the type of reference

### Example for independent literature (SLI) and caselaw decisions

* `citation_caselaw_to_sli_link`
    * A caselaw decision cites an SLI.
    * Both the SLI and the caselaw decision exist.
* `citation_caselaw_to_sli_blindlink_active`
    * A caselaw decision cites an SLI.
    * The caselaw decision exist. The SLI does not.
* `citation_caselaw_to_sli_blindlink_passive`
    * A caselaw decision cites an SLI.
    * The SLI exist. The caselaw decision does not.
    * This table does not exist in our (caselaw) database.
* `citation_sli_to_caselaw_link`
    * An SLI cites a caselaw decision.
    * Both the SLI and the caselaw decision exist.
    * This table does not exist in our (caselaw) database but in the global ref-schema.
* `citation_sli_to_caselaw_blindlink_active`
    * An SLI cites a caselaw decision.
    * The SLI exist. The caselaw decision does not.
    * This table does not exist in our (caselaw) database.
* `citation_sli_to_caselaw_blindlink_passive`
    * An SLI cites a caselaw decision.
    * The caselaw decision exist. The SLI does not.

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

### ULI

- [ ] write something about how they interact with Fundstellen

## Consequences

- We have a better and clearer database structure that enables us to do more advanced work with references.
- We can properly manage publishing and unpublishing links.
- We need to invest the time and effort into migrating our code and data into the new database structure. We need to do this using rolling-updates.

## Additional Small Decisions

* We no longer store the raw values for courts for blind-links: We have very few entries with courts we can not identify for caselaw citations and asked the doc-offices to fix these.
* We no longer store the raw values for document types for blind-links: There are no missing entries for caselaw citations
* We do not create extra tables for the fields shared between the three reference tables as these are only very few fields (e.g. for caselaw citations it's only the citation_type_id).
* We use the id of a doc unit for the foreign key and not the document_number. Using the document_number created problems with hibernate.
* We prefix the columns in the tables with `active_` or `passive_` to identify the side of the relation for which the column stores information.
