def call(Map config = [:]) {
    def customPathExport = config.customPathExport ?: ""
    def buildCommand     = config.buildCommand ?: ""
    def verifyDir        = config.verifyDir ?: "dist"

    if (!buildCommand) {
        error("buildCommand is required")
    }
    try{

    sh """
        set -e
        ${customPathExport}
        ${buildCommand}
        [ -d "${verifyDir}" ] || { echo "${verifyDir} missing"; exit 1; }
    """

    archiveArtifacts(
        artifacts: "${verifyDir}/**/*",
        fingerprint: true,
        allowEmptyArchive: false
    )
    } catch (Exception e) {
        error("error while bundling the code ${e.getMessage()}")
        throw e
    }
}
