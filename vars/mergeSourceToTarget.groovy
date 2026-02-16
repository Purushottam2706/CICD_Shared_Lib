def call(Map config = [:]) {

    def sourceBranch  = config.sourceBranch
    def targetBranch  = config.targetBranch
    def prId          = config.prId ?: null
    def workspace     = config.workspace
    def repoSlug      = config.repoSlug
    def credentialsId = config.credentialsId ?: 'bitbucket-user-name-cred'
    def commitEmail   = config.commitEmail ?: "jenkins@ci.local"
    def commitName    = config.commitName ?: "jenkins-ci"
    def commentOnPR   = config.commentOnPR ?: false

    if (!sourceBranch || !targetBranch) {
        error("sourceBranch, targetBranch and prId are required")
    }

    def preMergeSHA = ""
    def mergeCommitSHA = ""
    def mergeCompleted = false


    withBitbucketAuth(
        workspace: workspace,
        repoSlug: repoSlug,
        credentialsId: credentialsId,
        gitEmail:commitEmail,
        gitUser: commitName

    ) {
        try {

            sh """
                set -e

                git reset --hard
                git clean -fd

                git fetch origin ${targetBranch}:refs/remotes/origin/${targetBranch}
                git fetch origin ${sourceBranch}:refs/remotes/origin/${sourceBranch}
            """

            preMergeSHA = sh(
                script: "git rev-parse origin/${targetBranch}",
                returnStdout: true
            ).trim()

            sh """
                git checkout -B ${targetBranch} origin/${targetBranch}
                git merge origin/${sourceBranch} --no-ff \
                    -m "Auto-merged via Jenkins"
            """

            mergeCommitSHA = sh(
                script: "git rev-parse HEAD",
                returnStdout: true
            ).trim()

            // sh "git push origin ${targetBranch}"
            sh "git push origin HEAD:${targetBranch}"

            mergeCompleted = true
            if(commentOnPR)
            {
                sh """
                    curl -s -u ${BB_USER}:${BB_TOKEN} -X POST \
                        -H "Content-Type: application/json" \
                        -d '{"content":{"raw":"Auto-merge successful. Code is now in ${targetBranch} branch."}}' \
                        https://api.bitbucket.org/2.0/repositories/${workspace}/${repoSlug}/pullrequests/${prId}/comments
                """
            }

            echo "Merge completed successfully"

            return [
                preMergeSHA: preMergeSHA,
                mergeCommitSHA: mergeCommitSHA,
                mergeCompleted: mergeCompleted
            ]

        } catch (Exception e) {
            error("Merge to ${targetBranch} failed  ${e.getMessage()}")
            throw e
        }
    }
}
