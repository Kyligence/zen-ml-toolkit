# Development Guide

## Git workflow

You should follow the git workflow to contribute, fork the repo and pull request

## Dev Dependencies

- This project use **JDK17** & Maven
- Springboot 3.0+
- lombok plugin
- For macOS, you need install `coreutils` & `wget` via `brew install`
- For ZenML spec, please refer to  [ZenML Introduction](https://zen-docs.kyligence.io/en/appendix/zenml-reference)

## Build

```shell
$ mvn clean install
```

## Package

```shell
$ ./build/scripts/package.sh
```

`package.sh` generates `tar.gz` files as below:

- General package without embedded openjdk17
    - `Kyligence-ZenML-Toolkit-${project.version}.tar.gz`
- OS specific package with openjdk17
    - `Kyligence-ZenML-Toolkit-Darwin-AArch64-${project.version}.tar.gz`
    - `Kyligence-ZenML-Toolkit-Darwin-x64-${project.version}.tar.gz`
    - `Kyligence-ZenML-Toolkit-Linux-x64-${project.version}.tar.gz`

The openjdk17 is downloaded during package process, you can refer to `build/scripts/download-jdk.sh` for more details.

##  Docker Build
```shell
$ ./build/scripts/build_docker_image.sh
```

Start docker container

```shell
docker run -p 9000:9000 -dit kyligence/zenml-toolkit:${project.version}
```


## Entry Class

### For Command Line Interface Entry

We provide an CLI interface for user via command `./bin/zen.sh -i <arg> -o <arg>`.
The command options is defined in `io.kyligence.zenml.toolkit.ZenMlToolkitCLI`

### For Server Interface Entry

We also provide a server mode and API for user to upload a file, the metrics metadata will be extracted to a ZenML file
and an Excel file, compressed as a zip file to download.

The entry class is `io.kyligence.zenml.toolkit.ZenMlToolkitServer`

### Debug

Debug `io.kyligence.zenml.toolkit.ZenMlToolkitServer#main()`, in debug configuration, you need to define an Environment
variable named `ZEN_HOME`, the value is a directory path, put `build/conf/toolkit.properties`
to `$ZEN_HOME/conf/toolkit.properties`

### License

We use the Apache License 2.0, you should put the license content in the beginning of source code files
