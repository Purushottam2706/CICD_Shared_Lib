def call(Map config = [:]) {

    def sonarInstance = config.sonarInstance ?: "Sonarqube"
    def projectKey    = config.projectKey ?: ""
    def projectName   = config.projectName ?: ""
    def sources       = config.sources ?: "."
    def extraParams   = config.extraParams ?: ""

    if (!projectKey?.trim()) {
        error("Sonar projectKey must be provided")
    }
    withSonarQubeEnv(sonarInstance) {
        sh """
            set -e
            
            ${env.SONAR_HOME}/bin/sonar-scanner \
            -Dsonar.projectKey=${projectKey} \
            -Dsonar.projectName=${projectName} \
            -Dsonar.sources=${sources} \
            ${extraParams}
        """
    }
    echo "SonarQube analysis completed"
}
