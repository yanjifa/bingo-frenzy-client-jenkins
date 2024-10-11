def build(String platform, Map params) {
    stage("Build ${platform}") {
        echo "Building ${platform}..."
    }
}

return this;