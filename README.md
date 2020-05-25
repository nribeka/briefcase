# ODK Briefcase
![Platform](https://img.shields.io/badge/platform-Java-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/getodk/briefcase.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/getodk/briefcase)
[![codecov.io](https://codecov.io/github/getodk/briefcase/branch/master/graph/badge.svg)](https://codecov.io/github/getodk/briefcase)
[![Slack](https://img.shields.io/badge/chat-on%20slack-brightgreen)](https://slack.getodk.org)

ODK Briefcase is a desktop application that can locally store survey results gathered with [ODK Collect](https://docs.getodk.org/collect-intro/). It can also be used to make local copies and CSV exports of data from [ODK Aggregate](https://docs.getodk.org/aggregate-intro/) (or compatible servers) and push data to those servers.

ODK Briefcase is part of ODK, a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the ODK project and its history [here](https://getodk.org) and read about example ODK deployments [here](https://forum.getodk.org/c/showcase).

* ODK website: [https://getodk.org](https://getodk.org)
* ODK Briefcase usage instructions: [https://docs.getodk.org/briefcase-intro/](https://docs.getodk.org/briefcase-intro/)
* ODK forum: [https://forum.getodk.org](https://forum.getodk.org)
* ODK developer Slack chat: [https://slack.getodk.org](https://slack.getodk.org)
* ODK developer wiki: [https://github.com/getodk/getodk/wiki](https://github.com/getodk/getodk/wiki)

## Setting up your development environment

1. Fork the briefcase project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/briefcase

We recommend using [IntelliJ IDEA](https://www.jetbrains.com/idea/) for development. On the welcome screen, click `Import Project`, navigate to your briefcase folder, and select the `build.gradle` file. Use the defaults through the wizard. Once the project is imported, IntelliJ may ask you to update your remote maven repositories. Follow the instructions to do so.

If you're using IntelliJ IDEA, we also recommend you [import the code style scheme](https://www.jetbrains.com/help/idea/copying-code-style-settings.html) for Briefcase at `config/codestyle/codestyle.xml`. Once you activate that scheme, use the automatic reformatting tool to produce code that will comply with the checkstyle rules of the project.

The main class is `org.opendatakit.briefcase.ui.MainBriefcaseWindow`. This repository also contains code for three smaller utilities with the following main classes:
- `org.opendatakit.briefcase.ui.CharsetConverterDialog` converts CSVs to UTF-8
- `org.opendatakit.briefcase.ui.MainClearBriefcasePreferencesWindow` clears Briefcase preferences
- `org.opendatakit.briefcase.ui.MainFormUploaderWindow` uploads blank forms to Aggregate instances

There might be some compile errors in the IDE about a missing class `BuildConfig`. That class is generated by gradle and the warnings can be ignored.

If you are working with [encrypted forms](https://docs.getodk.org/encrypted-forms/) you may get an `InvalidKeyException`. This is because you do not have an unlimited crypto policy enabled in Java. Do this:

* Java 8 Update 151 or later: Set `crypto.policy=unlimited` in `$JAVA_HOME/jre/lib/security/java.security`
* Java 8: Install [Unlimited Strength Policy Files 8](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) in `$JAVA_HOME/jre/lib/security`
* Java 7: Install [Unlimited Strength Policy Files 7](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html) in `$JAVA_HOME/jre/lib/security`

## Running the project

To run the project, go to the `View` menu, then `Tool Windows > Gradle`. `run` will be in `odk-briefcase > Tasks > application > run`. Double-click `run` to run the application. This Gradle task will now be the default action in your `Run` menu.

You must use the Gradle task to run the application because there is a generated class (`BuildConfig`) that IntelliJ may not properly import and recognize.

To package a runnable jar, use the `jar` Gradle task.

To try the app, you can use the demo server. In the window that opens when running, choose Connect, then fill in the URL [https://opendatakit.appspot.com](https://opendatakit.appspot.com) leave username and password blank.

## Logging
Briefcase uses [SLF4J](https://www.slf4j.org/) with [Logback Classic](https://logback.qos.ch/) binding. The project also loads the [jcl-over-slf4j](https://www.slf4j.org/legacy.html) bridge for libraries that still use old [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/).

There are example configuration files that you can use while developing:
- Copy `test/resources/logback-test.xml.example` to `test/resources/logback-test.xml`. This conf will be used when running tests.
- Copy `res/logback.xml.example` to `res/logback.xml`. This conf will be used when launching Briefcase on your machine.

### Logging tests vs development vs release
During the release process, we use a specific logback for release. This configuration sends exceptions to Sentry.io and also logs to a `briefcase.log` file, created in the same folder where Briefcase is launched by the user.

For testing and development purposes, customization of logback conf files is encouraged, especially to filter different levels of logging for specific packages. The following example sets the default level to `INFO` and `DEBUG` for components under `org.opendatakit`:

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <logger name="org.opendatakit" level="debug"/>

  <root level="info">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```

More information on Logback configuration is available [here](https://logback.qos.ch/manual/configuration.html).

## Extended topics

There is a [`/docs`](./docs) directory in the repo with more documentation files that expand on certain topics:

- [Briefcase export CSV format](./docs/export-format.md)
- [How to release a Briefcase version](./docs/how-to-release.md)

## Contributing code
Any and all contributions to the project are welcome. ODK Briefcase is used across the world primarily by organizations with a social purpose so you can have real impact!

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

## Contributing testing
All releases are verified on the following operating systems:
* Ubuntu 16.04
* Windows 10
* OS X 10.11.6

Testing checklists can be found on the [Briefcase testing plan](https://docs.google.com/spreadsheets/d/1H46G7OW21rk5skSyjpEx3dCZVv5Ly4WDK8LISmrz714/edit?usp=sharing).

If you have finished testing a pull request, please use a template from [Testing result templates](.github/TESTING_RESULT_TEMPLATES.md) to report your insights.

## Downloading builds
Per-commit debug builds can be found on [CircleCI](https://circleci.com/gh/getodk/briefcase). Login with your GitHub account, click the build you'd like, then find the JAR in the Artifacts tab under $CIRCLE_ARTIFACTS/libs.

Current and previous production builds can be found on the [ODK website](https://getodk.org/software/#odk-briefcase).
