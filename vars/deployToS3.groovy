def call(Map config = [:]) {

    def bucketName = config.bucketName
    def region     = config.region
    def distPath   = config.distPath ?: "dist"

    try {

    sh """
        set -e
        if aws s3 ls s3://${bucketName}/${distPath}/ --region ${region} >/dev/null 2>&1; then
            aws s3 rm s3://${bucketName}/${distPath}/ --recursive --region ${region} || true
        fi

        aws s3 sync ./${distPath}/ s3://${bucketName}/${distPath}/ \
            --region ${region} \
            --exclude "*" \
            --include "*.html" \
            --include "service-worker.js" \
            --cache-control "no-cache, no-store, must-revalidate"

        echo "S3 deployment completed"
    """
    } catch (Exception e) {
        error("error while createing backup in s3 ${e.getMessage()}")
        throw e
    }

}
