# Domain Model

```mermaid
classDiagram
  class Norm
  Norm : +Guid guid
  Norm : +String longTitle
  Norm : +String firstParagraph

  class Guid
  Guid : +String identifier

  Norm --* Guid
```
