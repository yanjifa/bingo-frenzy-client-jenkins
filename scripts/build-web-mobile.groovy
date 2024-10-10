def build(String platform, Map params) {
    stage("build ${platform.capitalize()}") {
        echo "Building ${platform.capitalize()}..."
    }
}

return this;