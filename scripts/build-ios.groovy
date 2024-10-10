def build(String platform, Map params) {
    stage("Build ${platform.capitalize()}") {
        echo "Building ${platform.capitalize()}..."
    }
}

return this;