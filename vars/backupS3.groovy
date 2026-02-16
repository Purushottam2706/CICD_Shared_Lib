def call(Map config = [:]) {

    def bucketName = config.bucketName
    def region     = config.region
    def distPath   = config.distPath ?: "dist"
    def keepLast   = config.keepLast ?: 2
    def s3BackupTimestamp = config.s3BackupTimestamp

    try {
        sh """
            if aws s3 ls s3://${bucketName} --region ${region} 2>&1 | grep -q ${distPath}; then
                aws s3 sync s3://${bucketName}/${distPath}/ \
                    s3://${bucketName}/backup/${s3BackupTimestamp}/${distPath}/ \
                    --region ${region}
            fi
            aws s3 ls s3://${bucketName}/backup/ --region ${region} | \
                awk '{print \$2}' | sed 's#/##' | sort -r | tail -n +${keepLast + 1} | \
                while read OLD; do
                    aws s3 rm s3://${bucketName}/backup/\$OLD/ --recursive --region ${region} --recursive
                done
        """
    } catch (Exception e) {
        error("error while createing backup in s3 ${e.getMessage()}")
        throw e
    }

}
