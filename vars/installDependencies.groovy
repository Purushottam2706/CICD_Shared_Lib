def call(Map config = [:]) {

    def installCommand   = config.installCommand ?: ""
    def verifyPath       = config.verifyPath ?: ""
    def customPathExport = config.customPathExport ?: ""

    if (!installCommand?.trim()) {
        error("installCommand must be provided")
    }
    try {

        sh """
            set -e

            echo "Checking existing dependencies..."
            if [ -d "${verifyPath}" ]; then
                echo "${verifyPath} directory exists"
                ls -la ${verifyPath} | head -10
            else
                echo "No existing ${verifyPath} directory found"
            fi
            ${customPathExport}
            echo "Running install command..."
            ${installCommand}
        """
        echo "dependencies installed"

    } catch (Exception e) {
            echo "Dependency installation failed"
            throw e
    }

    if (verifyPath?.trim()) {
        sh """
            echo "Verifying dependency directory..."
            if [ -d "${verifyPath}" ]; then
                echo "${verifyPath} created successfully"
                echo "Package count: \$(find ${verifyPath} -type d -maxdepth 1 | wc -l)"
            else
                echo "${verifyPath} not found after installation"
                exit 1
            fi
        """
    }

    echo "Dependency installation completed successfully"
}
