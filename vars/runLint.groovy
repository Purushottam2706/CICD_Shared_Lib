def call(String lintCommand) {

    if (!lintCommand?.trim()) {
        error("Lint command must be provided")
    }
    try {
        sh """
            set -e
            ${lintCommand}
        """
        echo "Lint completed successfully"
    } catch (Exception e) {
        echo "Lint failed"
        throw e
    }
}
