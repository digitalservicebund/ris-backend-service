# 13. Using basic database constraints

Date: 2023-05-30

## Status

Proposed

## Context

We need to ensure data consistency and integrity at all levels of the RIS applications as we are hosting important data and plan to be the reference for legal information provided by the Federal Republic of Germany. A part of data consistency and integrity is [referential integrity](https://en.wikipedia.org/wiki/Referential_integrity). Until now the part for norms was not ensuring this at database level to have a higher development speed. With a growing database model this increases the probability of corrupting data through unwanted mistakes on backend level or while database migrations.

## Decision

To ensure basic referential integrity we ensure that we define the following constraints whereever possible:

* Foreign or referential keys to ensure that dependent data cannot be written without the referenced data
* Unique constraints for attributes and columns which must not be duplicated
* Not null constraints for attributes and columns where a value is required

## Consequences

In consequence the data model will be more safe against data corruption by mistake. A consequence on development side is a small bit of effort which must be invested when thinking about and changing the data model.

## References

* [Referential integrity on wikipedia](https://en.wikipedia.org/wiki/Referential_integrity)
* ["Nullable type" article mentioning "billion dollar mistake" by C.A.R. Hoare](https://en.wikipedia.org/wiki/Nullable_type)
* [Star schema for denormalized data in OLAP (what RIS is not)](https://en.wikipedia.org/wiki/Star_schema)
* Discussions on database constraints (which do merely not apply to RIS)
    * [Database constraints considered harmful?](https://dev.to/jonlauridsen/database-constraints-considered-harmful-38)
    * [9 reasons why there are no foreign keys constraints in your database](https://dataedo.com/blog/why-there-are-no-foreign-keys-in-your-database-referential-integrity-checks)
