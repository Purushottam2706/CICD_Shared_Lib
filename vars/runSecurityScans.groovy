def call(Map config = [:]) {

    def trivyEnabled      = config.trivyEnabled ?: true
    def owaspEnabled      = config.owaspEnabled ?: true
    def failOnError       = config.failOnError ?: false
    def trivySeverity     = config.trivySeverity ?: "HIGH,CRITICAL"
    def owaspFailCVSS     = config.owaspFailCVSS ?: 7
    def owaspInstallation = config.owaspInstallation ?: "OWASP-DC"

    parallel(

        trivyScan: {
            if (trivyEnabled) {
                echo "Running Trivy security scan..."
                def status = sh(
                    script: """
                        trivy fs . \
                        --severity ${trivySeverity} \
                        --format json \
                        --output trivy-report.json
                    """,
                    returnStatus: true
                )
                if (status != 0) {
                    echo "Trivy detected vulnerabilities"

                    if (failOnError) {
                        error("Trivy scan failed")
                    }
                } else {
                    echo "Trivy scan passed"
                }
            }
        },

        owaspScan: {
            if (owaspEnabled) {
                echo "Running OWASP Dependency Check..."

                try {

                    dependencyCheck additionalArguments: """
                        --scan . \
                        --format HTML \
                        --out dependency-check-report \
                        --failOnCVSS ${owaspFailCVSS}
                    """,
                    odcInstallation: owaspInstallation

                    publishHTML(target: [
                        allowMissing: false,
                        keepAll: true,
                        reportDir: 'dependency-check-report',
                        reportFiles: 'dependency-check-report.html',
                        reportName: 'OWASP Dependency Check'
                    ])

                } catch (Exception e) {
                    echo "OWASP Dependency Check detected issues"

                    if (failOnError) {
                        error("OWASP Dependency Check failed")
                    }
                }
            }
        }
    )

    echo "Security scan stage completed"
}
