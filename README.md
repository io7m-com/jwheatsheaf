jwheatsheaf
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.jwheatsheaf/com.io7m.jwheatsheaf.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.jwheatsheaf%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.jwheatsheaf/com.io7m.jwheatsheaf?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/jwheatsheaf/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/jwheatsheaf.svg?style=flat-square)](https://codecov.io/gh/io7m-com/jwheatsheaf)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=007fff)

![com.io7m.jwheatsheaf](./src/site/resources/jwheatsheaf.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jwheatsheaf/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/jwheatsheaf/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jwheatsheaf/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/jwheatsheaf/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jwheatsheaf/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/jwheatsheaf/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jwheatsheaf/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/jwheatsheaf/actions?query=workflow%3Amain.windows.temurin.lts)|


# jwheatsheaf

An alternative to JavaFX's FileChooser that aims to be feature-compatible,
if not fully API-compatible.

### Features

  * Configurable, styleable (CSS), consistent, non-native JavaFX file chooser.
  * Compatible with any JSR 203 filesystem.
  * Directory creation.
  * Configurable, extensible file/directory filtering.
  * Simple case-insensitive directory searching.
  * Written in pure Java 21.
  * [OSGi](https://www.osgi.org/) ready
  * [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System) ready
  * ISC license
  * High-coverage automated test suite

### Motivation

[JavaFX](https://openjfx.io/) provides `FileChooser` and `DirectoryChooser`
classes that delegate to the operating system's default file chooser implementation on each platform.
This is in contrast to the file chooser abstractions available in [Swing](https://docs.oracle.com/en/java/javase/13/docs/api/java.desktop/javax/swing/JFileChooser.html),
which provides a single non-native file chooser that behaves identically on all platforms. While native file
choosers do have certain benefits, it also means that the file choosers in JavaFX applications cannot be easily
styled to match the rest of the application. It also means that applications, for better or worse, behave
slightly differently on each platform.  The purpose of the `jwheatsheaf` package is to provide a
configurable, styleable, consistent, non-native file chooser implementation analogous to the `JFileChooser`
class.

### Building

```
$ mvn clean verify
```

Note: The above will run the test suite, and this _will_ take over your
keyboard and mouse for the duration of the test suite run. If you don't
want to run the tests, use the `skipTests` propery:

```
$ mvn -DskipTests=true clean verify
```

### Usage

The simplest possible code that can open a file chooser and select at most one file:

```
final Window mainWindow = ...;

final var configuration =
  JWFileChooserConfiguration.builder()
    .build();

final var choosers = JWFileChoosers.create();
final var chooser = choosers.create(configuration);
final List<Path> selected = chooser.showAndWait();
```

The above code will open a modal file chooser that can choose files from the 
default Java NIO filesystem, and will return the selected files (if any) in 
`selected`.

### Configuration

The `JWFileChooserConfiguration` class comes with numerous configuration parameters,
with the `FileSystem` parameter being the only mandatory parameter.

#### Filtering

A list of file filters can be passed to file choosers via the `fileFilters` configuration
parameter. File choosers are _always_ equipped with a file filter that displays all files
(in other words, does no filtering) even if an empty list is passed in `fileFilters`. The
list of file filters will appear in the menu at the bottom of file chooser dialogs, allowing the
user to select one to filter results.

```
final var configuration =
  JWFileChooserConfiguration.builder()
    .addFileFilters(new ExampleFilterRejectAll())
    .addFileFilters(new ExampleFilterXML())
    .build();
```

#### Glob Filtering

The [com.io7m.jwheatsheaf.filter.glob](com.io7m.jwheatsheaf.filter.glob) module
provides a convenient system for constructing rule-based filters for files.

A _Glob Filter_ consists of rules evaluated in declaration order against incoming
filenames.

A filter rule must be one of `INCLUDE`, `EXCLUDE`, `INCLUDE_AND_HALT`, or 
`EXCLUDE_AND_HALT`. The incoming file names are matched against the 
patterns given in the filter rules. The patterns are given in
[PathMatcher glob syntax](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)).

A file is included or excluded based on the result of the last rule that matched 
the file.

The `INCLUDE` rule marks a file as included if the pattern matches the file 
name. Evaluation of other rules continues if the pattern matches.

The `EXCLUDE` rule marks a file as excluded if the pattern matches the file 
name. Evaluation of other rules continues if the pattern matches.

The `INCLUDE_AND_HALT` rule marks a file as included if the pattern matches 
the file name. Evaluation of other rules halts if the pattern matches.

The `EXCLUDE_AND_HALT` rule marks a file as excluded if the pattern matches 
the file name. Evaluation of other rules halts if the pattern matches.

If no rules are specified at all, no files are included. If no rules 
match at all for a given file, the file is not included.

As an example, a filter that allows access to `.txt` and `.png` files but
excludes `data.txt`:

```
final var filters =
  new JWFilterGlobFactory();

final var filter =
  filters.create("Text and images (*.txt, *.png)")
    .addRule(INCLUDE, "**/*.txt")
    .addRule(INCLUDE, "**/*.png")
    .addRule(EXCLUDE_AND_HALT, "**/data.txt")
    .build();
```

#### Action

By default, file choosers are configured to allow the selection of at most one file. The "OK"
button cannot be clicked until one file is selected. Other behaviours can be specified by
setting the _action_ for the chooser:

```
final var configuration =
  JWFileChooserConfiguration.builder()
    .setAction(OPEN_EXISTING_MULTIPLE)
    .build();
```

#### Home Directory

If a home directory path is specified (typically a value taken from `System.getProperty("user.home")`),
a button will be displayed in the UI that allows for navigating directly to this path.

```
final var configuration =
  JWFileChooserConfiguration.builder()
    .setHomeDirectory(someHomeDirectoryPath)
    .build();
```

#### Custom Titles

The titles of file chooser dialogs can be adjusted in the configuration. By default, a generic "Please select…"
title is used if no other value is specified.

```
final var configuration =
  JWFileChooserConfiguration.builder()
    .setTitle("Export to PNG…")
    .build();
```

#### Custom Strings

Some of the strings in the file chooser UI may be customized on a per-chooser
basis. For example, this can be useful for specifying strings such as "Export"
instead of "Save" for the "Save" button in the file chooser. To specify custom
strings, provide an implementation of [JWFileChooserStringOverridesType](com.io7m.jwheatsheaf.api/src/main/java/com/io7m/jwheatsheaf/api/JWFileChooserStringOverridesType.java)
in the constructor. An [abstract implementation](com.io7m.jwheatsheaf.api/src/main/java/com/io7m/jwheatsheaf/api/JWFileChooserStringOverridesAbstract.java)
is available for convenience.

```
final var customStrings = 
  new JWFileChooserStringOverridesAbstract() {
    @Override
    public Optional<String> buttonSave()
    {
      return Optional.of("Export");
    }
  };

final var configuration =
  JWFileChooserConfiguration.builder()
    .setStringOverrides(customStrings)
    .build();
```

#### Confirmation

The file chooser can optionally prompt for confirmation of the selection of
files when in `CREATE` mode.

```
final var configuration =
  JWFileChooserConfiguration.builder()
    .setAction(CREATE)
    .setConfirmFileSelection(true)
    .build();
```

![Confirmation](./src/site/resources/confirm.png?raw=true)

#### Recent Items

The file chooser can display a list of recently used items. It is the responsibility of
applications using the file chooser to save and otherwise manage this list between
application runs; the `jwheatsheaf` file chooser simply displays whatever list
of paths is passed in:

```
final var configuration =
  JWFileChooserConfiguration.builder()
    .setRecentFiles(List.of(
      Paths.get("/tmp/x"),
      Paths.get("/tmp/y"),
      Paths.get("/tmp/z"),
    ))
    .build();
```

#### Directory Creation

The file chooser contains a button that allows for the creation of new directories.
This can be disabled.

```
final var configuration =
  JWFileChooserConfiguration.builder()
    .setAllowDirectoryCreation(false)
    .build();
```

#### Icons

The file chooser provides a `JWFileImageSetType` interface that allows for
defining the icons used by the user interface. Users wishing to use custom icon
sets should implement this interface and pass in an instance to the configuration:

```
final var configuration =
  JWFileChooserConfiguration.builder()
    .setFileImageSet(new CustomIcons())
    .build();
```

<h4><a id="css-styling" href="#css-styling">Styling</a></h4>

The `jwheatsheaf` file chooser is styleable via CSS. By default, the file chooser applies
no styling and uses whatever is the default for the application. A custom stylesheet and icon set
can be supplied via the `JWFileChooserConfiguration` class, allowing for very different
visuals:

![Basic light theme](./src/site/resources/select0.png?raw=true)

![Olive theme](./src/site/resources/select1.png?raw=true)

All of the elements in a file chooser window are assigned CSS identifiers.

![CSS identifiers](./src/site/resources/css.png?raw=true)

|Identifier|Description|
|----------|-----------|
|fileChooserPathMenu|The path menu used to select directory ancestors.|
|fileChooserUpButton|The button used to move to the parent directory.|
|fileChooserHomeButton|The button used to move to the home directory.|
|fileChooserCreateDirectoryButton|The button used to create directories.|
|fileChooserSelectDirectButton|The button used to enter paths directly.|
|fileChooserSearchField|The search field used to filter the directory table.|
|fileChooserDirectoryTable|The table that shows the contents of the current directory.|
|fileChooserSourceList|The list view that shows the recent items and the filesystem roots.|
|fileChooserNameField|The field that shows the selected file name.|
|fileChooserFilterMenu|The menu that allows for selecting file filters.|
|fileChooserCancelButton|The cancel button.|
|fileChooserOKButton|The confirmation button.|
|fileChooserProgress|The indeterminate progress indicator shown during I/O operations.|

