# siloshare
The name siloshare stands for `simple local share` and it describes the usage scenario almost completely.
It's a simple server to run on a local network to share volatile information in the network.
Keep in mind that it has only limited security, so use it only in a local environment and at your own risk.

## Build
This project can either build as an [OCI image](https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html) or a GraalVM Native Image.

### OCI image
- Create the image with `./gradlew bootBuildImage`
- Then run the image, e.g. `docker run --rm siloshare:0.0.1-SNAPSHOT`

### 
This requires the GraalVM native-image compiler (version 22.3+) to be installed and configured. See [this documentation](https://docs.spring.io/spring-boot/how-to/native-image/developing-your-first-application.html)
- Create the executable with `./gradlew nativeCompile`
- Then run the executable located in `build/native/nativeCompile/siloshare`

## Contribution
Feel free to contribute via pull requests.

## License
The project is licensed by the [Apache 2 license](LICENSE).