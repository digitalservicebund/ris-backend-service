workspace {

    model {
        caselawDocumentary = person "Caselaw Documentary" 
        normsDocumentary = person "Norms Documentary"
        systemAdministrator = person "System Administrator"
        user = person "User"
        publicUser = person "Public User" "A public human user" "future"


        group "DigitalService" {
            ris = softwareSystem "RIS" {
                risFrontend = container "Web-Anwendung" "Stellt das grafische Nutzerinterface zur Verfügung" "Typescript, Vue"
                risBackend = container "API Anwendung" "Bietet sämtliche backend Funktionalitäten zum Dokumentieren an" "Java, Kotlin, Spring, WebFlux" {
                    !include ./components.dsl
                }
                sessionStore = container "Nutzersession Speicher" "Speicher Nutzersession zwischen" "Redis" "datastore"
                database = container "Datenbank" "Speichert alle Dokumente und Tabellen" "Postgresql" "datastore"
                fileStore = container "Datei Speicher" "Speichert alle Dokumente und Tabellen" "S3 compatible" "datastore"

                monitoring = container "Monitoring" "Stellt Systeminformationen grafisch da" "Grafana"

                publicationStore = container "Publikationsspeicher" "Speichert die öffentlich verfügbaren Dokumente für das Portal" "S3(?), LegalDocML" "datastore,future"
                portalBackend = container "Portal API-Anwendung" "Indiziert und stellt Suchfunktion" "Java(?)" "future"
                portalFrontend = container "Portal Web-Anwendung" "Grafisches Nutzerinterface zur Recherche" "Typescript, Vue (?)" "future"

                risMigration = container "Migration Tool" "Kommandozeilenwerkzeug zur Übernahme der jDV Daten" "Java" "migration"
            }
        }
        
        group "juris" {
            jurisDocumentationManagement = softwareSystem "jDV"
        }
        
        group "Data consumers" {
            eLegislation = softwareSystem "E-Gesetzgebung"
        }

        group "Externe Anbieter" {
            openIdProvider = softwareSystem "IAM Anbieter" "Bare.ID (OpenID Connect)" "saas"
            emailApiProvider = softwareSystem "E-Mail Anbieter (Versand)" "SendInBlue" "saas"
            emailHoster = softwareSystem "E-Mail Anbieter (Empfang)" "IONOS" "saas"

            errorMonitoring = softwareSystem "Error Tracker" "Sentry überwacht und sammelt auftretende Fehler" "saas"
        }

        # relationships between people and software systems
        caselawDocumentary -> ris "documents verdicts"
        normsDocumentary -> ris "documents norms"
        normsDocumentary -> jurisDocumentationManagement "uploads norms"

        ris -> jurisDocumentationManagement "push verdicts"

        eLegislation -> ris "search norms"

        user -> openIdProvider "authentifiziert sich" "HTTPS"
        normsDocumentary -> openIdProvider "authentifiziert sich" "HTTPS"
        caselawDocumentary -> openIdProvider "authentifiziert sich" "HTTPS"
        systemAdministrator -> openIdProvider "verwaltet Nutzer" "HTTPS"

        publicUser -> portalFrontend "nutzt" "HTTPS"

        # relationships to/from containers
        user -> risFrontend "nutzt" "HTTPS"
        normsDocumentary -> risFrontend "nutzt" "HTTPS"
        caselawDocumentary -> risFrontend "nutzt" "HTTPS"
        
        systemAdministrator -> monitoring "liest Systeminformationen" "HTTPS"

        risFrontend -> risBackend "nutzt" "JSON/HTTPS"
        risFrontend -> errorMonitoring "sendet Fehler" "JSON, HTTPS" 

        risBackend -> database "speichern & lesen" "R2DBC"
        risBackend -> sessionStore "speichern & lesen" "RESP"
        risBackend -> fileStore "speichern & lesen" "S3 Protocol"
        risBackend -> openIdProvider "prüft Nutzer" "HTTPS"
        risBackend -> emailApiProvider "sendet E-Mails" "XML, HTTPS"
        risBackend -> publicationStore "publiziert nach" "LegalDocML, HTTPS"
        risBackend -> errorMonitoring "sendet Fehler" "JSON, HTTPS"

        monitoring -> risBackend "holt Systeminformationen" "HTTPS"
        
        portalFrontend -> portalBackend "nutzt" "HTTPS"
        portalFrontend -> publicationStore "verweist auf" "HTTPS"
        portalBackend -> publicationStore "indiziert" "HTTPS(?)"

        # relationships to/from components


        deploymentEnvironment "Development" {
            deploymentNode "Developer Laptop" "" "Microsoft Windows 10 or Apple macOS" {
                deploymentNode "Web Browser" "" "Chrome, Firefox, Safari, or Edge" {
                    developerRisFrontendInstance = containerInstance risFrontend
                }
                deploymentNode "Docker Container - Web Anwendung" "" "Docker" {
                    deploymentNode "Spring Boot Application" "" "Spring Boot 3.x" {
                        developerRisBackendInstance = containerInstance risBackend
                    }
                }
                deploymentNode "Docker Container - Database Server" "" "Docker" {
                    deploymentNode "Database Server" "" "Postgresql" {
                        developerDatabaseInstance = containerInstance database
                    }
                }
            }
            deploymentNode "Cloud" "" "Open Telecom Cloud" "" {
                deploymentNode "Compute Cluster" "" "Kubernetes" "" {
                    containerInstance risFrontend
                    containerInstance risBackend
                }
                deploymentNode "Managed Database" "" "PostgreSQL" "" {
                    containerInstance database
                }
                deploymentNode "Object Storage" "" "AWS S3 compatible" "" {
                    containerInstance fileStore
                }
            }

        }
    }

    views {
        systemContext ris "SystemContext" {
            include *
            exclude user
            exclude openIdProvider
            exclude emailApiProvider
        }

        container ris "ContainerView" {
            include *
            exclude caselawDocumentary
            exclude normsDocumentary
        }

        systemlandscape "SystemLandscape" {
            include *
            exclude caselawDocumentary
            exclude normsDocumentary
            exclude systemAdministrator
            exclude publicUser
        }

        component risBackend "BackendComponents" {
            include *
        }

        styles {
            element "Person" {
                color #ffffff
                background #08427B
                fontSize 22
                shape Person
            }
            element "Software System" {
                color #ffffff
                background #1168BD
            }
            element "Container" {
                color #ffffff
                background #438DD5
            }
            element "datastore" {
                shape Cylinder
            }
            element "saas" {
                background grey
            }
            element "future" {
                background #DB005B
            }
            element "migration" {
                background #02a35a
            }

        }
    }

}