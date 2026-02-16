def call(Map config = [:]) {

    def targetBranch   = config.targetBranch ?: ""
    def sourceBranch   = config.sourceBranch ?: ""
    def credentialsId  = config.credentialsId ?: ""
    def workspace      = config.workspace ?: ""
    def repoSlug       = config.repoSlug ?: ""
    def prId           = config.prId ?: ""
    def commentMessage = config.commentMessage ?: "Auto-merge failed due to conflicts. Please resolve conflicts manually before merging."

    if (!targetBranch || !sourceBranch) {
        error("Missing required parameters for prepareTestMerge")
    }

    withBitbucketAuth (
        workspace: workspace, 
        repoSlug: repoSlug,
        credentialsId: credentialsId
    ) {
        echo "Preparing test merge from ${sourceBranch} into ${targetBranch}"

        sh """
            set -e

            git fetch origin ${targetBranch}:refs/remotes/origin/${targetBranch}
            git fetch origin ${sourceBranch}:refs/remotes/origin/${sourceBranch}

            git checkout -B test-merge origin/${targetBranch}

            if ! git merge --no-ff origin/${sourceBranch} --no-edit --no-commit; then

                git status
                git merge --abort

                curl -s -u ${BB_USER}:${BB_TOKEN} -X POST \
                -H "Content-Type: application/json" \
                -d '{"content": {"raw": "${commentMessage}"}}' \
                "https://api.bitbucket.org/2.0/repositories/${workspace}/${repoSlug}/pullrequests/${prId}/comments"

                exit 1
            else
                echo "Merge successful (test only). Aborting merge."
                git merge --abort
            fi
        """
        echo "Test merge completed successfully"
    }
}
