def call(Map config = [:]) {

    def backupTimestamp = config.backupTimestamp
    def bucketName      = config.bucketName
    def awsRegion       = config.awsRegion
    def cloudfrontId    = config.cloudfrontId
    def distPath        = config.distPath ?: "dist"

    if (!backupTimestamp?.trim()) {
        echo "No S3 backup timestamp provided. Skipping rollback."
        return
    }

    if (!bucketName || !awsRegion || !cloudfrontId) {
        error("bucketName, awsRegion, and cloudfrontId are required")
    }

    echo "=" * 60
    echo "ROLLBACK: S3 Deployment"
    echo "Bucket: ${bucketName}"
    echo "Restoring backup: ${backupTimestamp}"
    echo "=" * 60

    try {

        sh """
            set -e

            if aws s3 ls s3://${bucketName}/backup/${backupTimestamp}/ --region ${awsRegion} 2>&1 | grep -q '${distPath}'; then

                aws s3 rm s3://${bucketName}/${distPath}/ --region ${awsRegion} --recursive || true
                aws s3 sync s3://${bucketName}/backup/${backupTimestamp}/${distPath}/ s3://${bucketName}/${distPath}/ \
                --region ${awsRegion}

                INVALIDATION_ID=\$(aws cloudfront create-invalidation \
                    --distribution-id ${cloudfrontId} \
                    --paths "/*" \
                    --query 'Invalidation.Id' \
                    --output text)

                echo "S3 rollback completed successfully"

            else
                echo "WARNING: Backup folder not found. Rollback skipped."
            fi
        """

    } catch (Exception e) {
        error("CRITICAL: S3 rollback failed: ${e.getMessage()}")
        throw e
    }
}
