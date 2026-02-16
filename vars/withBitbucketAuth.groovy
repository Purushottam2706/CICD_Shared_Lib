def call(Map config = [:], Closure body) {

    def workspace     = config.workspace ?: ""
    def repoSlug      = config.repoSlug ?: ""
    def credentialsId = config.credentialsId ?: 'bitbucket-user-name-cred'
    def gitEmail      = config.gitEmail ?: 'jenkins@ci.local'
    def gitUser       = config.gitUser  ?: 'jenkins-ci'

    if (!workspace || !repoSlug) {
        error "workspace and repoSlug are required parameters"
    }

    withCredentials([
        usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'BB_USER',
            passwordVariable: 'BB_TOKEN'
        )
    ]) {

        sh """
            set -e

            git config user.email "${gitEmail}"
            git config user.name "${gitUser}"
            git remote set-url origin https://${BB_USER}:${BB_TOKEN}@bitbucket.org/${workspace}/${repoSlug}.git
        """

        body()
    }
}
