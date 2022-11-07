# Domain Model

```mermaid
classDiagram
  class Norm
  Norm : +UUID guid
  Norm : +String longTitle
  Norm : +List~Article~ articles

  class Article
  Article : +UUID guid
  Article : +String marker
  Article : +String title
  Article : +List~Paragraph~ paragraphs

  class Paragraph
  Paragraph : +UUID guid
  Paragraph : +String marker
  Paragraph : +String text

  Norm "1" --> "*" Article
  Article "1" --> "*" Paragraph
```
