# siloshare
The name siloshare stands for `simple local share` and it describes the usage scenario almost completely.
It's a simple server to run on a local network to share volatile information in the network.
Keep in mind that it has only limited security, so use it only in a local environment and at your own risk.

### Features
- Send text to the server with a HTTP POST request
- Retrieve the texts with a HTTP GET request
- Texts are stored in memory and will be lost when the server is restarted. They are deleted after 10 minutes. (configure in [application.properties](src/main/resources/application.properties)

## Build
This project can either build as an [OCI image](https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html) or a GraalVM Native Image.
Important: Change the username and password in the [application.properties](src/main/resources/application.properties) to your own values before building the project.

### OCI image
- Create the image with `./gradlew bootBuildImage`
- Then run the image, e.g. `docker run --rm siloshare:0.0.1-SNAPSHOT`

### GraalVM Native Image
This requires the GraalVM native-image compiler (version 23.0.7 or newer) to be installed and configured. See [this documentation](https://docs.spring.io/spring-boot/how-to/native-image/developing-your-first-application.html)
- Create the executable with `./gradlew nativeCompile`
- Then run the `siloshare` executable located in `build/native/nativeCompile`

## Usage
- Use the [siloshare client](https://github.com/dbaelz/siloshare-client) available for Android, iOS, Web and Desktop to interact with the server.
- For development the project includes a [bruno collection](bruno/bruno.json) with the basic API calls. Install [Bruno](https://www.usebruno.com/) to use it.

## Contribution
Feel free to contribute via pull requests.

## License
The project is licensed by the [Apache 2 license](LICENSE).