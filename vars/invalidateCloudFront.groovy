def call(Map config = [:]) {

    def distributionId = config.distributionId

    sh """
        INVALIDATION_ID=\$(aws cloudfront create-invalidation \
            --distribution-id ${distributionId} \
            --paths "/*" \
            --query 'Invalidation.Id' \
            --output text)

        aws cloudfront wait invalidation-completed \
            --distribution-id ${distributionId} \
            --id \$INVALIDATION_ID
    """

    echo "CloudFront invalidation completed"
}
