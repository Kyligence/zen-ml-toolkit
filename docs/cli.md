# Usage of Command Line

## Download package and extract

Please go to [Release Page](https://github.com/Kyligence/zen-ml-toolkit/releases) to download.

If your environment has been setup jdk17, you can download this package:

- `Kyligence-ZenML-Toolkit-{version}.tar.gz`: Supports Linux and MacOS

Otherwise, you can download the package with embedded openjdk 17 according to your operating system:

- `Kyligence-ZenML-Toolkit-Linux-x64-{version}.tar.gz`: Supports Linux on X86
- `Kyligence-ZenML-Toolkit-Darwin-x64-{version}.tar.gz`: Supports MacOS on intel chipset
- `Kyligence-ZenML-Toolkit-Darwin-AArch64-{version}.tar.gz`: Supports MacOS on M series chipset

After downloaded package, extract the tar file

Download the package tar archive, extract it to a local path

```shell
$ tar -zxvf Kyligence-ZenML-Toolkit-${project.version}.tar.gz
$ cd ${Kyligence-ZenML-Toolkit-${project.version}
```

## Command

For Mac users & Linux users, in Terminal, enter into the folder `${Kyligence-ZenML-Toolkit-{version}}`

```shell
$ sh ./bin/zen.sh -i <arg> -o <arg>
 -h,--help           print help message.
 -i,--in <arg>       specify the location of source file
 -o,--output <arg>   specify the output directory for metrics file
                     generated
```

Example as below:

```shell
# please replace ${Kyligence-ZenML-Toolkit-{version}} with absolute path
$ cd ${Kyligence-ZenML-Toolkit-{version}}
$ sh ./bin/zen.sh -i ./samples/superstore.tds -o ./samples/
```

You will get a `superstore.zen.yml` file in folder `Kyligence-ZenML-Toolkit-{version}/samples`

## FAQ

## Windows User

For Windows users

1. Download `Kyligence-ZenML-Toolkit-{version}.tar.gz` and extract the tar package
2. Install JDK17 on your windows
3. Enter into folder `${Kyligence-ZenML-Toolkit-{version}}/lib` in command line or terminal
4. Execute the command as below

```shell
$ java -DZEN_HOME=${Kyligence-ZenML-Toolkit-{version}} -cp ${Kyligence-ZenML-Toolkit-{version}}/lib/zen-ml-toolkit.jar  -Dloader.main=io.kyligence.zenml.toolkit.ZenMlToolkitCLI org.springframework.boot.loader.PropertiesLauncher  -i <source_file_path> -o <output_folder>
```

And welcome to contribute the windows scripts

### MacOS Security & Privacy Setting

If you are using MacOS, and if you download the package with JDK17 embedded, MacOS will block running
please follow the instruction below:

1. open the terminal, execute command: `sudo spctl --master-disable`
2. Go to  **System Preferences** --> **Security & Privacy** -- > **General**, choose **Allow apps downloaded from
   Anywhere**

