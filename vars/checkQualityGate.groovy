def call(Map config = [:]) {

    def timeoutMinutes = config.timeoutMinutes ?: 5
    def failOnError    = config.failOnError ?: false

    timeout(time: timeoutMinutes, unit: 'MINUTES') {

        def qg = waitForQualityGate abortPipeline: false

        echo "Quality Gate status: ${qg.status}"
        if (qg.status != 'OK') {
            if (failOnError) {
                error("SonarQube Quality Gate failed: ${qg.status}")
            } else {
                echo "Quality Gate warning: ${qg.status}"
            }
        } else {
            echo "Quality Gate passed successfully"
        }
    }
}
