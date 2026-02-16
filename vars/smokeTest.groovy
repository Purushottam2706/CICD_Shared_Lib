def call(Map config = [:]) {
    def startCommand = config.startCommand
    def testCommand  = config.testCommand 
    def waitTime     = config.waitTime    
    def timeoutValue = config.timeout     
     
    echo "Running Smoke test..."
    try {
        sh """
            set -e

            echo "Starting application..."
            ${startCommand} &
            APP_PID=\$!

            sleep ${waitTime}

            timeout ${timeoutValue} ${testCommand} || {
                echo "Smoke tests failed!"
                kill \$APP_PID || true
                pkill -f "node" || true
                exit 1
            }

            echo "Smoke tests passed"

            kill \$APP_PID || true
            sleep 2
            pkill -f "node" || true
        """
        
        echo "Smoke tests completed successfully"

    }
    catch (Exception e) {
        echo "Smoke test failed inside shared library"
        sh 'pkill -f "node" || true'
        throw e
    }
}