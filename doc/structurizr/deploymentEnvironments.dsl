deploymentEnvironment "DevelopmentLocalEnv" {
    deploymentNode "Developer Laptop" "" "Microsoft Windows 10 or Apple macOS" {
        deploymentNode "Web Browser" "" "Chrome, Firefox, Safari, or Edge" {
            developerrspFrontendInstance = containerInstance rspFrontend
        }
        deploymentNode "Docker Container - Web Anwendung" "" "Docker" {
            deploymentNode "Spring Boot Application" "" "Spring Boot 3.x" {
                developerrspBackendInstance = containerInstance rspBackend
            }
        }
        deploymentNode "Docker Container - Database Server" "" "Docker" {
            deploymentNode "Database Server" "" "Postgresql" {
                developerDatabaseInstance = containerInstance database
            }
        }
    }
}

deploymentEnvironment "DevelopmentCloudEnv" {
    deploymentNode "Cloud" "" "Open Telecom Cloud" "" {
        deploymentNode "Compute Cluster" "" "Kubernetes" "" {
            containerInstance rspFrontend
            containerInstance rspBackend
        }
        deploymentNode "Managed Database" "" "PostgreSQL" "" {
            containerInstance database
        }
        deploymentNode "Object Storage" "" "AWS S3 compatible" "" {
            containerInstance fileStore
        }
    }
}