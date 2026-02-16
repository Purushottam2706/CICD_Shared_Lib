def call(Map config = [:]) {

    def buildCommand     = config.buildCommand ?: ""
    def verifyDirectory  = config.verifyDirectory ?: ""
    def verifyFile       = config.verifyFile ?: ""
    def customPathExport = config.customPathExport ?: ""

    if (!buildCommand?.trim()) {
        error("buildCommand must be provided")
    }
    echo "Starting build process..."
    try {
        sh """
            set -e
            ${customPathExport}
            ${buildCommand}
        """
        echo "Build command completed successfully"
    } catch (Exception e) {
        echo "Build failed during execution"
        throw e
    }

    if (verifyDirectory?.trim()) {
        sh """
            echo "Verifying build directory..."
            if [ ! -d "${verifyDirectory}" ]; then
                echo "Build failed: ${verifyDirectory} directory not found"
                exit 1
            fi
        """
    }
    if (verifyFile?.trim()) {
        sh """
            echo "Verifying build file..."
            if [ ! -f "${verifyFile}" ]; then
                echo "Build failed: ${verifyFile} not found"
                exit 1
            fi
        """
    }
    echo "Build verification successful"
}

