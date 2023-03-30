# Usage of Server Mode

## OS Supported

Server mode currently only support **Linux** && **MacOS**

## Download an start/stop the server

Please go to [Release Page](https://github.com/Kyligence/zen-ml-toolkit/releases) to download.

If your environment has been setup jdk17, you can download this package:

- `Kyligence-ZenML-Toolkit-{version}.tar.gz`: Supports Linux and MacOS

Otherwise, you can download the package with embedded openjdk 17 according to your operating system:

- `Kyligence-ZenML-Toolkit-Linux-x64-{version}.tar.gz`: Supports Linux on X86
- `Kyligence-ZenML-Toolkit-Darwin-x64-{version}.tar.gz`: Supports MacOS on intel chipset
- `Kyligence-ZenML-Toolkit-Darwin-AArch64-{version}.tar.gz`: Supports MacOS on M series chipset

After downloaded package, extract the tar file

```shell
$ tar -zxvf Kyligence-ZenML-Toolkit-${project.version}.tar.gz
$ cd ${Kyligence-ZenML-Toolkit-${project.version}

# start server
$ ./bin/zen.sh start
# stop server
$ ./bin/zen.sh stop
```

## API

### Health Check API

```shell
$ curl --request GET '127.0.0.1:9000/hello'
```

If the server process is alive, it will return a "hello world"

### Upload and Download API

```shell
$ curl --output /local/path/to/download_file --request POST '127.0.0.1:9000/convert_metrics' \
--header 'Content-Type: multipart/form-data' \
--form 'file=@"/local/path/to/file"'
```

Upload a file (i.e. tableau tds file) and save the zip file to `/local/path/to/download_file.zip`

Example:

```shell
$ curl --output ~/Downloads/test/superstore.zip --request POST '127.0.0.1:9000/convert_metrics' \
--header 'Content-Type: multipart/form-data' \
--form 'file=@"/Users/zhengshuai.peng/Downloads/test/Kyligence-ZenML-Toolkit-Darwin-x64-0.1.1-SNAPSHOT/samples/superstore.tds"'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  9648  100  3716  100  5932  69173   107k --:--:-- --:--:-- --:--:--  214k
```

You will find `superstore.zip` has been generated in `~/Downloads/test/superstore.zip`

## FAQ

### Change server port

The default port is `9000`, if you want to change the port, you can modify it
in `${Kyligence-ZenML-Toolkit-${project.version}/conf/toolkit.properties` and restart the server

### MacOS Security & Privacy Setting

If you are using MacOS, and if you download the package with JDK17 embedded, MacOS will block running
please follow the instruction below:

1. open the terminal, execute command: `sudo spctl --master-disable`
2. Go to  **System Preferences** --> **Security & Privacy** -- > **General**, choose **Allow apps downloaded from
   Anywhere**


