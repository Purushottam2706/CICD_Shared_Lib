def call(Map config = [:]) {

    def credentialsId = config.credentialsId
    def branchName    = config.branchName
    def targetSHA     = config.targetSHA
    def workspace     = config.workspace
    def repoSlug      = config.repoSlug
    def mode          = config.mode ?: "reset" 
    def reason        = config.reason ?: "Pipeline failure"

    if (!branchName || !targetSHA) {
        error("branchName and targetSHA are required for rollback")
    }

    echo "=" * 80
    echo "ROLLBACK INITIATED"
    echo "Branch: ${branchName}"
    echo "Mode: ${mode}"
    echo "Reason: ${reason}"
    echo "=" * 80

    withBitbucketAuth (
        workspace: workspace, 
        repoSlug: repoSlug,
        credentialsId: credentialsId
    ) {

        sh """
            set -e

            git fetch origin ${branchName}
            git checkout -B ${branchName} origin/${branchName}
        """

        if (mode == "revert") {
            echo "Performing SAFE revert..."
            def parentCount = sh(
                script: "git cat-file -p ${targetSHA} | grep '^parent' | wc -l",
                returnStdout: true
            ).trim()

            if (parentCount != '2') {
                error("Rollback aborted: ${targetSHA} is NOT a merge commit")
            }

            def revertStatus = sh(
                script: "git revert ${targetSHA} -m 1 --no-edit",
                returnStatus: true
            )

            if (revertStatus != 0) {
                error("git revert failed for ${targetSHA}")
            }

            sh "git push origin ${branchName}"

            echo "Revert completed successfully"

        } else if (mode == "reset") {
            echo "Performing FORCE reset..."
            sh """
                git reset --hard ${targetSHA}
                git push origin ${branchName} --force
            """
            echo "Force reset completed successfully"
        } else {
            error("Invalid rollback mode: ${mode}")
        }
    }
}
