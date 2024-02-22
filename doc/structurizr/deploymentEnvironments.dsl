deploymentEnvironment "DevelopmentLocal" {
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
}

deploymentEnvironment "DevelopmentCloud" {
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