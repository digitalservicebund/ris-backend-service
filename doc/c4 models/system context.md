RIS system context C4 model

```mermaid
C4Context
title Systemkontext des Rechtsinformationssystems

      Person(person_norms_documentary, "Norm Documentary")
      Person(person_caselaw_documentary, "Case-Law Documentary")

      Boundary(boundary_ris, "RIS") {
        System(system_ris_service, "RIS Service")
      }

      Boundary(boundary_data_provider, "Data Provider") {
        System(system_e_verkuendung, "E-Verkündung")
      }

      Boundary(boundary_juris, "Juris") {
        System(system_jdv, "jDV")
      }

      Boundary(boundary_data_consumer, "Data Consumer") {
        System(system_e_gesetzgebung, "E-Gesetzgebung")
        System(system_open_data_api, "Open Data API")
      }

    Rel(person_norms_documentary, system_ris_service, "documents norms")
    Rel(person_caselaw_documentary, system_ris_service, "documents verdicts")
    Rel(system_ris_service, system_jdv, "push verdicts")
    Rel(system_ris_service, system_e_verkuendung, "subscribe")
    Rel(system_e_gesetzgebung, system_ris_service, "search & read norms")
    Rel(system_open_data_api, system_ris_service, "read & search")
    Rel(system_jdv, system_open_data_api, "read")

    UpdateElementStyle(system_e_verkuendung, $bgColor="grey")
    UpdateElementStyle(system_open_data_api, $bgColor="grey")
```
