def call(Map config = [:]) {

    def url = config.url

    if (!url) {
        error("Smoke test URL required")
    }

    sh """
        sleep 5

        HTTP_CODE=\$(curl -s -o /dev/null -w "%{http_code}" ${url})

        if [ "\$HTTP_CODE" != "200" ]; then
            echo "Smoke test failed"
            exit 1
        fi

        echo "Smoke test passed"
    """
}
