def call(Map config = [:]) {

    def workspace  = config.workspace
    def repoSlug   = config.repoSlug
    def buildNumber = config.buildNumber ?: env.BUILD_NUMBER
    def credentialsId = config.credentialsId

    def timestamp = sh(
        script: 'date +%Y%m%d-%H%M%S',
        returnStdout: true
    ).trim()

    def releaseTag = "${timestamp}.${buildNumber}"
    withBitbucketAuth( 
        workspace: workspace,
        repoSlug: repoSlug,
        credentialsId:credentialsId
    ) {

        sh """
            git tag -a ${releaseTag} -m "Release ${releaseTag}"
            git push origin ${releaseTag}
        """
    }

    return [
        releaseTag:releaseTag,
        timestamp:timestamp
    ]
}
