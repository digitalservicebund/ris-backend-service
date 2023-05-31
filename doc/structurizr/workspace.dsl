workspace {

    model {
        caselawDocumentary = person "Caselaw Documentary" 
        normsDocumentary = person "Norms Documentary"
        systemAdministrator = person "System Administrator"
        human = person "User"

        group "DigitalService" {
            ris = softwareSystem "RIS" {
                risFrontend = container "Web-Anwendung" "Stellt das grafische Nutzerinterface zur Verfügung" "Typescript, Vue"
                monitoring = container "Monitoring" "Stellt Systeminformationen grafisch da" "Grafana"
                risBackend = container "API Anwendung" "Bietet sämtliche backend Funktionalitäten zum Dokumentieren an" "Java, Kotlin, Spring, WebFlux"
                sessionStore = container "Nutzersession Speicher" "Speicher Nutzersession zwischen" "Redis" "datastore"
                database = container "Datenbank" "Speichert alle Dokumente und Tabellen" "Postgresql" "datastore"
                fileStore = container "Datei Speicher" "Speichert alle Dokumente und Tabellen" "S3 compatible" "datastore"
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
            emailApiProvider = softwareSystem "E-Mail Anbieter" "SendInBlue" "saas"
        }

        # relationships between people and software systems
        caselawDocumentary -> ris "documents verdicts"
        normsDocumentary -> ris "documents norms"
        normsDocumentary -> jurisDocumentationManagement "uploads norms"

        ris -> jurisDocumentationManagement "push verdicts"

        eLegislation -> ris "search norms"

        human -> openIdProvider "authentifiziert sich" "HTTPS"
        normsDocumentary -> openIdProvider "authentifiziert sich" "HTTPS"
        caselawDocumentary -> openIdProvider "authentifiziert sich" "HTTPS"
        systemAdministrator -> openIdProvider "verwaltet Nutzer" "HTTPS"


        # relationships to/from containers
        human -> risFrontend "nutzt" "HTTPS"
        normsDocumentary -> risFrontend "nutzt" "HTTPS"
        caselawDocumentary -> risFrontend "nutzt" "HTTPS"
        
        systemAdministrator -> monitoring "liest Systeminformationen" "HTTPS"

        risFrontend -> risBackend "nutzt" "JSON/HTTPS"
        monitoring -> risBackend "holt Systeminformationen" "HTTPS"

        risBackend -> database "speichern & lesen" "R2DBC"
        risBackend -> sessionStore "speichern & lesen" "RESP"
        risBackend -> fileStore "speichern & lesen" "S3 Protocol"
        risBackend -> openIdProvider "prüft Nutzer" "HTTPS"
        risBackend -> emailApiProvider "sendet E-Mails" "XML, HTTPS"


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
                deploymentNode "bigbank-dev001" "" "" "" {
                    softwareSystemInstance ris
                }
            }

        }
    }

    views {
        systemContext ris "SystemContext" {
            include *
            exclude human
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

        }
    }

}