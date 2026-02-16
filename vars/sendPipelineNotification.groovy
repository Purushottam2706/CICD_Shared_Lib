def call(Map config = [:]) {

    def status           = config.status ?: "SUCCESS"
    def recipientEmail   = config.recipientEmail ?: "purushottam@bajajcapital.com"
    def sonarUrl         = config.sonarUrl ?: ""
    def owaspUrl         = config.owaspUrl ?: ""
    def attachTrivy      = config.attachTrivy ?: false
    def failedStage      = config.failedStage ?: "N/A"
    def buildUrl         = env.BUILD_URL ?: ""
    def consoleUrl       = "${env.BUILD_URL}consoleText"

    if (!recipientEmail?.trim()) {
        echo "No recipient email found."
        return
    }

    emailext(
        to: recipientEmail,
        subject: "Jenkins ${status}: ${env.JOB_NAME} #${env.BUILD_TYPE}",
        mimeType: 'text/html',
        attachLog: true,
        attachmentsPattern: attachTrivy ? "trivy-report.json" : "",
        body: """
        <h2>Pipeline Status: ${status}</h2>

        <p><b>Job:</b> ${env.JOB_NAME}</p>
        <p><b>Build:</b> #${env.BUILD_TYPE}</p>
        <p><b>Build URL:</b> <a href="${buildUrl}">${buildUrl}</a></p>
        <p><b>Console Log:</b> <a href="${consoleUrl}">View Full Log</a></p>

        ${status == 'FAILURE' ? "<p><b>Failed Stage:</b> ${failedStage}</p>" : ""}

        ${sonarUrl ? "<p><b>SonarQube:</b> <a href='${sonarUrl}'>View Report</a></p>" : ""}

        ${owaspUrl ? "<p><b>OWASP Report:</b> <a href='${owaspUrl}'>View Report</a></p>" : ""}

        <hr>
        <p>This is an automated Jenkins notification.</p>
        """
    )
}
